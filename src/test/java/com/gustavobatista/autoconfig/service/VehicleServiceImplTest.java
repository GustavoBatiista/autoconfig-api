package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import com.gustavobatista.autoconfig.dto.VehicleEntryRequestDTO;
import com.gustavobatista.autoconfig.dto.VehicleEntryResponseDTO;
import com.gustavobatista.autoconfig.entity.Order;
import com.gustavobatista.autoconfig.entity.VehicleEntry;
import com.gustavobatista.autoconfig.enums.VehicleCondition;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.OrderRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.repository.VehicleEntryRepository;
import com.gustavobatista.autoconfig.support.SecurityContextTestUtils;
import com.gustavobatista.autoconfig.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    private static final String CHASSIS_17 = "12345678901234567";

    @Mock
    private VehicleEntryRepository vehicleEntryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Order order;
    private VehicleEntryRequestDTO validDto;

    @BeforeEach
    void setUp() {
        order = TestFixtures.order(1L, TestFixtures.userAdmin(), TestFixtures.client(1L), TestFixtures.car(2L));
        LocalDateTime arrival = LocalDateTime.of(2025, 3, 1, 8, 0);
        validDto = new VehicleEntryRequestDTO(CHASSIS_17, arrival, VehicleCondition.PERFECT, 1L);
    }

    @Test
    @DisplayName("createVehicle: persiste quando pedido existe e chassis único")
    void createVehicle_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(vehicleEntryRepository.existsByChassisIgnoreCase(CHASSIS_17)).thenReturn(false);
            when(vehicleEntryRepository.save(any(VehicleEntry.class))).thenAnswer(inv -> {
                VehicleEntry v = inv.getArgument(0);
                return new VehicleEntry(200L, v.getChassis(), v.getArrivalDate(), v.getCondition(), v.getOrderId());
            });

            VehicleEntryResponseDTO result = vehicleService.createVehicle(validDto);

            assertEquals(200L, result.getId());
            assertEquals(CHASSIS_17, result.getChassis());
        }
    }

    @Test
    @DisplayName("createVehicle: ResourceNotFound quando pedido inexistente")
    void createVehicle_orderNotFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> vehicleService.createVehicle(validDto));
            assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
            verify(vehicleEntryRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createVehicle: ConflictException quando chassis duplicado")
    void createVehicle_chassisConflict() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            // Chassis check runs before order lookup — do not stub orderRepository here
            when(vehicleEntryRepository.existsByChassisIgnoreCase(CHASSIS_17)).thenReturn(true);

            ConflictException ex = assertThrows(ConflictException.class, () -> vehicleService.createVehicle(validDto));
            assertEquals(ErrorCode.VEHICLE_CHASSIS_CONFLICT, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("createVehicle: persiste quando seller e pedido existe")
    void createVehicle_sellerAllowed() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(vehicleEntryRepository.existsByChassisIgnoreCase(CHASSIS_17)).thenReturn(false);
            when(vehicleEntryRepository.save(any(VehicleEntry.class))).thenAnswer(inv -> {
                VehicleEntry v = inv.getArgument(0);
                return new VehicleEntry(200L, v.getChassis(), v.getArrivalDate(), v.getCondition(), v.getOrderId());
            });

            VehicleEntryResponseDTO result = vehicleService.createVehicle(validDto);

            assertEquals(200L, result.getId());
        }
    }

    @Test
    @DisplayName("createVehicle: Unauthorized quando não autenticado")
    void createVehicle_unauthenticated() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockUnauthenticated()) {
            assertThrows(UnauthorizedException.class, () -> vehicleService.createVehicle(validDto));
        }
    }

    @Test
    @DisplayName("updateVehicle: ResourceNotFound quando entrada inexistente")
    void updateVehicle_entryNotFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(vehicleEntryRepository.findById(50L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> vehicleService.updateVehicle(50L, validDto));
        }
    }

    @Test
    @DisplayName("findVehicleById: ResourceNotFound quando inexistente")
    void findVehicleById_notFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(vehicleEntryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> vehicleService.findVehicleById(1L));
        }
    }

    @Test
    @DisplayName("findAllVehicles: lista quando autenticado")
    void findAllVehicles_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            VehicleEntry v = new VehicleEntry(1L, CHASSIS_17, LocalDateTime.now(), VehicleCondition.PERFECT, order);
            when(vehicleEntryRepository.findAll()).thenReturn(List.of(v));

            assertEquals(1, vehicleService.findAllVehicles().size());
        }
    }
}
