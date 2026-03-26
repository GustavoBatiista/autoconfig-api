package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.gustavobatista.autoconfig.dto.ClientRequestDTO;
import com.gustavobatista.autoconfig.dto.ClientResponseDTO;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.ClientRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.support.SecurityContextTestUtils;
import com.gustavobatista.autoconfig.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private ClientRequestDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new ClientRequestDTO("John", "Doe", "11999999999");
    }

    @Test
    @DisplayName("createClient: persiste quando admin/manager e telefone único")
    void createClient_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            when(clientRepository.existsByPhoneNumber("11999999999")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
                Client c = inv.getArgument(0);
                return new Client(20L, c.getName(), c.getLastName(), c.getPhoneNumber());
            });

            ClientResponseDTO result = clientService.createClient(validDto);

            assertEquals(20L, result.getId());
            assertEquals("John", result.getName());
        }
    }

    @Test
    @DisplayName("createClient: ConflictException quando telefone duplicado")
    void createClient_phoneConflict() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(clientRepository.existsByPhoneNumber("11999999999")).thenReturn(true);

            ConflictException ex = assertThrows(ConflictException.class, () -> clientService.createClient(validDto));
            assertEquals(ErrorCode.CLIENT_PHONE_CONFLICT, ex.getErrorCode());
            verify(clientRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createClient: Forbidden quando seller")
    void createClient_sellerForbidden() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));

            assertThrows(ForbiddenOperationException.class, () -> clientService.createClient(validDto));
        }
    }

    @Test
    @DisplayName("createClient: Unauthorized quando não autenticado")
    void createClient_unauthenticated() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockUnauthenticated()) {
            assertThrows(UnauthorizedException.class, () -> clientService.createClient(validDto));
        }
    }

    @Test
    @DisplayName("updateClient: ResourceNotFound quando cliente inexistente")
    void updateClient_notFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(clientRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clientService.updateClient(1L, validDto));
        }
    }

    @Test
    @DisplayName("deleteClient: remove quando existe")
    void deleteClient_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            Client c = new Client(1L, "John", "Doe", "11999999999");
            when(clientRepository.findById(1L)).thenReturn(Optional.of(c));

            clientService.deleteClient(1L);

            verify(clientRepository).delete(c);
        }
    }

    @Test
    @DisplayName("findClientById: retorna quando cliente existe")
    void findClientById_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(new Client(1L, "John", "Doe", "11999999999")));

            ClientResponseDTO dto = clientService.findClientById(1L);

            assertEquals(1L, dto.getId());
        }
    }

    @Test
    @DisplayName("findAllClients: lista quando autenticado")
    void findAllClients_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.SELLER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.SELLER_EMAIL)).thenReturn(Optional.of(TestFixtures.userSeller()));
            when(clientRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(new Client(1L, "John", "Doe", "11999999999"))));

            assertEquals(1, clientService.findAllClients(PageRequest.of(0, 20)).getContent().size());
        }
    }
}
