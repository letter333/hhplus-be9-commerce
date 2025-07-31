package kr.hhplus.be.server.infrastructure.repository.jpa.product;

import kr.hhplus.be.server.infrastructure.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductJpaRepository extends JpaRepository<OrderProductEntity, Long> {
    List<OrderProductEntity> findByOrderId(Long orderId);

    List<OrderProductEntity> findByOrderIdIn(List<Long> orderIds);
}
