package kr.hhplus.be.server.infrastructure.repository.jpa.coupon;

import kr.hhplus.be.server.infrastructure.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity, Long> {
    Optional<UserCouponEntity> findByUserId(Long userId);

    boolean existsByCouponIdAndUserId(Long couponId, Long userId);

    Optional<UserCouponEntity> findByCouponIdAndUserId(Long couponId, Long userId);
}
