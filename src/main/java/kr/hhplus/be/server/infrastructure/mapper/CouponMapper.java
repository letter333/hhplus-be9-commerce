package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.infrastructure.entity.CouponEntity;

public class CouponMapper {
    public static Coupon toCoupon(CouponEntity couponEntity) {
        return Coupon.builder()
                .id(couponEntity.getId())
                .name(couponEntity.getName())
                .type(couponEntity.getType())
                .discountAmount(couponEntity.getDiscountAmount())
                .discountPercentage(couponEntity.getDiscountPercentage())
                .quantity(couponEntity.getQuantity())
                .issuedQuantity(couponEntity.getIssuedQuantity())
                .expiredAt(couponEntity.getExpiredAt())
                .build();
    }

    public static CouponEntity toCouponEntity(Coupon coupon) {
        return CouponEntity.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .type(coupon.getType())
                .discountAmount(coupon.getDiscountAmount())
                .discountPercentage(coupon.getDiscountPercentage())
                .quantity(coupon.getQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .expiredAt(coupon.getExpiredAt())
                .build();
    }
}
