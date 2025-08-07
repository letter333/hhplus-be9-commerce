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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponIssueUseCase {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;


    @Transactional
    public UserCoupon execute(CouponIssueCommand command) {
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(command.couponId())
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
