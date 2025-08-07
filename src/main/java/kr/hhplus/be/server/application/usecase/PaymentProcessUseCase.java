package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessUseCase {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            noRetryFor = {IllegalArgumentException.class},
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    @Transactional
    public Payment execute(PaymentProcessCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 정보입니다."));

        validateOrderStatus(order);

        if (command.paymentMethod() == PaymentMethod.POINT) {
            Point point = pointRepository.findByUserId(command.userId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            point.use(order.getFinalPrice());
            pointRepository.save(point);

            PointHistory pointHistory = PointHistory.builder()
                    .userId(command.userId())
                    .orderId(order.getId())
                    .type(PointHistoryType.USE)
                    .amount(order.getFinalPrice())
                    .balanceAfter(point.getBalance())
                    .createdAt(LocalDateTime.now())
                    .build();
            pointHistoryRepository.save(pointHistory);

        } else if (command.paymentMethod() == PaymentMethod.CARD) {
            //카드 결제 (외부 PG연동)

        }

        order.changeStatus(OrderStatus.PAID);
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .paymentMethod(command.paymentMethod())
                .amount(order.getFinalPrice())
                .status(PaymentStatus.SUCCESS)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        externalPaymentDataPlatformService.sendPaymentInfo(savedPayment);

        return savedPayment;
    }

    public void validateOrderStatus(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalArgumentException("이미 결제된 주문입니다.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("결제 대기중인 주문만 결제 가능합니다.");
        }
    }

    @Recover
    public Payment recoverFromOptimisticLockFailure(ObjectOptimisticLockingFailureException ex,
                                                    PaymentProcessCommand command) {
        log.error("결제 처리 최종 실패 - 낙관적 락 충돌로 인한 재시도 한계 도달", ex);
        log.error("실패한 요청 정보 - 사용자 ID: {}, 주문 ID: {}, 결제 방식: {}",
                command.userId(), command.orderId(), command.paymentMethod());
        log.error("오류 메시지: {}", ex.getMessage());
        log.error("충돌 발생 엔티티: {}", ex.getPersistentClassName());

        throw new RuntimeException(
                String.format("결제 처리 중 동시성 충돌이 발생하여 재시도에 실패했습니다. " +
                        "주문 ID: %d, 사용자 ID: %d", command.orderId(), command.userId()), ex);
    }

    @Recover
    public Payment recoverFromIllegalArgument(IllegalArgumentException ex,
                                              PaymentProcessCommand command) {
        log.warn("결제 처리 실패 - 잘못된 요청: {}", ex.getMessage());
        throw ex;
    }

    @Recover
    public Payment recoverFromAnyException(Exception ex, PaymentProcessCommand command) {
        log.error("결제 처리 중 예상치 못한 오류 발생", ex);
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new RuntimeException("결제 처리 중 시스템 오류가 발생했습니다.", ex);
    }

}
