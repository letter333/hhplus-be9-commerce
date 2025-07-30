package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCoupon {
    Long id;
    Long userId;
    Long couponId;
    String couponCode;
    UserCouponStatus status;
    LocalDateTime usedAt;
    LocalDateTime expiredAt;
    LocalDateTime createdAt;

    @Builder
    public UserCoupon(Long id, Long userId, Long couponId, String couponCode, UserCouponStatus status, LocalDateTime usedAt, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.status = status;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }
}
