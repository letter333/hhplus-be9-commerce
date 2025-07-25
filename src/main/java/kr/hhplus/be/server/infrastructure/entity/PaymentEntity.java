package kr.hhplus.be.server.infrastructure.entity;

import kr.hhplus.be.server.domain.model.PaymentMethod;
import kr.hhplus.be.server.domain.model.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentEntity extends BaseEntity {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private Long amount;
    private PaymentStatus status;

    @Builder
    public PaymentEntity(Long id, Long orderId, PaymentMethod paymentMethod, Long amount, PaymentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }
}
