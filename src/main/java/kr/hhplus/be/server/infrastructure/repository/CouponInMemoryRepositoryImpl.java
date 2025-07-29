package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.CouponType;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.infrastructure.entity.CouponEntity;
import kr.hhplus.be.server.infrastructure.mapper.CouponMapper;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CouponInMemoryRepositoryImpl implements CouponRepository {
    private final Map<Long, CouponEntity> table = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public CouponInMemoryRepositoryImpl() {
        initTable();
    }

    public void initTable() {
        table.put(1L, CouponEntity.builder()
                .id(1L)
                .name("쿠폰1")
                .type(CouponType.PERCENTAGE)
                .discountPercentage(10L)
                .quantity(1000)
                .expiredAt(ZonedDateTime.of(2025, 8, 31, 23, 59, 59, 0, ZoneId.of("Asia/Seoul")))
                .build());
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
        } else {
            table.put(couponEntity.getId(), couponEntity);
            savedEntity = couponEntity;
        }

        return CouponMapper.toCoupon(savedEntity);
    }
}
