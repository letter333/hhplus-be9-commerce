package kr.hhplus.be.server.infrastructure.repository.jpa.coupon;

import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.infrastructure.entity.UserCouponEntity;
import kr.hhplus.be.server.infrastructure.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {
    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return userCouponJpaRepository.findById(id)
                .map(UserCouponMapper::toUserCoupon);
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return userCouponJpaRepository.findByUserId(userId)
                .map(UserCouponMapper::toUserCoupon)
                .stream().toList();
    }

    @Override
    public boolean existsByCouponIdAndUserId(Long couponId, Long userId) {
        return userCouponJpaRepository.existsByCouponIdAndUserId(couponId, userId);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        UserCouponEntity userCouponEntity = UserCouponMapper.toUserCouponEntity(userCoupon);
        UserCouponEntity savedEntity = userCouponJpaRepository.save(userCouponEntity);

        return UserCouponMapper.toUserCoupon(savedEntity);
    }

    @Override
    public void deleteAll() {
        userCouponJpaRepository.deleteAll();
    }

    @Override
    public Optional<UserCoupon> findByCouponIdAndUserId(Long couponId, Long userId) {
        return userCouponJpaRepository.findByCouponIdAndUserId(couponId, userId)
                .map(UserCouponMapper::toUserCoupon);
    }
}
