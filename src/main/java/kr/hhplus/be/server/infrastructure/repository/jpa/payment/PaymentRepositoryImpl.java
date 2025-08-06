package kr.hhplus.be.server.infrastructure.repository.jpa.payment;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;
import kr.hhplus.be.server.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<Payment> saveAll(List<Payment> payments) {
        List<PaymentEntity> paymentEntities = payments.stream()
                .map(PaymentMapper::toPaymentEntity)
                .toList();
        List<PaymentEntity> savedEntities = paymentJpaRepository.saveAll(paymentEntities);
        return savedEntities.stream()
                .map(PaymentMapper::toPayment)
                .toList();
    }
}
