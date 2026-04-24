package com.gustavobatista.autoconfig.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gustavobatista.autoconfig.dto.ConfirmVehicleDTO;
import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;
import com.gustavobatista.autoconfig.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
@Tag(name = "4 - Orders", description = "Endpoints for order management")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> update(@PathVariable Long id, @Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.ok(orderService.updateOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> findAll(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    @PatchMapping("/{id}/confirm-vehicle")
    public ResponseEntity<OrderResponseDTO> confirmVehicle(@PathVariable Long id, @Valid @RequestBody ConfirmVehicleDTO dto) {
        return ResponseEntity.ok(orderService.confirmVehicle(id, dto));
    }

    @PatchMapping("/{id}/confirm-accessories")
    public ResponseEntity<OrderResponseDTO> confirmAccessories(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmAccessories(id));
    }

    @PatchMapping("/{id}/confirm-inspection")
    public ResponseEntity<OrderResponseDTO> confirmInspection(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmInspection(id));
    }

    @PatchMapping("/{id}/confirm-installation")
    public ResponseEntity<OrderResponseDTO> confirmInstallation(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmInstallation(id));
    }
}