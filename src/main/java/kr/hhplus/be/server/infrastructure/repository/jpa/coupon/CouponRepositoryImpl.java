package kr.hhplus.be.server.infrastructure.repository.jpa.coupon;

import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.infrastructure.entity.CouponEntity;
import kr.hhplus.be.server.infrastructure.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<Coupon> findById(Long id) {
        return couponJpaRepository.findById(id)
                .map(CouponMapper::toCoupon);
    }

    @Override
    public Optional<Coupon> findByIdWithLock(Long id) {
        return couponJpaRepository.findByIdWithPessimisticLock(id)
                .map(CouponMapper::toCoupon);
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity couponEntity = CouponMapper.toCouponEntity(coupon);
        CouponEntity savedEntity = couponJpaRepository.save(couponEntity);

        return CouponMapper.toCoupon(savedEntity);
    }

    @Override
    public void deleteAll() {
        couponJpaRepository.deleteAll();
    }
}
