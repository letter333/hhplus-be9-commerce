package kr.hhplus.be.server.infrastructure.repository.jpa.payment;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;
import kr.hhplus.be.server.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity = PaymentMapper.toPaymentEntity(payment);
        PaymentEntity savedEntity = paymentJpaRepository.save(paymentEntity);

        return PaymentMapper.toPayment(savedEntity);
    }
}
