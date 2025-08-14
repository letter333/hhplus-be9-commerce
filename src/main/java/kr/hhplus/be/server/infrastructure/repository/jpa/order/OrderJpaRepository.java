package kr.hhplus.be.server.infrastructure.repository.jpa.order;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.infrastructure.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> findByIdWithLock(@Param("id") Long id);

}
