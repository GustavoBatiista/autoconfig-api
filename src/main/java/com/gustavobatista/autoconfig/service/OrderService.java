package com.gustavobatista.autoconfig.service;

import java.util.List;

import com.gustavobatista.autoconfig.dto.OrderRequestDTO;
import com.gustavobatista.autoconfig.dto.OrderResponseDTO;

public interface OrderService {

    public OrderResponseDTO createOrder(OrderRequestDTO dto);

    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO dto);

    public void deleteOrder(Long id);

    public List<OrderResponseDTO> findAllOrders();

    public OrderResponseDTO findOrderById(Long id);
}