package com.gustavobatista.autoconfig.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavobatista.autoconfig.dto.AccessoryRequestDTO;
import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;
import com.gustavobatista.autoconfig.entity.Accessory;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.AccessoryRepository;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.security.RoleChecks;

@Service
@Transactional
public class AccessoryServiceImpl implements AccessoryService {

    private static final Logger log = LoggerFactory.getLogger(AccessoryServiceImpl.class);

    private final AccessoryRepository accessoryRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public AccessoryServiceImpl(
            AccessoryRepository accessoryRepository,
            CarRepository carRepository,
            UserRepository userRepository) {
        this.accessoryRepository = accessoryRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AccessoryResponseDTO createAccessory(AccessoryRequestDTO dto) {
        assertAdminOrManager();

        String name = trim(dto.getName());
        String description = trim(dto.getDescription());

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + dto.getCarId()));

        assertUniqueNamePerCar(name, car.getId(), null);

        Accessory saved = accessoryRepository.save(
                new Accessory(null, name, description, dto.getPrice(), car));

        log.info("Accessory created: id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public AccessoryResponseDTO updateAccessory(Long id, AccessoryRequestDTO dto) {
        assertAdminOrManager();

        Accessory accessory = accessoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ACCESSORY_NOT_FOUND, "Accessory not found: " + id));

        String name = trim(dto.getName());
        String description = trim(dto.getDescription());

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + dto.getCarId()));

        assertUniqueNamePerCar(name, car.getId(), id);

        accessory.setName(name);
        accessory.setDescription(description);
        accessory.setPrice(dto.getPrice());
        accessory.setCarId(car);

        Accessory saved = accessoryRepository.save(accessory);
        log.info("Accessory updated: id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public void deleteAccessory(Long id) {
        assertAdminOrManager();

        Accessory accessory = accessoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ACCESSORY_NOT_FOUND, "Accessory not found: " + id));

        accessoryRepository.delete(accessory);
        log.info("Accessory deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessoryResponseDTO> findAllAccessories() {
        assertAuthenticated();

        return accessoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccessoryResponseDTO findAccessoryById(Long id) {
        assertAuthenticated();

        Accessory accessory = accessoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ACCESSORY_NOT_FOUND, "Accessory not found: " + id));

        return toResponse(accessory);
    }

    private void assertUniqueNamePerCar(String name, Long carId, Long excludeId) {
        boolean duplicate = excludeId == null
                ? accessoryRepository.existsByNameIgnoreCaseAndCarId_Id(name, carId)
                : accessoryRepository.existsByNameIgnoreCaseAndCarId_IdAndIdNot(name, carId, excludeId);

        if (duplicate) {
            throw new ConflictException(
                    ErrorCode.ACCESSORY_NAME_CONFLICT,
                    "An accessory with this name already exists for this car");
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private AccessoryResponseDTO toResponse(Accessory accessory) {
        Car car = accessory.getCar();
        CarResponseDTO carDto = new CarResponseDTO(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getVersion());

        return new AccessoryResponseDTO(
                accessory.getId(),
                accessory.getName(),
                accessory.getDescription(),
                accessory.getPrice(),
                carDto);
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