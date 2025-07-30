package kr.hhplus.be.server.application.usecase.dto.command;

import lombok.Getter;

public record CouponIssueCommand(
        Long couponId,
        Long userId
) {
}
