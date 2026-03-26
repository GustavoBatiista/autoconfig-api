package com.gustavobatista.autoconfig.service;

import java.util.List;

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
import com.gustavobatista.autoconfig.security.RoleChecks;

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
        assertAdminOrManager();

        User seller = getCurrentUserOrThrow();
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + dto.getClientId()));
        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + dto.getCarId()));
        Accessory accessory = accessoryRepository.findById(dto.getAccessoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ACCESSORY_NOT_FOUND, "Accessory not found: " + dto.getAccessoryId()));

        assertAccessoryBelongsToCar(accessory, car);

        Order order = new Order(
                null,
                dto.getOrderDate(),
                dto.getTotalPrice(),
                dto.getStatus(),
                seller,
                client,
                car,
                List.of(accessory));

        Order saved = orderRepository.save(order);
        log.info("Order created: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO dto) {
        assertAdminOrManager();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + dto.getClientId()));
        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CAR_NOT_FOUND, "Car not found: " + dto.getCarId()));
        Accessory accessory = accessoryRepository.findById(dto.getAccessoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ACCESSORY_NOT_FOUND, "Accessory not found: " + dto.getAccessoryId()));

        assertAccessoryBelongsToCar(accessory, car);

        order.setOrderDate(dto.getOrderDate());
        order.setTotalPrice(dto.getTotalPrice());
        order.setStatus(dto.getStatus());
        order.setClientId(client);
        order.setCarId(car);
        order.setAccessories(List.of(accessory));

        Order saved = orderRepository.save(order);
        log.info("Order updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteOrder(Long id) {
        assertAdminOrManager();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));

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

    private static void assertAccessoryBelongsToCar(Accessory accessory, Car car) {
        Car accessoryCar = accessory.getCar();
        if (accessoryCar == null || !accessoryCar.getId().equals(car.getId())) {
            throw new BusinessRuleException(
                    ErrorCode.ORDER_ACCESSORY_CAR_MISMATCH,
                    "Accessory does not belong to the selected car");
        }
    }

    private OrderResponseDTO toResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getStatus(),
                toClientResponse(order.getClientId()),
                toCarResponse(order.getCarId()),
                order.getAccessories() == null
                        ? List.of()
                        : order.getAccessories().stream().map(this::toAccessoryResponse).toList());
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