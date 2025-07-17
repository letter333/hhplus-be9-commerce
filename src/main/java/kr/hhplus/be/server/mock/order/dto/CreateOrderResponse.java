package kr.hhplus.be.server.mock.order.dto;

import java.time.ZonedDateTime;

public record CreateOrderResponse(
        Long id,
        Long userCouponId,
        status status,
        Long totalAmount,
        Long discountAmount,
        Long finalAmount,
        String shippingAddress1,
        String shippingAddress2,
        String shippingZipCode,
        String recipientNumber,
        ZonedDateTime createdAt

) {
    public enum status {
        PENDING, PAID, CANCELLED
    }
}
