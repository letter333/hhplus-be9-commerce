package kr.hhplus.be.server.mock.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IssueCouponRequest(
        @Schema(description = "쿠폰 ID", example = "3") Long id
) {
}
