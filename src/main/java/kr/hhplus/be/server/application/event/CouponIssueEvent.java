package kr.hhplus.be.server.application.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CouponIssueEvent(
        Long couponId,
        Long userId,
        String couponCode,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expiredAt
) {
}
