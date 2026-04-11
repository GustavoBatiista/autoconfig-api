package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;
import com.gustavobatista.autoconfig.entity.Accessory;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.entity.Order;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.OrderStatus;
import com.gustavobatista.autoconfig.exception.BusinessRuleException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.AccessoryRepository;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.ClientRepository;
import com.gustavobatista.autoconfig.repository.OrderRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.support.SecurityContextTestUtils;
import com.gustavobatista.autoconfig.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AccessoryRepository accessoryRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User seller;
    private Client client;
    private Car car;
    private Accessory accessory;
    private OrderRequestDTO validDto;

    @BeforeEach
    void setUp() {
        seller = TestFixtures.userAdmin();
        client = TestFixtures.client(1L);
        car = TestFixtures.car(2L);
        accessory = new Accessory(3L, "Rack", "Roof", new BigDecimal("50.00"), car);
        validDto = new OrderRequestDTO(1L, 2L, 3L, new BigDecimal("250.00"), OrderStatus.WAITING_FOR_VEHICLE);
    }

    @Test
    @DisplayName("createOrder: persiste quando entidades existem e acessório pertence ao carro")
    void createOrder_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(seller));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(carRepository.findById(2L)).thenReturn(Optional.of(car));
            when(accessoryRepository.findById(3L)).thenReturn(Optional.of(accessory));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                return new Order(
                        100L,
                        o.getOrderDate(),
                        o.getTotalPrice(),
                        o.getStatus(),
                        o.getUserId(),
                        o.getClientId(),
                        o.getCarId(),
                        o.getAccessories());
            });

            OrderResponseDTO result = orderService.createOrder(validDto);

            assertEquals(100L, result.getId());
            assertEquals(OrderStatus.WAITING_FOR_VEHICLE, result.getStatus());
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("createOrder: ResourceNotFound quando cliente inexistente")
    void createOrder_clientNotFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(seller));
            when(clientRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> orderService.createOrder(validDto));
            assertEquals(ErrorCode.CLIENT_NOT_FOUND, ex.getErrorCode());
            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createOrder: BusinessRuleException quando acessório não pertence ao carro selecionado")
    void createOrder_accessoryCarMismatch() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            Car otherCar = TestFixtures.car(99L);
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(seller));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(carRepository.findById(99L)).thenReturn(Optional.of(otherCar));
            when(accessoryRepository.findById(3L)).thenReturn(Optional.of(accessory));

            OrderRequestDTO dto = new OrderRequestDTO(1L, 99L, 3L, new BigDecimal("100.00"), OrderStatus.WAITING_FOR_VEHICLE);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> orderService.createOrder(dto));
            assertEquals(ErrorCode.ORDER_ACCESSORY_CAR_MISMATCH, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("createOrder: persiste quando vendedor autenticado e entidades existem")
    void createOrder_sellerAllowed() {
        User sellerUser = TestFixtures.userSeller();
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(sellerUser));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(carRepository.findById(2L)).thenReturn(Optional.of(car));
            when(accessoryRepository.findById(3L)).thenReturn(Optional.of(accessory));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                return new Order(
                        100L,
                        o.getOrderDate(),
                        o.getTotalPrice(),
                        o.getStatus(),
                        o.getUserId(),
                        o.getClientId(),
                        o.getCarId(),
                        o.getAccessories());
            });

            OrderResponseDTO result = orderService.createOrder(validDto);

            assertEquals(100L, result.getId());
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("createOrder: Unauthorized quando não autenticado")
    void createOrder_unauthenticated() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockUnauthenticated()) {
            assertThrows(UnauthorizedException.class, () -> orderService.createOrder(validDto));
        }
    }

    @Test
    @DisplayName("findOrderById: ResourceNotFound quando pedido inexistente")
    void findOrderById_notFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.findOrderById(1L));
        }
    }

    @Test
    @DisplayName("deleteOrder: remove quando pedido existe e usuário é admin/manager")
    void deleteOrder_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            Order order = TestFixtures.order(1L, seller, client, car);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            orderService.deleteOrder(1L);

            verify(orderRepository).delete(order);
        }
    }
}
