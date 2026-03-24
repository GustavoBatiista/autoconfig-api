package com.gustavobatista.autoconfig.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gustavobatista.autoconfig.dto.UserRequestDTO;
import com.gustavobatista.autoconfig.dto.UserResponseDTO;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.support.SecurityContextTestUtils;
import com.gustavobatista.autoconfig.support.TestFixtures;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("createUser: persiste quando admin cria usuário com ROLE_ADMIN")
    void createUser_adminCreatesAdmin_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            UserRequestDTO dto = new UserRequestDTO("New", "User", "newuser", "new@test.com", "password12", Role.ROLE_ADMIN);
            when(passwordEncoder.encode("password12")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return new User(50L, u.getName(), u.getLastName(), u.getNickName(), u.getEmail(), u.getPassword(), u.getRole());
            });

            UserResponseDTO result = userService.createUser(dto);

            assertEquals(50L, result.getId());
            assertEquals(Role.ROLE_ADMIN, result.getRole());
            verify(passwordEncoder).encode("password12");
        }
    }

    @Test
    @DisplayName("createUser: Forbidden quando manager tenta criar usuário ROLE_ADMIN")
    void createUser_managerCannotCreateAdmin() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            UserRequestDTO dto = new UserRequestDTO("New", "User", "newuser", "new@test.com", "password12", Role.ROLE_ADMIN);

            ForbiddenOperationException ex = assertThrows(ForbiddenOperationException.class, () -> userService.createUser(dto));
            assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("createUser: manager pode criar usuário não-ADMIN")
    void createUser_managerCreatesSeller_happyPath() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            UserRequestDTO dto = new UserRequestDTO("Sel", "User", "seller1", "sel@test.com", "password12", Role.ROLE_SELLER);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return new User(51L, u.getName(), u.getLastName(), u.getNickName(), u.getEmail(), u.getPassword(), u.getRole());
            });

            UserResponseDTO result = userService.createUser(dto);

            assertEquals(Role.ROLE_SELLER, result.getRole());
        }
    }

    @Test
    @DisplayName("createUser: Unauthorized quando token sem usuário no repositório")
    void createUser_userNotInRepository() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.empty());
            UserRequestDTO dto = new UserRequestDTO("A", "B", "c", "x@test.com", "password12", Role.ROLE_SELLER);

            assertThrows(UnauthorizedException.class, () -> userService.createUser(dto));
        }
    }

    @Test
    @DisplayName("updateUser: Forbidden quando manager altera usuário ADMIN")
    void updateUser_managerCannotEditAdmin() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            User targetAdmin = new User(10L, "A", "B", "adm", "admin2@test.com", "hash", Role.ROLE_ADMIN);
            when(userRepository.findById(10L)).thenReturn(Optional.of(targetAdmin));
            UserRequestDTO dto = new UserRequestDTO("A", "B", "c", "admin2@test.com", "password12", Role.ROLE_ADMIN);

            assertThrows(ForbiddenOperationException.class, () -> userService.updateUser(10L, dto));
        }
    }

    @Test
    @DisplayName("updateUser: Forbidden quando manager promove para ROLE_ADMIN")
    void updateUser_managerCannotPromoteToAdmin() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            User target = new User(10L, "A", "B", "sel", "sel2@test.com", "hash", Role.ROLE_SELLER);
            when(userRepository.findById(10L)).thenReturn(Optional.of(target));
            UserRequestDTO dto = new UserRequestDTO("A", "B", "c", "sel2@test.com", "password12", Role.ROLE_ADMIN);

            assertThrows(ForbiddenOperationException.class, () -> userService.updateUser(10L, dto));
        }
    }

    @Test
    @DisplayName("updateUser: ResourceNotFound quando id inexistente")
    void updateUser_notFound() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.ADMIN_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.ADMIN_EMAIL)).thenReturn(Optional.of(TestFixtures.userAdmin()));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            UserRequestDTO dto = new UserRequestDTO("A", "B", "c", "x@test.com", "password12", Role.ROLE_SELLER);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUser(999L, dto));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("deleteUser: Forbidden quando manager tenta excluir ADMIN")
    void deleteUser_managerCannotDeleteAdmin() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockAuthenticatedUser(TestFixtures.MANAGER_EMAIL)) {
            when(userRepository.findByEmail(TestFixtures.MANAGER_EMAIL)).thenReturn(Optional.of(TestFixtures.userManager()));
            User targetAdmin = new User(10L, "A", "B", "adm", "admin2@test.com", "hash", Role.ROLE_ADMIN);
            when(userRepository.findById(10L)).thenReturn(Optional.of(targetAdmin));

            assertThrows(ForbiddenOperationException.class, () -> userService.deleteUser(10L));
        }
    }

    @Test
    @DisplayName("findById: ResourceNotFound quando usuário inexistente")
    void findById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.findById(1L));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("findAll: retorna lista do repositório")
    void findAll_happyPath() {
        when(userRepository.findAll()).thenReturn(List.of(TestFixtures.userAdmin()));

        List<UserResponseDTO> all = userService.findAll();

        assertEquals(1, all.size());
    }

    @Test
    @DisplayName("getCurrentUser: Unauthorized quando não autenticado (update)")
    void updateUser_unauthenticated() {
        try (MockedStatic<SecurityContextHolder> ctx = SecurityContextTestUtils.mockUnauthenticated()) {
            UserRequestDTO dto = new UserRequestDTO("A", "B", "c", "x@test.com", "password12", Role.ROLE_SELLER);

            assertThrows(UnauthorizedException.class, () -> userService.updateUser(1L, dto));
        }
    }
}
