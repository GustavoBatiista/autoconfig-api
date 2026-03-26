package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.gustavobatista.autoconfig.dto.CarRequestDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;
import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.CarRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.support.SecurityContextTestUtils;
import com.gustavobatista.autoconfig.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CarServiceImpl carService;

    private CarRequestDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new CarRequestDTO("Ford", "Focus", "SEL");
    }

    @Test
    @DisplayName("createCar: persiste e retorna DTO quando admin e combinação única")
    void createCar_happyPath_admin() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(carRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCase("Ford", "Focus", "SEL"))
                    .thenReturn(false);
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
                Car c = inv.getArgument(0);
                return new Car(10L, c.getBrand(), c.getModel(), c.getVersion());
            });

            CarResponseDTO result = carService.createCar(validDto);

            assertEquals(10L, result.getId());
            assertEquals("Ford", result.getBrand());
            verify(carRepository).save(any(Car.class));
        }
    }

    @Test
    @DisplayName("createCar: lança ConflictException quando marca/modelo/versão já existem")
    void createCar_duplicate_throwsConflict() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(carRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCase(anyString(), anyString(), anyString()))
                    .thenReturn(true);

            ConflictException ex = assertThrows(ConflictException.class, () -> carService.createCar(validDto));
            assertEquals(ErrorCode.CAR_DUPLICATE, ex.getErrorCode());
            verify(carRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createCar: lança ForbiddenOperationException quando papel não é admin nem manager")
    void createCar_seller_forbidden() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));

            assertThrows(ForbiddenOperationException.class, () -> carService.createCar(validDto));
            verify(carRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createCar: lança UnauthorizedException quando não autenticado")
    void createCar_unauthenticated() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockUnauthenticated()) {
            assertThrows(UnauthorizedException.class, () -> carService.createCar(validDto));
        }
    }

    @Test
    @DisplayName("updateCar: retorna DTO quando carro existe e combinação única")
    void updateCar_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            Car existing = new Car(5L, "VW", "Golf", "GTI");
            when(carRepository.findById(5L)).thenReturn(Optional.of(existing));
            when(carRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCaseAndIdNot("Ford", "Focus", "SEL", 5L))
                    .thenReturn(false);
            when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

            CarResponseDTO result = carService.updateCar(5L, validDto);

            assertEquals(5L, result.getId());
            assertEquals("Ford", result.getBrand());
        }
    }

    @Test
    @DisplayName("updateCar: lança ResourceNotFoundException quando id inexistente")
    void updateCar_notFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(carRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> carService.updateCar(99L, validDto));
            assertEquals(ErrorCode.CAR_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("deleteCar: remove quando existe e usuário é admin/manager")
    void deleteCar_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            Car existing = new Car(5L, "Ford", "Focus", "SEL");
            when(carRepository.findById(5L)).thenReturn(Optional.of(existing));

            carService.deleteCar(5L);

            verify(carRepository).delete(existing);
        }
    }

    @Test
    @DisplayName("findCarById: retorna DTO quando autenticado e carro existe")
    void findCarById_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(carRepository.findById(1L)).thenReturn(Optional.of(new Car(1L, "Ford", "Focus", "SEL")));

            CarResponseDTO result = carService.findCarById(1L);

            assertEquals(1L, result.getId());
        }
    }

    @Test
    @DisplayName("findCarById: lança ResourceNotFound quando não existe")
    void findCarById_notFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(carRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> carService.findCarById(1L));
        }
    }

    @Test
    @DisplayName("findAllCars: lista todos quando autenticado")
    void findAllCars_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(carRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(new Car(1L, "Ford", "Focus", "SEL"))));

            assertEquals(1, carService.findAllCars(PageRequest.of(0, 20)).getContent().size());
        }
    }
}
