package kr.hhplus.be.server.infrastructure.scheduler;

import kr.hhplus.be.server.application.usecase.OrderCancelUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {
    private final OrderCancelUseCase orderCancelUseCase;

    @Scheduled(cron = "0 * * * * *")
    public void cancelUnpaidOrder() {
        try {
            log.info("결제 대기 주문 취소 스케줄러 시작");
            orderCancelUseCase.execute();
            log.info("결제 대기 주문 취소 스케줄러 종료.");
        } catch (Exception ex) {
            log.error("결제 대기 주문 취소 스케줄러 실행 중 오류 발생", ex);
        }
    }
}
