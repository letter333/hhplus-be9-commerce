package kr.hhplus.be.server.mock.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CreateOrderRequest(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "주문 상품 목록")
        List<OrderProductRequest> orderProductList,
        @Schema(description = "사용 쿠폰", example = "1")
        Long userCouponId,
        @Schema(description = "기본 주소", example = "기본 주소")
        String shippingAddress1,
        @Schema(description = "상세 주소", example = "상세 주소")
        String shippingAddress2,
        @Schema(description = "우편 번호", example = "우편 번호")
        String shippingZipCode
) {
}
