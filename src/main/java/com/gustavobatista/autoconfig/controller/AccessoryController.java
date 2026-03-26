package com.gustavobatista.autoconfig.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gustavobatista.autoconfig.dto.AccessoryRequestDTO;
import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;
import com.gustavobatista.autoconfig.service.AccessoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/accessories")
@Tag(name = "7 - Accessories", description = "Endpoints for accessory management")
public class AccessoryController {

    private final AccessoryService accessoryService;

    public AccessoryController(AccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }

    @PostMapping
    public ResponseEntity<AccessoryResponseDTO> create(@Valid @RequestBody AccessoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessoryService.createAccessory(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccessoryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AccessoryRequestDTO dto) {
        return ResponseEntity.ok(accessoryService.updateAccessory(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accessoryService.deleteAccessory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<AccessoryResponseDTO>> findAll(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accessoryService.findAllAccessories(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessoryResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(accessoryService.findAccessoryById(id));
    }
}