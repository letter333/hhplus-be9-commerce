package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.CouponType;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.infrastructure.entity.CouponEntity;
import kr.hhplus.be.server.infrastructure.mapper.CouponMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public class CouponInMemoryRepositoryImpl implements CouponRepository {
    private final Map<Long, CouponEntity> table = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private static final Long LOCK_TIMEOUT_SECONDS = 10L;

    public CouponInMemoryRepositoryImpl() {
        initTable();
    }

    public void initTable() {
        CouponEntity couponEntity = CouponEntity.builder()
                .id(1L)
                .name("쿠폰1")
                .type(CouponType.PERCENTAGE)
                .discountPercentage(10L)
                .quantity(1000)
                .expiredAt(LocalDateTime.of(2025, 8, 31, 23, 59, 59, 0))
                .build();
        table.put(1L, couponEntity);
        lockMap.put(1L, new ReentrantLock(true));
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return Optional.ofNullable(table.get(id))
                .map(CouponMapper::toCoupon);

    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity couponEntity = CouponMapper.toCouponEntity(coupon);
        CouponEntity savedEntity;

        if(couponEntity.getId() == null || table.get(couponEntity.getId()) == null) {
            Long newId = idGenerator.incrementAndGet();
            savedEntity = CouponEntity.builder()
                    .id(newId)
                    .name(couponEntity.getName())
                    .type(couponEntity.getType())
                    .discountAmount(couponEntity.getDiscountAmount())
                    .discountPercentage(couponEntity.getDiscountPercentage())
                    .quantity(couponEntity.getQuantity())
                    .issuedQuantity(couponEntity.getIssuedQuantity())
                    .expiredAt(couponEntity.getExpiredAt())
                    .build();

            table.put(newId, savedEntity);
            lockMap.put(newId, new ReentrantLock(true));
        } else {
            ReentrantLock lock = lockMap.computeIfAbsent(couponEntity.getId(), k -> new ReentrantLock(true));

            try {
                if (lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    try {
                        table.put(couponEntity.getId(), couponEntity);
                        savedEntity = couponEntity;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    throw new IllegalStateException("쿠폰 저장 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("쿠폰 저장 중 문제가 발생했습니다.", e);
            }

        }

        return CouponMapper.toCoupon(savedEntity);
    }
}
