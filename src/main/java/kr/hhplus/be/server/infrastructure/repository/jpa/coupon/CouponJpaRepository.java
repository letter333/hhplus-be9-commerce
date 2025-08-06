package kr.hhplus.be.server.infrastructure.repository.jpa.coupon;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id = :id")
    Optional<CouponEntity> findByIdWithPessimisticLock(@Param("id") Long id);
}
