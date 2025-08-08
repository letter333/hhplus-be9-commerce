package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.repository.*;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @MockitoBean
    private ExternalPaymentDataPlatformService externalPaymentDataPlatformService;

    private User testUser;
    private Order testOrder;
    private Point testPoint;


    void setUp() {
        testUser = User.builder()
                .name("테스트 사용자")
                .phoneNumber("010-1234-5678")
                .build();
        testUser = userRepository.save(testUser);

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
        userRepository.deleteAll();
        pointRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Nested
    @DisplayName("결제 통합 테스트")
    class PointPaymentIntegrationTest {
        @Test
        @DisplayName("포인트 결제 성공")
        void 포인트_결제() {
            // given
            setUp();
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
            setUp();
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
            setUp();
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
            setUp();
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
            setUp();
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
            setUp();
            PaymentProcessCommand command = new PaymentProcessCommand(
                    testOrder.getId(),
                    999L,
                    PaymentMethod.POINT
            );

            // when & then
            assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결제 동시성 테스트")
    class ConcurrencyControlTest {
        @Test
        @DisplayName("동시에 같은 주문에 대한 결제 요청 시 하나만 성공")
        void 동일_주문_동시_결제_하나만_성공() throws InterruptedException {
            // given
            setUp();
            int threadCount = 5;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

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
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(threadCount - 1);

            assertThat(exceptions).hasSize(threadCount - 1);
            assertThat(exceptions).allMatch(ex ->
                    ex instanceof IllegalArgumentException ||
                            ex instanceof RuntimeException);

            Order finalOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAID);

            Point finalPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            assertThat(finalPoint.getBalance()).isEqualTo(50000L);

            List<Payment> payments = paymentRepository.findByOrderId(testOrder.getId());
            assertThat(payments).hasSize(1);
        }

        @Test
        @DisplayName("포인트 부족 시 낙관적 락 예외와 비즈니스 예외를 구분")
        void 포인트_부족_시_예외_구분() {
            // given
            setUp();
            Point existingPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            existingPoint.use(90000L);
            pointRepository.save(existingPoint);

            PaymentProcessCommand command = new PaymentProcessCommand(
                    testUser.getId(),
                    testOrder.getId(),
                    PaymentMethod.POINT
            );

            // when & then
            assertThatThrownBy(() -> paymentProcessUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);

            Point unchangedPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            assertThat(unchangedPoint.getBalance()).isEqualTo(10000L);

            Order unchangedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("여러 사용자가 동시에 포인트 결제 시 정상 처리")
        void 여러_사용자_동시_포인트_결제() throws InterruptedException {
            // given
            int userCount = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(userCount);
            CountDownLatch latch = new CountDownLatch(userCount);
            AtomicInteger successCount = new AtomicInteger(0);
            List<Payment> successfulPayments = Collections.synchronizedList(new ArrayList<>());
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            List<User> users = new ArrayList<>();
            List<Order> orders = new ArrayList<>();
            List<Point> points = new ArrayList<>();

            for (int i = 0; i < userCount; i++) {
                User user = User.builder()
                        .name("테스트 사용자" + i)
                        .phoneNumber("test-phone-" + i)
                        .build();
                User savedUser = userRepository.save(user);
                users.add(savedUser);

                Point point = Point.builder()
                        .userId(savedUser.getId())
                        .balance(100000L)
                        .build();
                points.add(pointRepository.save(point));

                Address address = new Address(
                        "서울시 강남구 테헤란로",
                        "123번길 4" + i,
                        "12345"
                );

                Order order = Order.builder()
                        .userId(savedUser.getId())
                        .status(OrderStatus.PENDING)
                        .orderProducts(List.of())
                        .totalPrice(30000L)
                        .discountAmount(0L)
                        .finalPrice(30000L)
                        .shippingAddress(address)
                        .recipientNumber("010-9876-543" + i)
                        .createdAt(LocalDateTime.now())
                        .build();
                orders.add(orderRepository.save(order));
            }

            // when
            for (int i = 0; i < userCount; i++) {
                final int userIndex = i;
                executorService.submit(() -> {
                    try {
                        PaymentProcessCommand command = new PaymentProcessCommand(
                                users.get(userIndex).getId(),
                                orders.get(userIndex).getId(),
                                PaymentMethod.POINT
                        );
                        Payment result = paymentProcessUseCase.execute(command);
                        successfulPayments.add(result);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(15, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(userCount);
            assertThat(exceptions).isEmpty();
            assertThat(successfulPayments).hasSize(userCount);

            for (Order order : orders) {
                Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            }

            for (int i = 0; i < userCount; i++) {
                Point updatedPoint = pointRepository.findByUserId(users.get(i).getId()).orElseThrow();
                assertThat(updatedPoint.getBalance()).isEqualTo(70000L);
            }

            List<Payment> allPayments = paymentRepository.findAll();
            assertThat(allPayments).hasSize(userCount);
            assertThat(allPayments).allMatch(payment ->
                    payment.getStatus() == PaymentStatus.SUCCESS &&
                            payment.getAmount() == 30000L &&
                            payment.getPaymentMethod() == PaymentMethod.POINT
            );
        }

        @Test
        @DisplayName("포인트 부족 상황에서 동시 결제 요청 시 일부만 성공")
        void 포인트_부족_상황_동시_결제() throws InterruptedException {
            // given
            setUp();
            testPoint.use(70000L);
            pointRepository.save(testPoint);

            int orderCount = 3;
            long orderPrice = 20000L;
            ExecutorService executorService = Executors.newFixedThreadPool(orderCount);
            CountDownLatch latch = new CountDownLatch(orderCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < orderCount; i++) {
                Address address = new Address(
                        "서울시 강남구 테헤란로",
                        "123번길 4" + i,
                        "12345"
                );

                Order order = Order.builder()
                        .userId(testUser.getId())
                        .status(OrderStatus.PENDING)
                        .orderProducts(List.of())
                        .totalPrice(orderPrice)
                        .discountAmount(0L)
                        .finalPrice(orderPrice)
                        .shippingAddress(address)
                        .recipientNumber("test-phone-" + i)
                        .createdAt(LocalDateTime.now())
                        .build();
                orders.add(orderRepository.save(order));
            }

            // when
            for (int i = 0; i < orderCount; i++) {
                final int orderIndex = i;
                executorService.submit(() -> {
                    try {
                        PaymentProcessCommand command = new PaymentProcessCommand(
                                testUser.getId(),
                                orders.get(orderIndex).getId(),
                                PaymentMethod.POINT
                        );
                        paymentProcessUseCase.execute(command);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(15, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(2);

            List<Payment> payments = paymentRepository.findAll();
            assertThat(payments).hasSize(1);
            assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);

            Point finalPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            assertThat(finalPoint.getBalance()).isEqualTo(10000L);

            long paidOrderCount = orders.stream()
                    .map(order -> orderRepository.findById(order.getId()).orElseThrow())
                    .filter(order -> order.getStatus() == OrderStatus.PAID)
                    .count();
            assertThat(paidOrderCount).isEqualTo(1);
        }

    }
}
