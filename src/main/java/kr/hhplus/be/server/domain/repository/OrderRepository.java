package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    Optional<Order> findByIdWithLock(Long id);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);
    Order save(Order order);
    List<Order> saveAll(List<Order> orders);
    void deleteAll();
    List<Order> findAll();
}
