package kr.hhplus.be.server.mock.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChargePointResponse(
        @Schema(description = "사용자 ID", example = "1") Long id,
        @Schema(description = "충전 포인트", example = "10000L") Long amount) {
}
