package com.gustavobatista.autoconfig.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;

public interface OrderService {

    public OrderResponseDTO createOrder(OrderRequestDTO dto);

    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO dto);

    public void deleteOrder(Long id);

    public Page<OrderResponseDTO> findAllOrders(Pageable pageable);

    public OrderResponseDTO findOrderById(Long id);
}