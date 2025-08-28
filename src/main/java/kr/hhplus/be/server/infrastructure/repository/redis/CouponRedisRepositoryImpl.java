package kr.hhplus.be.server.infrastructure.repository.redis;

import kr.hhplus.be.server.domain.repository.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepositoryImpl implements CouponRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_COUNT_KEY_PREFIX = "coupon:count:";
    private static final String COUPON_ISSUE_QUEUE_KEY = "coupon:issue_queue";
    private static final String COUPON_ISSUED_USERS_KEY_PREFIX = "coupon:issued_users:";

    @Override
    public Long incrementIssuedCouponCount(Long couponId) {
        String key = COUPON_COUNT_KEY_PREFIX + couponId;

        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long decrementIssuedCouponCount(Long couponId) {
        String key = COUPON_COUNT_KEY_PREFIX + couponId;
        return redisTemplate.opsForValue().decrement(key);
    }

    @Override
    public void addCouponIssueRequestToQueue(Long couponId, Long userId, String couponCode, LocalDateTime expiredAt) {
        CouponIssueQueuePayload payload = new CouponIssueQueuePayload(couponId, userId, couponCode, expiredAt, 0);
        redisTemplate.opsForList().leftPush(COUPON_ISSUE_QUEUE_KEY, payload);
    }

    @Override
    public Boolean isAlreadyIssued(Long couponId, Long userId) {
        String key = COUPON_ISSUED_USERS_KEY_PREFIX + couponId;
        Long addedCount = redisTemplate.opsForSet().add(key, String.valueOf(userId));

        return addedCount != null && addedCount == 0;
    }

    @Override
    public void removeIssuedUser(Long couponId, Long userId) {
        String key = COUPON_ISSUED_USERS_KEY_PREFIX + couponId;
        redisTemplate.opsForSet().remove(key, String.valueOf(userId));
    }

    @Override
    public Long getIssuedCouponCount(Long couponId) {
        String key = COUPON_COUNT_KEY_PREFIX + couponId;
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return Long.parseLong(value.toString());
    }

    @Override
    public void deleteAll() {
        Set<String> keys = redisTemplate.keys("coupon:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete(COUPON_ISSUE_QUEUE_KEY);
    }
}
