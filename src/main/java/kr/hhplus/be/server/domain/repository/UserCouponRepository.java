package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.UserCoupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCoupon> findById(Long id);
    List<UserCoupon> findByUserId(Long userId);
    boolean existsByCouponIdAndUserId(Long couponId, Long userId);
    UserCoupon save(UserCoupon userCoupon);
    void deleteAll();
    Optional<UserCoupon> findByCouponIdAndUserId(Long couponId, Long userId);
}
