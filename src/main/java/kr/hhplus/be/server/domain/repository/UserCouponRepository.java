package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.UserCoupon;

import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCoupon> findById(Long id);
    Optional<UserCoupon> findByUserId(Long userId);
    boolean existsByCouponIdAndUserId(Long couponId, Long userId);
    UserCoupon save(UserCoupon userCoupon);
}
