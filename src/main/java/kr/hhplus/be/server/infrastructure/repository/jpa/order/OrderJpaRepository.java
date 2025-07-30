package kr.hhplus.be.server.infrastructure.repository.jpa.order;

import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.infrastructure.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);
}
