package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.infrastructure.entity.UserCouponEntity;
import kr.hhplus.be.server.infrastructure.mapper.UserCouponMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserCouponInMemoryRepositoryImpl implements UserCouponRepository {
    private final Map<Long, UserCouponEntity> table = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return Optional.ofNullable(table.get(id))
                .map(UserCouponMapper::toUserCoupon );
    }

    @Override
    public Optional<UserCoupon> findByUserId(Long userId) {
        return table.values().stream()
                .filter(userCouponEntity -> userCouponEntity.getUserId().equals(userId))
                .findFirst()
                .map(UserCouponMapper::toUserCoupon );
    }

    @Override
    public boolean existsByCouponIdAndUserId(Long couponId, Long userId) {
        return false;
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        UserCouponEntity userCouponEntity = UserCouponMapper.toUserCouponEntity(userCoupon);
        UserCouponEntity savedEntity;

        if (userCouponEntity.getId() == null || table.get(userCouponEntity.getId()) == null) {
            Long newId = idGenerator.incrementAndGet();
            savedEntity = UserCouponEntity.builder()
                    .id(newId)
                    .userId(userCouponEntity.getUserId())
                    .couponId(userCouponEntity.getCouponId())
                    .couponCode(userCouponEntity.getCouponCode())
                    .status(userCouponEntity.getStatus())
                    .usedAt(userCouponEntity.getUsedAt())
                    .expiredAt(userCouponEntity.getExpiredAt())
                    .build();

            table.put(newId, savedEntity);
        } else {
            table.put(userCouponEntity.getId(), userCouponEntity);
            savedEntity = userCouponEntity;
        }

        return UserCouponMapper.toUserCoupon(savedEntity);
    }
}
