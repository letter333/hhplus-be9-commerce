package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;

public class PaymentMapper {
    public static Payment toPayment(PaymentEntity paymentEntity) {
        return Payment.builder()
                .id(paymentEntity.getId())
                .orderId(paymentEntity.getOrderId())
                .paymentMethod(paymentEntity.getPaymentMethod())
                .amount(paymentEntity.getAmount())
                .status(paymentEntity.getStatus())
                .build();
    }

    public static PaymentEntity toPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();
    }
}
