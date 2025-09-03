package kr.hhplus.be.server.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Payment(@JsonProperty("id") Long id,
                   @JsonProperty("orderId") Long orderId,
                   @JsonProperty("paymentMethod") PaymentMethod paymentMethod,
                   @JsonProperty("amount") Long amount,
                   @JsonProperty("status") PaymentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }
}
