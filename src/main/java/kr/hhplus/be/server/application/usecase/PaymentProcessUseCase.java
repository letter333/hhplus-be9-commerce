package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.event.PaymentSuccessEvent;
import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessUseCase {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final RedissonLockManager redissonLockManager;

    public Payment execute(PaymentProcessCommand command) {
        String orderLockKey = "paymentProcessLock:" + command.orderId();
        RLock orderLock = redissonLockManager.getLock(orderLockKey);

        RLock pointLock = null;
        String pointLockKey = null;

        if(command.paymentMethod() == PaymentMethod.POINT) {
            pointLockKey = "pointLock:" + command.userId();
            pointLock = redissonLockManager.getLock(pointLockKey);
        }

        try {
            boolean isLocked = orderLock.tryLock(10, 5, java.util.concurrent.TimeUnit.SECONDS);
            if(!isLocked) {
                throw new IllegalStateException("결제를 위한 락을 획득하지 못했습니다.");
            }

            if(pointLock != null) {
                boolean isPointLocked = pointLock.tryLock(10, 5, java.util.concurrent.TimeUnit.SECONDS);
                if(!isPointLocked) {
                    throw new IllegalStateException("포인트 처리를 위한 락을 획득하지 못했습니다.");
                }
            }

            return transactionTemplate.execute(status -> {
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
                applicationEventPublisher.publishEvent(new PaymentSuccessEvent(savedPayment));

                return savedPayment;
            });
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("결제 처리 중 문제가 발생했습니다.");
        } finally {
            if(orderLock.isHeldByCurrentThread()) {
                orderLock.unlock();
            }
            if(pointLock != null && pointLock.isHeldByCurrentThread()) {
                pointLock.unlock();
            }
        }
    }

    public void validateOrderStatus(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalArgumentException("이미 결제된 주문입니다.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("결제 대기중인 주문만 결제 가능합니다.");
        }
    }
}
