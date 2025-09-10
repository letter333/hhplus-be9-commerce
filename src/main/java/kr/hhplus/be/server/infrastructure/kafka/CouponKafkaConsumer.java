package kr.hhplus.be.server.infrastructure.kafka;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.event.CouponIssueEvent;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponKafkaConsumer {
    private final UserCouponRepository userCouponRepository;

    @KafkaListener(
            topics = "coupon-issue",
            groupId = "coupon-issue-group",
            concurrency = "3"
    )
    @Transactional
    public void handleCouponIssueEvent(CouponIssueEvent event) {
        try {
            log.info("[KafkaConsumer]쿠폰 발급 이벤트 수신  Event: {}", event);

            if (userCouponRepository.existsByCouponIdAndUserId(event.couponId(), event.userId())) {
                log.warn("[KafkaConsumer]이미 발급된 쿠폰입니다. userId: {}, couponId: {}",
                        event.userId(), event.couponId());
                return;
            }

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(event.userId())
                    .couponId(event.couponId())
                    .couponCode(event.couponCode())
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(event.expiredAt())
                    .build();

            userCouponRepository.save(userCoupon);

            log.info("[KafkaConsumer]쿠폰 발급 완료 userId: {}, couponId: {}",
                    event.userId(), event.couponId());
        } catch (Exception e) {
            log.error("[KafkaConsumer]쿠폰 발급 처리 중 오류 발생 Event: {}, Error: {}", event, e.getMessage(), e);
            throw e;
        }

    }
}
