package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.event.PaymentSuccessEvent;
import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessUseCaseTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PaymentProcessUseCase paymentProcessUseCase;

    @Test
    @DisplayName("포인트 결제 성공")
    void 포인트_결제() {
        // given
        long userId = 1L;
        long orderId = 1L;
        long finalPrice = 50000L;
        long initialPoints = 100000L;
        PaymentProcessCommand command = new PaymentProcessCommand(userId, orderId, PaymentMethod.POINT);

        Order mockOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .finalPrice(finalPrice)
                .build();

        Point mockPoint = Point.builder()
                .userId(userId)
                .balance(initialPoints)
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));
        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(mockPoint));
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Payment resultPayment = paymentProcessUseCase.execute(command);

        // then
        assertThat(resultPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(resultPayment.getAmount()).isEqualTo(finalPrice);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(mockPoint.getBalance()).isEqualTo(initialPoints - finalPrice);

        then(orderRepository).should().findById(orderId);
        then(pointRepository).should().findByUserId(userId);
        then(pointRepository).should().save(mockPoint);
        then(orderRepository).should().save(mockOrder);
        then(paymentRepository).should().save(any(Payment.class));
        then(applicationEventPublisher).should().publishEvent(any(PaymentSuccessEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 주문 결제 시 예외 처리")
    void 존재하지_않는_주문_결제() {
        // given
        long nonExistentOrderId = 999L;
        PaymentProcessCommand command = new PaymentProcessCommand(1L, nonExistentOrderId, PaymentMethod.POINT);
        given(orderRepository.findById(nonExistentOrderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        then(paymentRepository).should(never()).save(any());
        then(orderRepository).should(never()).save(any());
        then(pointRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("포인트 부족 시 결제 실패")
    void 포인트_부족() {
        // given
        long userId = 1L;
        long orderId = 1L;
        PaymentProcessCommand command = new PaymentProcessCommand(userId, orderId, PaymentMethod.POINT);

        long finalPrice = 50_000L;
        Order mockOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .finalPrice(finalPrice)
                .build();

        long insufficientPoints = 40_000L;
        Point mockPoint = Point.builder()
                .userId(userId)
                .balance(insufficientPoints)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(mockPoint));

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(pointRepository, never()).save(any(Point.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}