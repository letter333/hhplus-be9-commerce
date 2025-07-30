package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class CouponIssueUseCase {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT_SECONDS = 10L;


    @Transactional
    public UserCoupon execute(CouponIssueCommand command) {
        ReentrantLock lock = lockMap.computeIfAbsent(command.couponId(), k -> new ReentrantLock(true));

        try {
            if(lock.tryLock(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
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
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException("쿠폰 발급이 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("쿠폰 발급 중 문제가 발생했습니다.");
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
