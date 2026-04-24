package com.gustavobatista.autoconfig.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.ConfirmVehicleDTO;
import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO dto);

    OrderResponseDTO updateOrder(Long id, OrderRequestDTO dto);

    void deleteOrder(Long id);

    Page<OrderResponseDTO> findAllOrders(Pageable pageable);

    OrderResponseDTO findOrderById(Long id);

    OrderResponseDTO confirmVehicle(Long orderId, ConfirmVehicleDTO dto);

    OrderResponseDTO confirmAccessories(Long orderId);

    OrderResponseDTO confirmInspection(Long orderId);

    OrderResponseDTO confirmInstallation(Long orderId);
}
