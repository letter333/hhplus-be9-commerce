package kr.hhplus.be.server.mock.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IssueCouponRequest(
        @Schema(description = "사용자 ID", example = "1") Long userId
) {
}
