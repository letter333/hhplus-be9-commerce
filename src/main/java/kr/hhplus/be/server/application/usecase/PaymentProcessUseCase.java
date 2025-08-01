package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentProcessUseCase {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    @Transactional
    public Payment execute(PaymentProcessCommand command) {
        Order order = orderRepository.findByIdWithLock(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 정보입니다."));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalArgumentException("이미 결제된 주문입니다.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("결제 대기중인 주문만 결제 가능합니다.");
        }

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
}
