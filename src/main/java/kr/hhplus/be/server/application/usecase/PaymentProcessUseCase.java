package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.PaymentProcessCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessUseCase {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;

    public Payment execute(PaymentProcessCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 정보입니다."));

        if(order.getStatus() == OrderStatus.PENDING) {
            if(command.paymentMethod() == PaymentMethod.POINT) {
                Point point = pointRepository.findByUserId(command.userId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

                point.use(order.getFinalPrice());

                pointRepository.save(point);

                order.changeStatus(OrderStatus.PAID);
                orderRepository.save(order);
            } else if (command.paymentMethod() == PaymentMethod.CARD) {
                //카드 결제 (외부 PG연동)
            }
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .paymentMethod(command.paymentMethod())
                .amount(order.getFinalPrice())
                .status(PaymentStatus.SUCCESS)
                .build();

        return paymentRepository.save(payment);
    }
}
