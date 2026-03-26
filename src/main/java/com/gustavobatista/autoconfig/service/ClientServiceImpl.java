package com.gustavobatista.autoconfig.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.ClientRequestDTO;
import com.gustavobatista.autoconfig.dto.ClientResponseDTO;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.exception.ConflictException;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.ForbiddenOperationException;
import com.gustavobatista.autoconfig.exception.ResourceNotFoundException;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.ClientRepository;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.security.RoleChecks;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ClientResponseDTO createClient(ClientRequestDTO dto) {
        assertAdminOrManager();

        String name = trim(dto.getName());
        String lastName = trim(dto.getLastName());
        String phoneNumber = trim(dto.getPhoneNumber());

        assertUniquePhone(phoneNumber, null);

        Client saved = clientRepository.save(new Client(null, name, lastName, phoneNumber));
        log.info("Client created: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public ClientResponseDTO updateClient(Long id, ClientRequestDTO dto) {
        assertAdminOrManager();

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + id));

        String name = trim(dto.getName());
        String lastName = trim(dto.getLastName());
        String phoneNumber = trim(dto.getPhoneNumber());

        assertUniquePhone(phoneNumber, id);

        client.setName(name);
        client.setLastName(lastName);
        client.setPhoneNumber(phoneNumber);

        Client saved = clientRepository.save(client);
        log.info("Client updated: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteClient(Long id) {
        assertAdminOrManager();

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + id));

        clientRepository.delete(client);
        log.info("Client deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> findAllClients(Pageable pageable) {
        assertAuthenticated();

        return clientRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO findClientById(Long id) {
        assertAuthenticated();

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client not found: " + id));

        return toResponse(client);
    }

    private void assertUniquePhone(String phoneNumber, Long excludeId) {
        boolean duplicate = excludeId == null
                ? clientRepository.existsByPhoneNumber(phoneNumber)
                : clientRepository.existsByPhoneNumberAndIdNot(phoneNumber, excludeId);

        if (duplicate) {
            throw new ConflictException(
                    ErrorCode.CLIENT_PHONE_CONFLICT,
                    "A client with this phone number already exists");
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private ClientResponseDTO toResponse(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getLastName(),
                client.getPhoneNumber());
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