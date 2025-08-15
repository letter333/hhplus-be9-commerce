package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponIssueUseCase {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final TransactionTemplate transactionTemplate;
    private final RedissonLockManager redissonLockManager;


    public UserCoupon execute(CouponIssueCommand command) {
        String lockKey = String.format("couponIssueLock:%s", command.couponId());
        RLock lock = redissonLockManager.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if(!isLocked) {
                throw new IllegalArgumentException("쿠폰 발급을 위한 락을 획득하지 못했습니다.");
            }

            return transactionTemplate.execute(status -> {
                Coupon coupon = couponRepository.findById(command.couponId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

                validateDuplicateIssue(command.couponId(), command.userId());

                coupon.issue();

                String couponCode = generateCouponCode();

                UserCoupon userCoupon = UserCoupon.builder()
                        .userId(command.userId())
                        .couponId(command.couponId())
                        .couponCode(couponCode)
                        .status(UserCouponStatus.ISSUED)
                        .expiredAt(coupon.getExpiredAt())
                        .build();

                couponRepository.save(coupon);

                return userCouponRepository.save(userCoupon);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("쿠폰 발급 중 오류가 발생했습니다.", e.getCause());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void validateDuplicateIssue(Long couponId, Long userId) {
        if(userCouponRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }
    }

    private String generateCouponCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        return String.format("%s-%s-%s-%s",
                uuid.substring(0, 4),
                uuid.substring(4, 8),
                uuid.substring(8, 12),
                uuid.substring(12, 16)
        );
    }
}
