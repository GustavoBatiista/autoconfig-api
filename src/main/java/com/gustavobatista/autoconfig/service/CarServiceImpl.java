package com.gustavobatista.autoconfig.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.CarRequestDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.security.RoleChecks;



@Service
@Transactional
public class CarServiceImpl implements CarService {

    private static final Logger log = LoggerFactory.getLogger(CarServiceImpl.class);

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public CarServiceImpl(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CarResponseDTO createCar(CarRequestDTO dto) {
        assertAdminOrManager();

        String brand = trim(dto.getBrand());
        String model = trim(dto.getModel());
        String version = trim(dto.getVersion());

        assertUniqueCombination(brand, model, version, null);

        Car saved = carRepository.save(new Car(null, brand, model, version));
        log.info("Car created: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public CarResponseDTO updateCar(Long id, CarRequestDTO dto) {
        assertAdminOrManager();

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + id));

        String brand = trim(dto.getBrand());
        String model = trim(dto.getModel());
        String version = trim(dto.getVersion());

        assertUniqueCombination(brand, model, version, id);

        car.setBrand(brand);
        car.setModel(model);
        car.setVersion(version);

        Car saved = carRepository.save(car);
        log.info("Car updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteCar(Long id) {
        assertAdminOrManager();

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + id));

        carRepository.delete(car);
        log.info("Car deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarResponseDTO> findAllCars(Pageable pageable) {
        assertAuthenticated();

        return carRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponseDTO findCarById(Long id) {
        assertAuthenticated();

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + id));

        return toResponse(car);
    }

    private void assertUniqueCombination(String brand, String model, String version, Long excludeId) {
        boolean duplicate = excludeId == null
                ? carRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCase(brand, model, version)
                : carRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCaseAndIdNot(
                        brand, model, version, excludeId);

        if (duplicate) {
            throw new ConflictException(
                    ErrorCode.CAR_DUPLICATE,
                    "A car with the same brand, model and version already exists (case-insensitive)");
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private CarResponseDTO toResponse(Car car) {
        return new CarResponseDTO(car.getId(), car.getBrand(), car.getModel(), car.getVersion());
    }

    private void assertAdminOrManager() {
        User current = getCurrentUserOrThrow();
        if (!RoleChecks.isAdminOrManager(current.getRole())) {
            throw new ForbiddenOperationException("Admin or manager only");
        }
    }

    private void assertAuthenticated() {
        getCurrentUserOrThrow();
    }

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }
}