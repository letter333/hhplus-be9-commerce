package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;
import kr.hhplus.be.server.infrastructure.mapper.PaymentMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PaymentInMemoryRepositoryImpl implements PaymentRepository {
    private final Map<Long, PaymentEntity> table = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity = PaymentMapper.toPaymentEntity(payment);
        PaymentEntity savedEntity;

        if(paymentEntity.getId() == null || table.get(paymentEntity.getId()) == null) {
            Long newId = idGenerator.incrementAndGet();
            savedEntity = PaymentEntity.builder()
                    .id(newId)
                    .orderId(paymentEntity.getOrderId())
                    .paymentMethod(paymentEntity.getPaymentMethod())
                    .amount(paymentEntity.getAmount())
                    .status(paymentEntity.getStatus())
                    .build();

            table.put(newId, savedEntity);
        } else {
            table.put(paymentEntity.getId(), paymentEntity);
            savedEntity = paymentEntity;
        }

        return PaymentMapper.toPayment(savedEntity);
    }
}
