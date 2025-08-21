package kr.hhplus.be.server.application.scheduler;

import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.infrastructure.repository.redis.CouponIssueQueuePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserCouponRepository userCouponRepository;
    private static final String COUPON_ISSUE_QUEUE_KEY = "coupon:issue_queue";
    private static final String FAILED_QUEUE_KEY = COUPON_ISSUE_QUEUE_KEY + ":failed";
    private static final int MAX_RETRY_COUNT = 3;


    @Scheduled(fixedRate = 100)
    @Transactional
    public void processCouponIssueQueue() {
        CouponIssueQueuePayload payload = (CouponIssueQueuePayload) redisTemplate.opsForList().rightPop(COUPON_ISSUE_QUEUE_KEY);

        if (payload == null) {
            return;
        }

        try {
            log.info("Process coupon issue request from queue: {}", payload);

            if (userCouponRepository.existsByCouponIdAndUserId(payload.couponId(), payload.userId())) {
                log.warn("이미 발급된 쿠폰입니다. userId: {}, couponId: {}", payload.userId(), payload.couponId());
                return;
            }

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(payload.userId())
                    .couponId(payload.couponId())
                    .couponCode(payload.couponCode())
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(payload.expiredAt())
                    .build();

            userCouponRepository.save(userCoupon);

        } catch (Exception e) {
            log.error("쿠폰 발급 처리 중 오류 발생: {}", payload, e);
            handleRetry(payload);
        }
    }

    private void handleRetry(CouponIssueQueuePayload payload) {
        if (payload.retryCount() < MAX_RETRY_COUNT) {
            int nextRetryCount = payload.retryCount() + 1;
            CouponIssueQueuePayload newPayload = new CouponIssueQueuePayload(
                    payload.couponId(),
                    payload.userId(),
                    payload.couponCode(),
                    payload.expiredAt(),
                    nextRetryCount
            );
            log.info("재시도를 위해 큐에 다시 추가 (시도 횟수: {})", nextRetryCount);
            redisTemplate.opsForList().leftPush(COUPON_ISSUE_QUEUE_KEY, newPayload);
        } else {
            log.error("최대 재시도 횟수({})를 초과", MAX_RETRY_COUNT);
            moveToDeadLetterQueue(payload, "최대 재시도 횟수 초과");
        }
    }

    private void moveToDeadLetterQueue(CouponIssueQueuePayload payload, String errorMessage) {
        redisTemplate.opsForList().leftPush(FAILED_QUEUE_KEY, payload);
    }
}
