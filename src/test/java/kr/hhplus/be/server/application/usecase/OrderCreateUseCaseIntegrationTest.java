package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.OrderCreateCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class OrderCreateUseCaseIntegrationTest {
    @Autowired
    private OrderCreateUseCase orderCreateUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @BeforeEach
    void setUp() {
        orderProductRepository.deleteAll();
        orderRepository.deleteAll();
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("주문 생성 기본 테스트")
    class OrderCreateBasicTest {

        @Test
        @DisplayName("주문 생성 성공 테스트")
        void 주문_생성_성공() {
            // given
            Product product1 = Product.builder()
                    .name("상품1")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(100)
                    .build();

            Product product2 = Product.builder()
                    .name("상품2")
                    .description("상품 설명")
                    .price(20000L)
                    .stock(50)
                    .build();

            Product savedProduct1 = productRepository.save(product1);
            Product savedProduct2 = productRepository.save(product2);

            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(savedProduct1.getId())
                            .quantity(2)
                            .build(),
                    OrderProduct.builder()
                            .productId(savedProduct2.getId())
                            .quantity(1)
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    null,
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when
            Order result = orderCreateUseCase.execute(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getTotalPrice()).isEqualTo(40000L);
            assertThat(result.getFinalPrice()).isEqualTo(40000L);
            assertThat(result.getShippingAddress()).isEqualTo(shippingAddress);
            assertThat(result.getRecipientNumber()).isEqualTo("010-1234-5678");

            Product updatedProduct1 = productRepository.findById(savedProduct1.getId()).orElseThrow();
            Product updatedProduct2 = productRepository.findById(savedProduct2.getId()).orElseThrow();
            assertThat(updatedProduct1.getStock()).isEqualTo(98);
            assertThat(updatedProduct2.getStock()).isEqualTo(49);

            List<OrderProduct> savedOrderProducts = orderProductRepository.findByOrderId(result.getId());
            assertThat(savedOrderProducts).hasSize(2);
            assertThat(savedOrderProducts.get(0).getPrice()).isEqualTo(10000L);
            assertThat(savedOrderProducts.get(1).getPrice()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("존재하지 않는 상품 주문 시 예외 발생")
        void 존재하지_않는_상품_주문_예외() {
            // given
            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(999L)
                            .quantity(1)
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    null,
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("재고 부족 시 예외 발생")
        void 재고_부족_예외() {
            // given
            Product product = Product.builder()
                    .name("상품")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(5)
                    .build();

            Product savedProduct = productRepository.save(product);

            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(savedProduct.getId())
                            .quantity(10) // 재고보다 많은 수량
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    null,
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 테스트")
    class CouponApplicationTest {

        @Test
        @DisplayName("고정 할인 쿠폰 적용 성공")
        void 고정_할인_쿠폰_적용_성공() {
            // given
            Product product = Product.builder()
                    .name("상품")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(100)
                    .build();
            Product savedProduct = productRepository.save(product);

            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
            Coupon coupon = Coupon.builder()
                    .name("3000원 할인 쿠폰")
                    .type(CouponType.FIXED)
                    .discountAmount(3000L)
                    .quantity(100)
                    .issuedQuantity(1)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            Coupon savedCoupon = couponRepository.save(coupon);

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(1L)
                    .couponId(savedCoupon.getId())
                    .couponCode("ABCD-1234-5678-0123")
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(savedProduct.getId())
                            .quantity(2)
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    savedUserCoupon.getId(),
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when
            Order result = orderCreateUseCase.execute(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualTo(20000L); // 10000*2
            assertThat(result.getFinalPrice()).isEqualTo(17000L); // 20000-3000

            UserCoupon updatedUserCoupon = userCouponRepository.findById(savedUserCoupon.getId()).orElseThrow();
            assertThat(updatedUserCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
            assertThat(updatedUserCoupon.getUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("비율 할인 쿠폰 적용 성공")
        void 비율_할인_쿠폰_적용_성공() {
            // given
            Product product = Product.builder()
                    .name("상품")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(100)
                    .build();
            Product savedProduct = productRepository.save(product);

            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
            Coupon coupon = Coupon.builder()
                    .name("10% 할인 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(1)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            Coupon savedCoupon = couponRepository.save(coupon);

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(1L)
                    .couponId(savedCoupon.getId())
                    .couponCode("TEST-COUPON-002")
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(savedProduct.getId())
                            .quantity(3)
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    savedUserCoupon.getId(),
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when
            Order result = orderCreateUseCase.execute(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualTo(30000L);
            assertThat(result.getFinalPrice()).isEqualTo(27000L);

            UserCoupon updatedUserCoupon = userCouponRepository.findById(savedUserCoupon.getId()).orElseThrow();
            assertThat(updatedUserCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
        }

        @Test
        @DisplayName("잘못된 쿠폰 ID로 주문 시 예외 발생")
        void 잘못된_쿠폰_ID_예외() {
            // given
            Product product = Product.builder()
                    .name("상품")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(100)
                    .build();
            Product savedProduct = productRepository.save(product);

            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(savedProduct.getId())
                            .quantity(1)
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    999L,
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);

        }
    }

    @Nested
    @DisplayName("주문 동시성 제어 테스트")
    class ConcurrencyControlTest {

        @Test
        @DisplayName("동일 상품에 대한 동시 주문 시 재고가 정확히 감소한다")
        void 동일_상품_동시_주문_재고_제어() throws InterruptedException {
            // given
            int threadCount = 10;
            int orderQuantityPerThread = 2;
            int initialStock = 15;

            Product product = Product.builder()
                    .name("동시성 테스트 상품")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(initialStock)
                    .build();
            Product savedProduct = productRepository.save(product);

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1L;
                executorService.submit(() -> {
                    try {
                        List<OrderProduct> orderProductList = List.of(
                                OrderProduct.builder()
                                        .productId(savedProduct.getId())
                                        .quantity(orderQuantityPerThread)
                                        .build()
                        );

                        Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
                        OrderCreateCommand command = new OrderCreateCommand(
                                userId,
                                null,
                                orderProductList,
                                shippingAddress,
                                "010-1234-5678"
                        );

                        orderCreateUseCase.execute(command);
                        successCount.incrementAndGet();

                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            int expectedSuccessCount = initialStock / orderQuantityPerThread;
            int expectedFailureCount = threadCount - expectedSuccessCount;

            assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
            assertThat(failureCount.get()).isEqualTo(expectedFailureCount);

            Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(1);

            List<Order> savedOrders = orderRepository.findAll();
            assertThat(savedOrders).hasSize(expectedSuccessCount);
        }
    }

    @Nested
    @DisplayName("트랜잭션 롤백 테스트")
    class TransactionRollbackTest {

        @Test
        @Transactional
        @DisplayName("주문 처리 중 예외 발생 시 쿠폰 상태가 복구된다")
        void 주문_실패_시_쿠폰_상태_복구() {
            // given
            Product product = Product.builder()
                    .name("상품")
                    .description("상품 설명")
                    .price(10000L)
                    .stock(100)
                    .build();
            Product savedProduct = productRepository.save(product);

            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
            Coupon coupon = Coupon.builder()
                    .name("할인 쿠폰")
                    .type(CouponType.FIXED)
                    .discountAmount(1000L)
                    .quantity(100)
                    .issuedQuantity(1)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            Coupon savedCoupon = couponRepository.save(coupon);

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(1L)
                    .couponId(savedCoupon.getId())
                    .couponCode("ABCD-1234-5678-0123")
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

            List<OrderProduct> orderProductList = List.of(
                    OrderProduct.builder()
                            .productId(savedProduct.getId())
                            .quantity(200)
                            .build()
            );

            Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
            OrderCreateCommand command = new OrderCreateCommand(
                    1L,
                    savedUserCoupon.getId(),
                    orderProductList,
                    shippingAddress,
                    "010-1234-5678"
            );

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalStateException.class);

            UserCoupon restoredUserCoupon = userCouponRepository.findById(savedUserCoupon.getId()).orElseThrow();
            assertThat(restoredUserCoupon.getStatus()).isEqualTo(UserCouponStatus.ISSUED);
            assertThat(restoredUserCoupon.getUsedAt()).isNull();
        }
    }

    @Test
    @DisplayName("여러 상품 동시 주문 시 모든 상품 재고가 정확히 감소한다")
    void 여러_상품_동시_주문_재고_제어() throws InterruptedException {
        // given
        int threadCount = 5;
        int product1InitialStock = 20;
        int product2InitialStock = 15;
        int orderQuantityPerThread = 3;

        Product product1 = Product.builder()
                .name("상품1")
                .description("상품 설명1")
                .price(10000L)
                .stock(product1InitialStock)
                .build();

        Product product2 = Product.builder()
                .name("상품2")
                .description("상품 설명2")
                .price(20000L)
                .stock(product2InitialStock)
                .build();

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1L;
            executorService.submit(() -> {
                try {
                    List<OrderProduct> orderProductList = List.of(
                            OrderProduct.builder()
                                    .productId(savedProduct1.getId())
                                    .quantity(orderQuantityPerThread)
                                    .build(),
                            OrderProduct.builder()
                                    .productId(savedProduct2.getId())
                                    .quantity(orderQuantityPerThread)
                                    .build()
                    );

                    Address shippingAddress = new Address("서울시 강남구", "테헤란로 123", "12345");
                    OrderCreateCommand command = new OrderCreateCommand(
                            userId,
                            null,
                            orderProductList,
                            shippingAddress,
                            "010-1234-5678"
                    );

                    orderCreateUseCase.execute(command);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        int expectedSuccessCount = Math.min(product1InitialStock / orderQuantityPerThread,
                product2InitialStock / orderQuantityPerThread);
        int expectedFailureCount = threadCount - expectedSuccessCount;

        assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        assertThat(failureCount.get()).isEqualTo(expectedFailureCount);

        Product updatedProduct1 = productRepository.findById(savedProduct1.getId()).orElseThrow();
        Product updatedProduct2 = productRepository.findById(savedProduct2.getId()).orElseThrow();

        assertThat(updatedProduct1.getStock()).isEqualTo(product1InitialStock - (expectedSuccessCount * orderQuantityPerThread));
        assertThat(updatedProduct2.getStock()).isEqualTo(product2InitialStock - (expectedSuccessCount * orderQuantityPerThread));

        List<Order> savedOrders = orderRepository.findAll();
        assertThat(savedOrders).hasSize(expectedSuccessCount);
    }
}
