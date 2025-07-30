package kr.hhplus.be.server.interfaces.dto.response;

import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.OrderStatus;

public record OrderResponse(
        Long id,
        OrderStatus status,
        Long totalPrice,
        Long discountAmount,
        Long finalPrice,
        Address address,
        String recipientNumber
) {
}
