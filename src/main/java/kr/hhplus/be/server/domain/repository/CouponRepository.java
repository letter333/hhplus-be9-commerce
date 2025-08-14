package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findById(Long id);
    Optional<Coupon> findByIdWithPessimisticLock(Long id);
    Coupon save(Coupon coupon);
    void deleteAll();
}
