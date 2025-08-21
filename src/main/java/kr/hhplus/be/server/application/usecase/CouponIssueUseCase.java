package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponIssueUseCase {
    private final CouponRepository couponRepository;
    private final CouponRedisRepository couponRedisRepository;

    public UserCoupon execute(CouponIssueCommand command) {
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        if(coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 쿠폰입니다.");
        }

        if(couponRedisRepository.isAlreadyIssued(command.couponId(), command.userId())) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }

        Long issuedCount = couponRedisRepository.incrementIssuedCouponCount(command.couponId());

        if(issuedCount > coupon.getQuantity()) {
            couponRedisRepository.removeIssuedUser(command.couponId(), command.userId());
            couponRedisRepository.decrementIssuedCouponCount(command.couponId());
            throw new IllegalArgumentException("쿠폰이 모두 소진되었습니다.");
        }

        String couponCode = generateCouponCode();

        couponRedisRepository.addCouponIssueRequestToQueue(
                command.couponId(),
                command.userId(),
                couponCode,
                coupon.getExpiredAt()
        );

        return UserCoupon.builder()
                .couponId(command.couponId())
                .userId(command.userId())
                .couponCode(couponCode)
                .status(UserCouponStatus.ISSUED)
                .expiredAt(coupon.getExpiredAt())
                .build();
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
