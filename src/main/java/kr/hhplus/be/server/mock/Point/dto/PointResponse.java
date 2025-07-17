package kr.hhplus.be.server.mock.Point.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PointResponse(
        @Schema(description = "사용자 ID", example = "1") Long id,
        @Schema(description = "현재 포인트", example = "10000L") Long amount
) {
}
