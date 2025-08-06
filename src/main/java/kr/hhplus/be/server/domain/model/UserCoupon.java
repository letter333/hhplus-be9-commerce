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

    public Long calculateDiscount(CouponType type, Long discountAmount, Long totalPrice) {
        if(this.status == UserCouponStatus.EXPIRED) {
            throw new IllegalStateException("사용기간이 만료된 쿠폰입니다.");
        }

        if(this.status == UserCouponStatus.USED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        if(type == CouponType.PERCENTAGE) {
            return Math.max(0L, totalPrice - (totalPrice * discountAmount / 100));
        } else {
            return Math.max(0L, totalPrice - discountAmount);
        }
    }

    public void use() {
        this.status = UserCouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void restore() {
        this.status = UserCouponStatus.ISSUED;
        this.usedAt = null;
    }
}
