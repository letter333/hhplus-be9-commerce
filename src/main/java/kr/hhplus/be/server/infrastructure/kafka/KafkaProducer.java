package kr.hhplus.be.server.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Object event) {
        try {
            log.info("[kafkaProducer]{} 토픽으로 메시지 전송 시작", topic);
            kafkaTemplate.send(topic, event);
            log.info("[kafkaProducer]{} 토픽으로 메시지 전송 종료", topic);
        } catch (Exception e) {
            log.error("[kafkaProducer]{} 토픽 메시지 전송 실패", topic, e);
        }
    }

    public void send(String topic, String key, Object event) {
        try {
            log.info("[kafkaProducer]{} 토픽으로 키({})와 함께 메시지 전송 시작", topic, key);
            kafkaTemplate.send(topic, key, event);
            log.info("[kafkaProducer]{} 토픽으로 키({})와 함께 메시지 전송 종료", topic, key);
        } catch (Exception e) {
            log.error("[kafkaProducer]{} 토픽 메시지 전송 실패 (키: {})", topic, key, e);
        }
    }

}
