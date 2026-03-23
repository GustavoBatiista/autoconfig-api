package com.gustavobatista.autoconfig.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gustavobatista.autoconfig.dto.UserRequestDTO;
import com.gustavobatista.autoconfig.dto.UserResponseDTO;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.security.RoleChecks;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ROLE_ADMIN','ROLE_ROLE_MANAGER')")
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User actor = getCurrentUserEntityOrThrow();
        if (userRequestDTO.getRole() == Role.ROLE_ADMIN && !RoleChecks.isAdmin(actor.getRole())) {
            throw new ForbiddenOperationException("Only administrators can create users with role ROLE_ADMIN");
        }

        User user = new User(
                null,
                userRequestDTO.getName(),
                userRequestDTO.getLastName(),
                userRequestDTO.getNickName(),
                userRequestDTO.getEmail(),
                passwordEncoder.encode(userRequestDTO.getPassword()),
                userRequestDTO.getRole()
        );

        User saved = userRepository.save(user);
        log.info("User created: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ROLE_ADMIN','ROLE_ROLE_MANAGER')")
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User actor = getCurrentUserEntityOrThrow();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found: " + id));

        if (user.getRole() == Role.ROLE_ADMIN && !RoleChecks.isAdmin(actor.getRole())) {
            throw new ForbiddenOperationException("Cannot modify administrator users");
        }
        if (userRequestDTO.getRole() == Role.ROLE_ADMIN && !RoleChecks.isAdmin(actor.getRole())) {
            throw new ForbiddenOperationException("Only administrators can assign role ROLE_ADMIN");
        }

        user.setName(userRequestDTO.getName());
        user.setLastName(userRequestDTO.getLastName());
        user.setNickName(userRequestDTO.getNickName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setRole(userRequestDTO.getRole());

        User saved = userRepository.save(user);
        log.info("User updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ROLE_ADMIN','ROLE_ROLE_MANAGER')")
    public void deleteUser(Long id) {
        User actor = getCurrentUserEntityOrThrow();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found: " + id));

        if (user.getRole() == Role.ROLE_ADMIN && !RoleChecks.isAdmin(actor.getRole())) {
            throw new ForbiddenOperationException("Cannot delete administrator users");
        }

        userRepository.delete(user);
        log.info("User deleted: id={}", id);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ROLE_ADMIN','ROLE_ROLE_MANAGER')")
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ROLE_ADMIN','ROLE_ROLE_MANAGER')")
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found: " + id));

        return toResponse(user);
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getNickName(),
                user.getEmail(),
                user.getRole()
        );
    }

    private User getCurrentUserEntityOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }
}