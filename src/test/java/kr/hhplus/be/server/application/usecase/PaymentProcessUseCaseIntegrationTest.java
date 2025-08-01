package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class PaymentProcessUseCaseIntegrationTest {

    @Autowired
    private PaymentProcessUseCase paymentProcessUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @MockitoBean
    private ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    private User testUser;
    private Order testOrder;
    private Point testPoint;


    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("테스트 사용자")
                .phoneNumber("010-1234-5678")
                .build();

        testPoint = Point.builder()
                .userId(testUser.getId())
                .balance(100000L)
                .build();
        testPoint = pointRepository.save(testPoint);

        Address testAddress = new Address(
                "서울시 강남구 테헤란로",
                "123번길 45",
                "12345"
        );

        testOrder = Order.builder()
                .userId(testUser.getId())
                .status(OrderStatus.PENDING)
                .orderProducts(List.of())
                .totalPrice(50000L)
                .discountAmount(0L)
                .finalPrice(50000L)
                .shippingAddress(testAddress)
                .recipientNumber("010-9876-5432")
                .createdAt(LocalDateTime.now())
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        pointHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        pointRepository.deleteAll();
        pointHistoryRepository.deleteAll();
    }


    @Test
    @DisplayName("포인트 결제 성공")
    void 포인트_결제() {
        // given
        PaymentProcessCommand command = new PaymentProcessCommand(
                testUser.getId(),
                testOrder.getId(),
                PaymentMethod.POINT
        );

        // when
        Payment result = paymentProcessUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(testOrder.getId());
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.POINT);
        assertThat(result.getAmount()).isEqualTo(testOrder.getFinalPrice());
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        Point updatedPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(updatedPoint.getBalance()).isEqualTo(100000L - 50000L);

        verify(externalPaymentDataPlatformService, times(1)).sendPaymentInfo(any(Payment.class));
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제 시 예외 발생")
    void 존재하지_않는_주문() {
        // given
        PaymentProcessCommand command = new PaymentProcessCommand(
                testUser.getId(),
                999L,
                PaymentMethod.POINT
        );

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 결제된 주문 결제 시 예외 발생")
    void 이미_결제된_주문() {
        // given
        testOrder.changeStatus(OrderStatus.PAID);
        orderRepository.save(testOrder);

        PaymentProcessCommand command = new PaymentProcessCommand(
                testUser.getId(),
                testOrder.getId(),
                PaymentMethod.POINT
        );

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("결제 대기 상태가 아닌 주문 결제 시 예외 발생")
    void 결제_대기_상태가_아닌_주문() {
        // given
        testOrder.changeStatus(OrderStatus.CANCELED);
        orderRepository.save(testOrder);

        PaymentProcessCommand command = new PaymentProcessCommand(
                testOrder.getId(),
                testUser.getId(),
                PaymentMethod.POINT
        );

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 부족으로 결제 실패")
    void 포인트_부족() {
        // given
        testPoint.use(90000L);
        pointRepository.save(testPoint);

        PaymentProcessCommand command = new PaymentProcessCommand(
                testUser.getId(),
                testOrder.getId(),
                PaymentMethod.POINT
        );

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        Order unchangedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 포인트 결제 시 예외 발생")
    void 존재하지_않는_사용자_포인트_결제() {
        // given
        PaymentProcessCommand command = new PaymentProcessCommand(
                testOrder.getId(),
                999L,
                PaymentMethod.POINT
        );

        // when & then
        assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동시 결제 요청")
    void 동시성_테스트() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        PaymentProcessCommand command = new PaymentProcessCommand(
                testUser.getId(),
                testOrder.getId(),
                PaymentMethod.POINT
        );

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    paymentProcessUseCase.execute(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(1, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);

        Order finalOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        Point finalPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(finalPoint.getBalance()).isEqualTo(100000L - 50000L);
    }

}
