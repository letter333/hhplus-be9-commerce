package kr.hhplus.be.server.interfaces.dto.response;

import kr.hhplus.be.server.domain.model.PaymentMethod;
import kr.hhplus.be.server.domain.model.PaymentStatus;

public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod paymentMethod,
        Long amount,
        PaymentStatus status
) {
}
