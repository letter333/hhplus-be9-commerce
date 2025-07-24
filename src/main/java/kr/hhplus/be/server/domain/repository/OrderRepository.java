package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    Order save(Order order);
}
