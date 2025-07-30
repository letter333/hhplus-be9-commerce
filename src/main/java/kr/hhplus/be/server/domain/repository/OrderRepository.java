package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, ZonedDateTime dateTime);
    Order save(Order order);
    List<Order> saveAll(List<Order> orders);
}
