package com.gustavobatista.autoconfig.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;
import com.gustavobatista.autoconfig.dto.ClientResponseDTO;
import com.gustavobatista.autoconfig.dto.ConfirmVehicleDTO;
import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;
import com.gustavobatista.autoconfig.dto.VehicleEntrySummaryDTO;
import com.gustavobatista.autoconfig.entity.Accessory;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.entity.Order;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.entity.VehicleEntry;
import com.gustavobatista.autoconfig.enums.OrderStatus;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.exception.BusinessRuleException;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.AccessoryRepository;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.ClientRepository;
import com.gustavobatista.autoconfig.repository.OrderRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.repository.VehicleEntryRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    private final AccessoryRepository accessoryRepository;
    private final VehicleEntryRepository vehicleEntryRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            CarRepository carRepository,
            AccessoryRepository accessoryRepository,
            VehicleEntryRepository vehicleEntryRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.carRepository = carRepository;
        this.accessoryRepository = accessoryRepository;
        this.vehicleEntryRepository = vehicleEntryRepository;
    }

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        assertAuthenticated();

        User seller = getCurrentUserOrThrow();
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + dto.getClientId()));
        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + dto.getCarId()));

        List<Accessory> accessories = loadAccessoriesForCar(dto.getAccessoryIds(), car);
        BigDecimal totalPrice = sumAccessoryPrices(accessories);

        Order order = new Order(
                null,
                LocalDateTime.now(),
                totalPrice,
                OrderStatus.WAITING_VEHICLE,
                seller,
                client,
                car,
                accessories,
                false,
                false,
                false);
        updateStatus(order);

        Order saved = orderRepository.save(order);
        log.info("Order created: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO dto) {
        assertAuthenticated();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

        assertCanMutateOrder(order);

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + dto.getClientId()));
        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + dto.getCarId()));

        List<Accessory> accessories = loadAccessoriesForCar(dto.getAccessoryIds(), car);
        BigDecimal totalPrice = sumAccessoryPrices(accessories);

        order.setTotalPrice(totalPrice);
        order.setClientId(client);
        order.setCarId(car);
        order.setAccessories(accessories);
        updateStatus(order);

        Order saved = orderRepository.save(order);
        log.info("Order updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteOrder(Long id) {
        assertAuthenticated();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

        assertCanMutateOrder(order);

        orderRepository.delete(order);
        log.info("Order deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAllOrders(Pageable pageable) {
        assertAuthenticated();

        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO findOrderById(Long id) {
        assertAuthenticated();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

        return toResponse(order);
    }

    @Override
    public OrderResponseDTO confirmVehicle(Long orderId, ConfirmVehicleDTO dto) {
        assertAuthenticated();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

        assertCanConfirmVehicle(order);

        String chassis = trim(dto.getChassis());
        Optional<VehicleEntry> existingOpt = vehicleEntryRepository.findByOrderId_Id(orderId);
        Long excludeId = existingOpt.map(VehicleEntry::getId).orElse(null);
        assertUniqueChassisForConfirm(chassis, excludeId);

        VehicleEntry entry = existingOpt.orElseGet(VehicleEntry::new);
        entry.setOrderId(order);
        entry.setChassis(chassis);
        entry.setArrivalDate(dto.getArrivalDate());
        entry.setCondition(dto.getCondition());
        vehicleEntryRepository.save(entry);

        order.setVehicleArrived(true);
        updateStatus(order);
        Order saved = orderRepository.save(order);
        log.info("Order vehicle confirmed: id={}", orderId);
        return toResponse(saved);
    }

    @Override
    public OrderResponseDTO confirmAccessories(Long orderId) {
        assertAuthenticated();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

        assertCanConfirmAccessories(order);

        if (!order.isVehicleArrived()) {
            throw new BusinessRuleException(
                    ErrorCode.ORDER_CONFIRMATION_SEQUENCE,
                    "Vehicle must be confirmed before accessories");
        }

        order.setAccessoriesConfirmed(true);
        updateStatus(order);
        Order saved = orderRepository.save(order);
        log.info("Order accessories confirmed: id={}", orderId);
        return toResponse(saved);
    }

    @Override
    public OrderResponseDTO confirmInstallation(Long orderId) {
        assertAuthenticated();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

        assertCanMutateOrder(order);

        if (!order.isVehicleArrived() || !order.isAccessoriesConfirmed()) {
            throw new BusinessRuleException(
                    ErrorCode.ORDER_CONFIRMATION_SEQUENCE,
                    "Vehicle and accessories must be confirmed before installation");
        }

        order.setInstallationCompleted(true);
        updateStatus(order);
        Order saved = orderRepository.save(order);
        log.info("Order installation confirmed: id={}", orderId);
        return toResponse(saved);
    }

    private void updateStatus(Order order) {
        if (!order.isVehicleArrived()) {
            order.setStatus(OrderStatus.WAITING_VEHICLE);
        } else if (!order.isAccessoriesConfirmed()) {
            order.setStatus(OrderStatus.WAITING_ACCESSORIES);
        } else if (!order.isInstallationCompleted()) {
            order.setStatus(OrderStatus.WAITING_SCHEDULING);
        } else {
            order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        }
    }

    private List<Accessory> loadAccessoriesForCar(List<Long> requestedIds, Car car) {
        List<Long> distinctOrdered = new ArrayList<>(new LinkedHashSet<>(requestedIds));

        List<Accessory> loaded = accessoryRepository.findAllById(distinctOrdered);
        if (loaded.size() != distinctOrdered.size()) {
            Set<Long> foundIds = loaded.stream().map(Accessory::getId).collect(Collectors.toSet());
            for (Long accessoryId : distinctOrdered) {
                if (!foundIds.contains(accessoryId)) {
                    throw new ResourceNotFoundException(ErrorCode.ACCESSORY_NOT_FOUND, "Accessory not found: " + accessoryId);
                }
            }
        }

        Map<Long, Accessory> byId = loaded.stream().collect(Collectors.toMap(Accessory::getId, a -> a));
        List<Accessory> ordered = new ArrayList<>();
        for (Long accessoryId : distinctOrdered) {
            Accessory accessory = byId.get(accessoryId);
            assertAccessoryBelongsToCar(accessory, car);
            ordered.add(accessory);
        }
        return ordered;
    }

    private static BigDecimal sumAccessoryPrices(List<Accessory> accessories) {
        return accessories.stream()
                .map(Accessory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void assertAccessoryBelongsToCar(Accessory accessory, Car car) {
        Car accessoryCar = accessory.getCar();
        if (accessoryCar == null || !accessoryCar.getId().equals(car.getId())) {
            throw new BusinessRuleException(
                    ErrorCode.ORDER_ACCESSORY_CAR_MISMATCH,
                    "Accessory does not belong to the selected car");
        }
    }

    private OrderResponseDTO toResponse(Order order) {
        User seller = order.getUserId();
        Long sellerId = seller == null ? null : seller.getId();
        VehicleEntrySummaryDTO vehicleSummary = null;
        if (order.getId() != null) {
            vehicleSummary = vehicleEntryRepository.findByOrderId_Id(order.getId())
                    .map(this::toVehicleEntrySummary)
                    .orElse(null);
        }
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getTotalPrice(),
                order.getStatus(),
                order.isVehicleArrived(),
                order.isAccessoriesConfirmed(),
                order.isInstallationCompleted(),
                sellerId,
                sellerDisplayName(seller),
                toClientResponse(order.getClientId()),
                toCarResponse(order.getCarId()),
                order.getAccessories() == null
                        ? List.of()
                        : order.getAccessories().stream().map(this::toAccessoryResponse).toList(),
                vehicleSummary);
    }

    private static String sellerDisplayName(User seller) {
        if (seller == null) {
            return null;
        }
        String first = seller.getName() == null ? "" : seller.getName().trim();
        String last = seller.getLastName() == null ? "" : seller.getLastName().trim();
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? null : combined;
    }

    private VehicleEntrySummaryDTO toVehicleEntrySummary(VehicleEntry entry) {
        return new VehicleEntrySummaryDTO(
                entry.getId(),
                entry.getChassis(),
                entry.getArrivalDate(),
                entry.getCondition());
    }

    private void assertCanMutateOrder(Order order) {
        User current = getCurrentUserOrThrow();
        Role role = current.getRole();
        if (role == Role.ROLE_ADMIN || role == Role.ROLE_MANAGER) {
            return;
        }
        if (role == Role.ROLE_SELLER) {
            User owner = order.getUserId();
            if (owner == null || !owner.getId().equals(current.getId())) {
                throw new ForbiddenOperationException(
                        ErrorCode.ORDER_MUTATION_FORBIDDEN,
                        "Sellers may only modify or delete orders they created");
            }
            return;
        }
        throw new ForbiddenOperationException(
                ErrorCode.ORDER_MUTATION_FORBIDDEN,
                "Not allowed to modify orders");
    }

    private void assertCanConfirmVehicle(Order order) {
        User current = getCurrentUserOrThrow();
        Role role = current.getRole();
        if (role == Role.ROLE_ADMIN || role == Role.ROLE_MANAGER || role == Role.ROLE_VEHICLE_STOCK) {
            return;
        }
        if (role == Role.ROLE_SELLER) {
            User owner = order.getUserId();
            if (owner != null && owner.getId().equals(current.getId())) {
                return;
            }
        }
        throw new ForbiddenOperationException(
                ErrorCode.ORDER_MUTATION_FORBIDDEN,
                "Not allowed to confirm vehicle for this order");
    }

    private void assertCanConfirmAccessories(Order order) {
        User current = getCurrentUserOrThrow();
        Role role = current.getRole();
        if (role == Role.ROLE_ADMIN || role == Role.ROLE_MANAGER || role == Role.ROLE_ACCESSORY_STOCK) {
            return;
        }
        if (role == Role.ROLE_SELLER) {
            User owner = order.getUserId();
            if (owner != null && owner.getId().equals(current.getId())) {
                return;
            }
        }
        throw new ForbiddenOperationException(
                ErrorCode.ORDER_MUTATION_FORBIDDEN,
                "Not allowed to confirm accessories for this order");
    }

    private void assertUniqueChassisForConfirm(String chassis, Long excludeVehicleEntryId) {
        if (excludeVehicleEntryId == null) {
            if (vehicleEntryRepository.existsByChassisIgnoreCase(chassis)) {
                throw new ConflictException(ErrorCode.VEHICLE_CHASSIS_CONFLICT, "A vehicle entry with this chassis already exists");
            }
        } else if (vehicleEntryRepository.existsByChassisIgnoreCaseAndIdNot(chassis, excludeVehicleEntryId)) {
            throw new ConflictException(ErrorCode.VEHICLE_CHASSIS_CONFLICT, "A vehicle entry with this chassis already exists");
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private ClientResponseDTO toClientResponse(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getLastName(),
                client.getPhoneNumber());
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
