package kr.hhplus.be.server.interfaces.mapper;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.interfaces.dto.response.PaymentResponse;

public class PaymentResponseMapper {
    public static PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getStatus()
        );
    }
}
