package kr.hhplus.be.server.mock.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChargePointRequest(
        @Schema(description = "충전 포인트", defaultValue = "10000", example = "10000L") Long amount) {
}
