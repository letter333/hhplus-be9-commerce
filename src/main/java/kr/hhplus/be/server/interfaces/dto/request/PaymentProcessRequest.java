package kr.hhplus.be.server.interfaces.dto.request;

import kr.hhplus.be.server.domain.model.PaymentMethod;

public record PaymentProcessRequest(
        Long orderId,
        PaymentMethod paymentMethod
) {
}
