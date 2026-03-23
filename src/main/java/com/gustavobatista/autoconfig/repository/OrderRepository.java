package com.gustavobatista.autoconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
