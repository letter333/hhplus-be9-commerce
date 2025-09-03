package kr.hhplus.be.server.infrastructure.kafka;

import kr.hhplus.be.server.application.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaProducer {
    private static final String TOPIC = "payment-success";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(PaymentSuccessEvent event) {
        try {
            log.info("[kafkaProducer]결제 정보 전송 시작");
            kafkaTemplate.send(TOPIC, event);
            log.info("[kafkaProducer]결제 정보 전송 종료");
        } catch (Exception e) {
            log.error("[kafkaProducer]결제 정보 전송 실패", e);
        }
    }
}
