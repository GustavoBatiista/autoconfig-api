package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
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
import com.gustavobatista.autoconfig.repository.VehicleEntryRepository;
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

    @Mock
    private VehicleEntryRepository vehicleEntryRepository;

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
        validDto = new OrderRequestDTO(1L, 2L, List.of(3L));
        lenient().when(vehicleEntryRepository.findByOrderId_Id(anyLong())).thenReturn(Optional.empty());
    }

    private static Order copyOrderWithId(Order o, Long id) {
        return new Order(
                id,
                o.getOrderDate(),
                o.getTotalPrice(),
                o.getStatus(),
                o.getUserId(),
                o.getClientId(),
                o.getCarId(),
                o.getAccessories(),
                o.isVehicleArrived(),
                o.isAccessoriesConfirmed(),
                o.isInstallationCompleted());
    }

    @Test
    @DisplayName("createOrder: persiste quando entidades existem e acessório pertence ao carro")
    void createOrder_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(seller));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(carRepository.findById(2L)).thenReturn(Optional.of(car));
            when(accessoryRepository.findAllById(List.of(3L))).thenReturn(List.of(accessory));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> copyOrderWithId(inv.getArgument(0), 100L));

            OrderResponseDTO result = orderService.createOrder(validDto);

            assertEquals(100L, result.getId());
            assertEquals(OrderStatus.WAITING_VEHICLE, result.getStatus());
            assertEquals(0, new BigDecimal("50.00").compareTo(result.getTotalPrice()));
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("createOrder: totalPrice é soma dos acessórios selecionados")
    void createOrder_totalSumOfAccessories() {
        Accessory second = new Accessory(4L, "Moldura", "Chrome", new BigDecimal("25.50"), car);
        OrderRequestDTO dto = new OrderRequestDTO(1L, 2L, List.of(3L, 4L));
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(seller));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(carRepository.findById(2L)).thenReturn(Optional.of(car));
            when(accessoryRepository.findAllById(List.of(3L, 4L))).thenReturn(List.of(accessory, second));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> copyOrderWithId(inv.getArgument(0), 200L));

            OrderResponseDTO result = orderService.createOrder(dto);

            assertEquals(0, new BigDecimal("75.50").compareTo(result.getTotalPrice()));
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
            when(accessoryRepository.findAllById(List.of(3L))).thenReturn(List.of(accessory));

            OrderRequestDTO dto = new OrderRequestDTO(1L, 99L, List.of(3L));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> orderService.createOrder(dto));
            assertEquals(ErrorCode.ORDER_ACCESSORY_CAR_MISMATCH, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("createOrder: ResourceNotFound quando id de acessório inexistente")
    void createOrder_accessoryNotFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(seller));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(carRepository.findById(2L)).thenReturn(Optional.of(car));
            when(accessoryRepository.findAllById(List.of(3L, 999L))).thenReturn(List.of(accessory));

            OrderRequestDTO dto = new OrderRequestDTO(1L, 2L, List.of(3L, 999L));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(dto));
            assertEquals(ErrorCode.ACCESSORY_NOT_FOUND, ex.getErrorCode());
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
            when(accessoryRepository.findAllById(List.of(3L))).thenReturn(List.of(accessory));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> copyOrderWithId(inv.getArgument(0), 100L));

            OrderResponseDTO result = orderService.createOrder(validDto);

            assertEquals(100L, result.getId());
            verify(orderRepository).save(any(Order.class));
            verify(accessoryRepository).findAllById(anyList());
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
