package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Payment {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private Long amount;
    private PaymentStatus status;

    @Builder
    public Payment(Long id, Long orderId, PaymentMethod paymentMethod, Long amount, PaymentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }
}
