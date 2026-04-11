package com.gustavobatista.autoconfig.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;
import com.gustavobatista.autoconfig.dto.ClientResponseDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;
import com.gustavobatista.autoconfig.dto.VehicleEntryRequestDTO;
import com.gustavobatista.autoconfig.dto.VehicleEntryResponseDTO;
import com.gustavobatista.autoconfig.entity.Accessory;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.entity.Order;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.entity.VehicleEntry;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.repository.OrderRepository;
import com.gustavobatista.autoconfig.repository.VehicleEntryRepository;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleEntryRepository vehicleEntryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public VehicleServiceImpl(
            VehicleEntryRepository vehicleEntryRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.vehicleEntryRepository = vehicleEntryRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VehicleEntryResponseDTO createVehicle(VehicleEntryRequestDTO dto) {
        assertAuthenticated();

        String chassis = trim(dto.getChassis());
        assertUniqueChassis(chassis, null);

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + dto.getOrderId()));

        VehicleEntry saved = vehicleEntryRepository.save(
                new VehicleEntry(null, chassis, dto.getArrivalDate(), dto.getCondition(), order));

        log.info("Vehicle entry created: id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public VehicleEntryResponseDTO updateVehicle(Long id, VehicleEntryRequestDTO dto) {
        assertAuthenticated();

        VehicleEntry vehicle = vehicleEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VEHICLE_ENTRY_NOT_FOUND, "Vehicle entry not found: " + id));

        String chassis = trim(dto.getChassis());
        assertUniqueChassis(chassis, id);

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + dto.getOrderId()));

        vehicle.setChassis(chassis);
        vehicle.setArrivalDate(dto.getArrivalDate());
        vehicle.setCondition(dto.getCondition());
        vehicle.setOrderId(order);

        VehicleEntry saved = vehicleEntryRepository.save(vehicle);
        log.info("Vehicle entry updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteVehicle(Long id) {
        assertAuthenticated();

        VehicleEntry vehicle = vehicleEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VEHICLE_ENTRY_NOT_FOUND, "Vehicle entry not found: " + id));

        vehicleEntryRepository.delete(vehicle);
        log.info("Vehicle entry deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleEntryResponseDTO> findAllVehicles() {
        assertAuthenticated();

        return vehicleEntryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleEntryResponseDTO findVehicleById(Long id) {
        assertAuthenticated();

        VehicleEntry vehicle = vehicleEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VEHICLE_ENTRY_NOT_FOUND, "Vehicle entry not found: " + id));

        return toResponse(vehicle);
    }

    private void assertUniqueChassis(String chassis, Long excludeId) {
        boolean duplicate = excludeId == null
                ? vehicleEntryRepository.existsByChassisIgnoreCase(chassis)
                : vehicleEntryRepository.existsByChassisIgnoreCaseAndIdNot(chassis, excludeId);

        if (duplicate) {
            throw new ConflictException(ErrorCode.VEHICLE_CHASSIS_CONFLICT, "A vehicle entry with this chassis already exists");
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private VehicleEntryResponseDTO toResponse(VehicleEntry vehicle) {
        return new VehicleEntryResponseDTO(
                vehicle.getId(),
                vehicle.getChassis(),
                vehicle.getArrivalDate(),
                vehicle.getCondition(),
                toOrderResponse(vehicle.getOrderId()));
    }

    private OrderResponseDTO toOrderResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                order.getStatus(),
                toClientResponse(order.getClientId()),
                toCarResponse(order.getCarId()),
                order.getAccessories() == null
                        ? List.of()
                        : order.getAccessories().stream().map(this::toAccessoryResponse).toList());
    }

    private ClientResponseDTO toClientResponse(Client client) {
        return new ClientResponseDTO(client.getId(), client.getName(), client.getLastName(), client.getPhoneNumber());
    }

    private CarResponseDTO toCarResponse(Car car) {
        return new CarResponseDTO(car.getId(), car.getBrand(), car.getModel(), car.getVersion());
    }

    private AccessoryResponseDTO toAccessoryResponse(Accessory accessory) {
        Car car = accessory.getCar();
        CarResponseDTO carDto = car == null ? null : toCarResponse(car);
        return new AccessoryResponseDTO(
                accessory.getId(),
                accessory.getName(),
                accessory.getDescription(),
                accessory.getPrice(),
                carDto);
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