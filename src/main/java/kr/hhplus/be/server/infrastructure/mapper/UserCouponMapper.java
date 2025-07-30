package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.infrastructure.entity.UserCouponEntity;

public class UserCouponMapper {
    public static UserCoupon toUserCoupon(UserCouponEntity userCouponEntity) {
        return UserCoupon.builder()
                .id(userCouponEntity.getId())
                .userId(userCouponEntity.getUserId())
                .couponId(userCouponEntity.getCouponId())
                .couponCode(userCouponEntity.getCouponCode())
                .status(userCouponEntity.getStatus())
                .usedAt(userCouponEntity.getUsedAt())
                .expiredAt(userCouponEntity.getExpiredAt())
                .createdAt(userCouponEntity.getCreatedAt())
                .build();
    }

    public static UserCouponEntity toUserCouponEntity(UserCoupon userCoupon) {
        return UserCouponEntity.builder()
                .id(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .couponCode(userCoupon.getCouponCode())
                .status(userCoupon.getStatus())
                .usedAt(userCoupon.getUsedAt())
                .expiredAt(userCoupon.getExpiredAt())
                .build();
    }
}
