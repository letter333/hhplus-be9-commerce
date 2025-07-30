package kr.hhplus.be.server.infrastructure.entity;

import kr.hhplus.be.server.domain.model.UserCouponStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class UserCouponEntity extends BaseEntity{
    private Long id;
    private Long userId;
    private Long couponId;
    String couponCode;
    UserCouponStatus status;
    ZonedDateTime usedAt;
    ZonedDateTime expiredAt;

    @Builder
    public UserCouponEntity(Long id, Long userId, Long couponId, String couponCode, UserCouponStatus status, ZonedDateTime usedAt, ZonedDateTime expiredAt) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.status = status;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }
}
