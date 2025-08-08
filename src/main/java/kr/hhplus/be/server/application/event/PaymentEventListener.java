package kr.hhplus.be.server.application.event;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    @Async
    @TransactionalEventListener
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        Payment payment = event.payment();
        try {
            externalPaymentDataPlatformService.sendPaymentInfo(payment);
        } catch (Exception e) {

        }
    }
}
