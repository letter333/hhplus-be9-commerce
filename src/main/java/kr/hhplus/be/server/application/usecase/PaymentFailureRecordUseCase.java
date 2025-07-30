package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.PaymentFailureCommand;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.model.PaymentStatus;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentFailureRecordUseCase {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment execute(PaymentFailureCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다."));

        Payment failurePayment = Payment.builder()
                .orderId(command.orderId())
                .paymentMethod(command.paymentMethod())
                .amount(order.getFinalPrice())
                .status(PaymentStatus.FAILED)
                .build();

        return paymentRepository.save(failurePayment);
    }
}
