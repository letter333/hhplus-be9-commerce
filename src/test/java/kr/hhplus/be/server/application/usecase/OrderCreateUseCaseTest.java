package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.OrderCreateCommand;
import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("주문 단위 테스트")
class OrderCreateUseCaseTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderCreateUseCase orderCreateUseCase;

    @Nested
    @DisplayName("주문 생성 테스트")
    class OrderCreateTest {
        @Test
        @DisplayName("주문 생성 성공")
        void 주문_생성() {
            //given
            Long userId = 1L;
            List<OrderProduct> orderProductList = List.of(
                    new OrderProduct(1L, 1L, 5, 1000L),
                    new OrderProduct(2L, 2L, 1, 20_000L)
            );
            Address shippingAddress = new Address("기본 주소", "상세 주소", "1234567890");
            String recipientNumber = "010-1234-5678";
            OrderCreateCommand command = new OrderCreateCommand(userId, orderProductList, shippingAddress, recipientNumber);

            Product product1 = Product.builder().id(1L).name("상품1").price(10000L).stock(10).build();
            Product product2 = Product.builder().id(2L).name("상품2").price(5000L).stock(5).build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Order resultOrder = orderCreateUseCase.execute(command);

            // then
            assertThat(resultOrder).isNotNull();
            assertThat(resultOrder.getUserId()).isEqualTo(userId);
            assertThat(resultOrder.getTotalPrice()).isEqualTo(55000L);
            assertThat(resultOrder.getFinalPrice()).isEqualTo(55000L);
            assertThat(product1.getStock()).isEqualTo(5);
            assertThat(product2.getStock()).isEqualTo(4);

            verify(productRepository, times(2)).findById(anyLong());
            verify(productRepository, times(1)).saveAll(anyList());
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 예외 처리")
        void 존재하지_않는_상품_주문() {
            // given
            Long userId = 1L;
            List<OrderProduct> orderProductList = List.of(new OrderProduct(1L, 1111L, 1, 1000L));
            Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
            String recipientNumber = "010-1234-5678";
            OrderCreateCommand command = new OrderCreateCommand(userId, orderProductList, shippingAddress, recipientNumber);

            when(productRepository.findById(1111L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command)).isInstanceOf(IllegalArgumentException.class);

            verify(orderRepository, never()).save(any(Order.class));
            verify(productRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("동시성 제어 테스트")
    class ConcurrencyControlTest {
        @Test
        @DisplayName("동일 상품에 대한 동시 주문 시 재고가 정확히 감소한다")
        void 동일_상품_동시_주문() throws InterruptedException {
            // given
            int threadCount = 100;
            int orderQuantityPerThread = 1;
            Product testProduct = Product.builder()
                    .id(1L)
                    .name("테스트 상품")
                    .price(10000L)
                    .stock(100)
                    .build();

            Address testAddress = new Address("서울시 강남구", "테헤란로 123", "12345");

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Order> successOrders = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger failureCount = new AtomicInteger(0);

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return Order.builder()
                        .id(System.currentTimeMillis()) // 임시 ID 생성
                        .userId(order.getUserId())
                        .status(order.getStatus())
                        .orderProducts(order.getOrderProducts())
                        .totalPrice(order.getTotalPrice())
                        .finalPrice(order.getFinalPrice())
                        .shippingAddress(order.getShippingAddress())
                        .recipientNumber(order.getRecipientNumber())
                        .build();
            });

            // when
            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1L;
                executorService.submit(() -> {
                    try {
                        OrderProduct orderProduct = new OrderProduct(1L, 1L, orderQuantityPerThread, 10000L);
                        OrderCreateCommand command = new OrderCreateCommand(
                                userId,
                                List.of(orderProduct),
                                testAddress,
                                "010-1234-5678"
                        );

                        Order result = orderCreateUseCase.execute(command);
                        successOrders.add(result);

                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            int expectedSuccessCount = 100;
            int expectedFailureCount = threadCount - expectedSuccessCount;

            assertThat(successOrders).hasSize(expectedSuccessCount);
            assertThat(failureCount.get()).isEqualTo(expectedFailureCount);
            assertThat(testProduct.getStock()).isEqualTo(0);

            successOrders.forEach(order -> {
                assertThat(order.getOrderProducts()).hasSize(1);
                assertThat(order.getTotalPrice()).isEqualTo(10000L);
                assertThat(order.getFinalPrice()).isEqualTo(10000L);
            });
        }

        @Test
        @DisplayName("재고 부족 시 일부 주문만 성공하고 나머지는 실패한다")
        void 재고_부족시_일부_주문만_성공() throws InterruptedException {
            // given
            Product testProduct = Product.builder()
                    .id(1L)
                    .name("테스트 상품")
                    .price(10000L)
                    .stock(10)
                    .build();

            Address testAddress = new Address("서울시 강남구", "테헤란로 123", "12345");

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            int threadCount = 20;
            int orderQuantityPerThread = 2;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Order> successOrders = Collections.synchronizedList(new ArrayList<>());
            List<Exception> failures = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1L;
                executorService.submit(() -> {
                    try {
                        OrderProduct orderProduct = new OrderProduct(1L, 1L, orderQuantityPerThread, 10000L);
                        OrderCreateCommand command = new OrderCreateCommand(
                                userId,
                                List.of(orderProduct),
                                testAddress,
                                "010-1234-5678"
                        );

                        Order result = orderCreateUseCase.execute(command);
                        successOrders.add(result);

                    } catch (Exception e) {
                        failures.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            int maxPossibleOrders = 10 / orderQuantityPerThread;
            assertThat(successOrders).hasSize(maxPossibleOrders);
            assertThat(failures).hasSize(threadCount - maxPossibleOrders);
            assertThat(testProduct.getStock()).isEqualTo(0);

            failures.forEach(exception -> {
                assertThat(exception).isInstanceOf(IllegalArgumentException.class);
            });
        }
    }

    @Nested
    @DisplayName("다중 상품 동시성 테스트")
    class MultipleProductConcurrencyTest {

        @Test
        @DisplayName("서로 다른 상품에 대한 동시 주문은 서로 영향을 주지 않는다")
        void 서로_다른_상품_동시_주문_독립적_처리() throws InterruptedException {
            // given
            Product product1 = Product.builder().id(1L).name("상품1").price(10000L).stock(50).build();
            Product product2 = Product.builder().id(2L).name("상품2").price(20000L).stock(50).build();
            Address testAddress = new Address("서울시 강남구", "테헤란로 123", "12345");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return Order.builder()
                        .id(System.currentTimeMillis())
                        .userId(order.getUserId())
                        .status(order.getStatus())
                        .orderProducts(order.getOrderProducts())
                        .totalPrice(order.getTotalPrice())
                        .discountAmount(order.getDiscountAmount())
                        .finalPrice(order.getFinalPrice())
                        .shippingAddress(order.getShippingAddress())
                        .recipientNumber(order.getRecipientNumber())
                        .createdAt(order.getCreatedAt())
                        .build();
            });

            int threadCount = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Order> successOrders = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1L;
                final long productId = (i % 2) + 1L;

                executorService.submit(() -> {
                    try {
                        OrderProduct orderProduct = new OrderProduct(1L, productId, 1, productId * 10000L);
                        OrderCreateCommand command = new OrderCreateCommand(
                                userId,
                                List.of(orderProduct),
                                testAddress,
                                "010-1234-5678"
                        );

                        Order result = orderCreateUseCase.execute(command);
                        successOrders.add(result);

                    } catch (Exception e) {

                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(successOrders).hasSize(100);
            assertThat(product1.getStock()).isEqualTo(0);
            assertThat(product2.getStock()).isEqualTo(0);

            long product1Orders = successOrders.stream()
                    .mapToLong(order -> order.getOrderProducts().stream()
                            .filter(op -> op.getProductId().equals(1L))
                            .count())
                    .sum();
            long product2Orders = successOrders.stream()
                    .mapToLong(order -> order.getOrderProducts().stream()
                            .filter(op -> op.getProductId().equals(2L))
                            .count())
                    .sum();

            assertThat(product1Orders).isEqualTo(50);
            assertThat(product2Orders).isEqualTo(50);
        }

        @Nested
        @DisplayName("락 타임아웃 테스트")
        class LockTimeoutTest {

            @Test
            @DisplayName("락 획득 타임아웃 시 예외 발생")
            void 락_획득_타임아웃() throws InterruptedException {
                // given
                Product testProduct = Product.builder()
                        .id(1L)
                        .name("테스트 상품")
                        .price(10000L)
                        .stock(10)
                        .build();
                Address testAddress = new Address("서울시 강남구", "테헤란로 123", "12345");

                when(productRepository.findById(1L)).thenAnswer(invocation -> {
                    Thread.sleep(6000);
                    return Optional.of(testProduct);
                });

                int threadCount = 3;
                ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
                CountDownLatch latch = new CountDownLatch(threadCount);
                List<Exception> timeoutExceptions = Collections.synchronizedList(new ArrayList<>());

                // when
                for (int i = 0; i < threadCount; i++) {
                    final long userId = i + 1L;
                    executorService.submit(() -> {
                        try {
                            OrderProduct orderProduct = new OrderProduct(1L, 1L, 1, 10000L);
                            OrderCreateCommand command = new OrderCreateCommand(
                                    userId,
                                    List.of(orderProduct),
                                    testAddress,
                                    "010-1234-5678"
                            );

                            orderCreateUseCase.execute(command);

                        } catch (IllegalStateException e) {
                            if (e.getMessage().contains("요청에 실패했습니다")) {
                                timeoutExceptions.add(e);
                            }
                        } catch (Exception e) {

                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await(20, TimeUnit.SECONDS);
                executorService.shutdown();

                // then
                assertThat(timeoutExceptions.size()).isGreaterThan(0);
            }
        }
    }
}