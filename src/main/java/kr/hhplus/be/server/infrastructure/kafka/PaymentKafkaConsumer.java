package kr.hhplus.be.server.infrastructure.kafka;

import kr.hhplus.be.server.application.event.PaymentSuccessEvent;
import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaConsumer {
    private final ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    @KafkaListener(topics = "payment-success", groupId = "payment-group")
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        try {
            log.info("[KafkaConsumer]외부 데이터 플랫폼으로 결제 정보 전송 시작");
            Payment payment = event.payment();
            externalPaymentDataPlatformService.sendPaymentInfo(payment);
            log.info("[KafkaConsumer]외부 데이터 플랫폼으로 결제 정보 전송 종료");
        } catch (Exception e) {
            log.warn("[KafkaConsumer]외부 데이터 플랫폼으로 결제 정보 전송 실패, error : {}", e.getMessage());
            throw e;
        }
    }
}
