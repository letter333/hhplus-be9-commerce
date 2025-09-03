package kr.hhplus.be.server.application.event;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    @Async
    @TransactionalEventListener
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        Payment payment = event.payment();
        try {
            externalPaymentDataPlatformService.sendPaymentInfo(payment);
        } catch (Exception e) {
            log.warn("외부 데이터 플랫폼으로 결제 정보 전송 실패 paymentId : {}, error : {}", payment.getId(), e.getMessage());
        }
    }
}
