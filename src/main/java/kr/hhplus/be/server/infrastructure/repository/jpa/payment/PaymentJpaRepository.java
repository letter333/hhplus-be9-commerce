package kr.hhplus.be.server.infrastructure.repository.jpa.payment;

import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}
