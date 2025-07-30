package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class UserCoupon {
    Long id;
    Long userId;
    Long couponId;
    String couponCode;
    UserCouponStatus status;
    ZonedDateTime usedAt;
    ZonedDateTime expiredAt;
    ZonedDateTime createdAt;

    @Builder
    public UserCoupon(Long id, Long userId, Long couponId, String couponCode, UserCouponStatus status, ZonedDateTime usedAt, ZonedDateTime expiredAt, ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.status = status;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }
}
