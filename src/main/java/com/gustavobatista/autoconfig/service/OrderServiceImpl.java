package com.gustavobatista.autoconfig.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;
import com.gustavobatista.autoconfig.entity.Accessory;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.entity.Order;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.exception.BusinessRuleException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.AccessoryRepository;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.ClientRepository;
import com.gustavobatista.autoconfig.repository.OrderRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    private final AccessoryRepository accessoryRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            CarRepository carRepository,
            AccessoryRepository accessoryRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.carRepository = carRepository;
        this.accessoryRepository = accessoryRepository;
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
                dto.getStatus(),
                seller,
                client,
                car,
                accessories);

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
        order.setStatus(dto.getStatus());
        order.setClientId(client);
        order.setCarId(car);
        order.setAccessories(accessories);

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

    /**
     * Loads accessories by id (deduplicated, order preserved), ensures all exist and belong to {@code car}.
     */
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
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                order.getStatus(),
                sellerId,
                toClientResponse(order.getClientId()),
                toCarResponse(order.getCarId()),
                order.getAccessories() == null
                        ? List.of()
                        : order.getAccessories().stream().map(this::toAccessoryResponse).toList());
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
