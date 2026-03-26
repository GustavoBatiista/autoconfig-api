package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gustavobatista.autoconfig.dto.AccessoryRequestDTO;
import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;
import com.gustavobatista.autoconfig.entity.Accessory;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.AccessoryRepository;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.support.SecurityContextTestUtils;
import com.gustavobatista.autoconfig.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
class AccessoryServiceImplTest {

    @Mock
    private AccessoryRepository accessoryRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccessoryServiceImpl accessoryService;

    private Car car;
    private AccessoryRequestDTO validDto;

    @BeforeEach
    void setUp() {
        car = TestFixtures.car(1L);
        validDto = new AccessoryRequestDTO("Roof Rack", "Roof mounted", new BigDecimal("199.90"), 1L);
    }

    @Test
    @DisplayName("createAccessory: persiste quando carro existe e nome único por carro")
    void createAccessory_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(accessoryRepository.existsByNameIgnoreCaseAndCarId_Id("Roof Rack", 1L)).thenReturn(false);
            when(accessoryRepository.save(any(Accessory.class))).thenAnswer(inv -> {
                Accessory a = inv.getArgument(0);
                return new Accessory(50L, a.getName(), a.getDescription(), a.getPrice(), car);
            });

            AccessoryResponseDTO result = accessoryService.createAccessory(validDto);

            assertEquals(50L, result.getId());
            assertEquals("Roof Rack", result.getName());
        }
    }

    @Test
    @DisplayName("createAccessory: ResourceNotFound quando carro inexistente")
    void createAccessory_carNotFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(carRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> accessoryService.createAccessory(validDto));
            assertEquals(ErrorCode.CAR_NOT_FOUND, ex.getErrorCode());
            verify(accessoryRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createAccessory: ConflictException quando nome duplicado para o mesmo carro")
    void createAccessory_nameConflict() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(carRepository.findById(1L)).thenReturn(Optional.of(car));
            when(accessoryRepository.existsByNameIgnoreCaseAndCarId_Id("Roof Rack", 1L)).thenReturn(true);

            ConflictException ex = assertThrows(ConflictException.class, () -> accessoryService.createAccessory(validDto));
            assertEquals(ErrorCode.ACCESSORY_NAME_CONFLICT, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("createAccessory: Forbidden quando seller")
    void createAccessory_sellerForbidden() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));

            assertThrows(ForbiddenOperationException.class, () -> accessoryService.createAccessory(validDto));
        }
    }

    @Test
    @DisplayName("createAccessory: Unauthorized quando não autenticado")
    void createAccessory_unauthenticated() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockUnauthenticated()) {
            assertThrows(UnauthorizedException.class, () -> accessoryService.createAccessory(validDto));
        }
    }

    @Test
    @DisplayName("updateAccessory: ResourceNotFound quando acessório inexistente")
    void updateAccessory_accessoryNotFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(accessoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> accessoryService.updateAccessory(99L, validDto));
        }
    }

    @Test
    @DisplayName("deleteAccessory: remove quando existe")
    void deleteAccessory_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            Accessory acc = new Accessory(1L, "X", "Y", new BigDecimal("10"), car);
            when(accessoryRepository.findById(1L)).thenReturn(Optional.of(acc));

            accessoryService.deleteAccessory(1L);

            verify(accessoryRepository).delete(acc);
        }
    }

    @Test
    @DisplayName("findAccessoryById: retorna quando existe")
    void findAccessoryById_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            Accessory acc = new Accessory(1L, "X", "Y", new BigDecimal("10"), car);
            when(accessoryRepository.findById(1L)).thenReturn(Optional.of(acc));

            AccessoryResponseDTO dto = accessoryService.findAccessoryById(1L);

            assertEquals(1L, dto.getId());
        }
    }

    @Test
    @DisplayName("findAllAccessories: lista quando autenticado")
    void findAllAccessories_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(accessoryRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(new Accessory(1L, "X", "Y", new BigDecimal("10"), car))));

            assertEquals(1, accessoryService.findAllAccessories(PageRequest.of(0, 20)).getContent().size());
        }
    }
}
