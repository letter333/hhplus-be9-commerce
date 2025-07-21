package kr.hhplus.be.server.mock.payment.dto;

public record PaymentResponse(
        Long id,
        Long orderId,
        method method,
        Long amount,
        status status
) {
    public enum method {
        CARD, POINT
    }

    public enum status {
        SUCCESS, FAIL
    }
}
