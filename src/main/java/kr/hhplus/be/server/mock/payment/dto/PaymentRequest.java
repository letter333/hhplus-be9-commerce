package kr.hhplus.be.server.mock.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentRequest(
        @Schema(description = "주문 ID", example = "1")
        Long orderId,
        @Schema(description = "결제 방법", example = "POINT")
        method method
) {
    public enum method {
        CARD, POINT
    }
}
