package kr.hhplus.be.server.application.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.entity.OrderEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class OrderCancelUseCaseIntegrationTest {
    @Autowired
    private OrderCancelUseCase orderCancelUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("주문 취소 통합 테스트")
    class OrderCancelBasicTest {
        @Test
        @DisplayName("30분 이상 PENDING 상태 주문 취소 성공")
        void 주문_취소_성공() {
            // given
            LocalDateTime thirtyOneMinutesAgo = LocalDateTime.now().minusMinutes(31);

            Product product1 = Product.builder()
                    .name("테스트 상품1")
                    .description("테스트 설명1")
                    .price(10000L)
                    .stock(50)
                    .build();

            Product product2 = Product.builder()
                    .name("테스트 상품2")
                    .description("테스트 설명2")
                    .price(20000L)
                    .stock(30)
                    .build();

            Product savedProduct1 = productRepository.save(product1);
            Product savedProduct2 = productRepository.save(product2);

            OrderEntity orderEntity = OrderEntity.builder()
                    .userId(1L)
                    .status(OrderStatus.PENDING)
                    .totalPrice(50000L)
                    .discountAmount(0L)
                    .finalPrice(50000L)
                    .shippingAddress1("기본 주소")
                    .shippingAddress2("상세 주소")
                    .shippingZipCode("12345")
                    .recipientNumber("010-1234-5678")
                    .build();

            // EntityManager로 직접 저장하여 createdAt 우회
            entityManager.persist(orderEntity);
            entityManager.flush();

            // 직접 SQL로 createdAt 업데이트
            entityManager.createQuery(
                            "UPDATE OrderEntity o SET o.createdAt = :createdAt WHERE o.id = :id")
                    .setParameter("createdAt", thirtyOneMinutesAgo)
                    .setParameter("id", orderEntity.getId())
                    .executeUpdate();

            entityManager.flush();
            entityManager.clear();


            List<OrderProduct> orderProductList = new ArrayList<>();

            orderProductList.add(OrderProduct.builder()
                    .orderId(orderEntity.getId())
                    .productId(savedProduct1.getId())
                    .quantity(2)
                    .price(10000L)
                    .build());

            orderProductList.add(OrderProduct.builder()
                    .orderId(orderEntity.getId())
                    .productId(savedProduct2.getId())
                    .quantity(1)
                    .price(20000L)
                    .build());


            orderProductRepository.saveAll(orderProductList);

            // when
            orderCancelUseCase.execute();

            // then
            Order updatedOrder = orderRepository.findById(orderEntity.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

            Product updatedProduct1 = productRepository.findById(savedProduct1.getId()).orElseThrow();
            Product updatedProduct2 = productRepository.findById(savedProduct2.getId()).orElseThrow();

            assertThat(updatedProduct1.getStock()).isEqualTo(52);
            assertThat(updatedProduct2.getStock()).isEqualTo(31);
        }

        @Test
        @DisplayName("30분 미만 PENDING 상태 주문 취소하지 않음")
        void 주문_취소_미대상() {
            // given
            Product product = Product.builder()
                    .name("테스트 상품")
                    .description("테스트 설명")
                    .price(10000L)
                    .stock(50)
                    .build();

            Product savedProduct = productRepository.save(product);

            Address address = new Address(
                    "기본 주소",
                    "상세 주소",
                    "12345"
            );

            Order order = Order.builder()
                    .userId(1L)
                    .status(OrderStatus.PENDING)
                    .totalPrice(10000L)
                    .discountAmount(0L)
                    .finalPrice(10000L)
                    .shippingAddress(address)
                    .recipientNumber("010-1234-5678")
                    .version(0L)
                    .build();

            Order savedOrder = orderRepository.save(order);

            List<OrderProduct> orderProductList = new ArrayList<>();

            orderProductList.add(OrderProduct.builder()
                    .orderId(savedOrder.getId())
                    .productId(savedProduct.getId())
                    .quantity(1)
                    .price(10000L)
                    .build());

            orderProductRepository.saveAll(orderProductList);

            // when
            orderCancelUseCase.execute();

            // then
            Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

            Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(50);
        }

        @Test
        @DisplayName("이미 PAID 상태인 주문 취소 대상 아님")
        void 이미_결제된_주문은_취소되지_않음() {
            // given
            LocalDateTime thirtyOneMinutesAgo = LocalDateTime.now().minusMinutes(31);

            Product product = Product.builder()
                    .name("테스트 상품")
                    .description("테스트 설명")
                    .price(10000L)
                    .stock(50)
                    .build();

            Product savedProduct = productRepository.save(product);

            OrderEntity orderEntity = OrderEntity.builder()
                    .userId(1L)
                    .status(OrderStatus.PAID)
                    .totalPrice(50000L)
                    .discountAmount(0L)
                    .finalPrice(50000L)
                    .shippingAddress1("기본 주소")
                    .shippingAddress2("상세 주소")
                    .shippingZipCode("12345")
                    .recipientNumber("010-1234-5678")
                    .build();

            // EntityManager로 직접 저장하여 createdAt 우회
            entityManager.persist(orderEntity);
            entityManager.flush();

            // 직접 SQL로 createdAt 업데이트
            entityManager.createQuery(
                            "UPDATE OrderEntity o SET o.createdAt = :createdAt WHERE o.id = :id")
                    .setParameter("createdAt", thirtyOneMinutesAgo)
                    .setParameter("id", orderEntity.getId())
                    .executeUpdate();

            entityManager.flush();
            entityManager.clear();

            List<OrderProduct> orderProductList = new ArrayList<>();

            orderProductList.add(OrderProduct.builder()
                    .orderId(orderEntity.getId())
                    .productId(savedProduct.getId())
                    .quantity(1)
                    .price(10000L)
                    .build());

            orderProductRepository.saveAll(orderProductList);

            // when
            orderCancelUseCase.execute();

            // then
            Order updatedOrder = orderRepository.findById(orderEntity.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

            Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("주문 취소 동시성 테스트")
    class ConcurrencyControlTest {
        @Test
        @DisplayName("동일한 상품에 대한 동시 주문 취소 시 재고가 정확히 복원")
        void 동일_상품_동시_취소() throws InterruptedException {
            // given
            final int threadCount = 5;
            final int quantityPerOrder = 3;
            final int initialStock = 100;
            final int expectedFinalStock = initialStock + (threadCount * quantityPerOrder);

            LocalDateTime thirtyOneMinutesAgo = LocalDateTime.now().minusMinutes(31);

            Product product = Product.builder()
                    .name("테스트 상품")
                    .description("테스트 설명")
                    .price(10000L)
                    .stock(initialStock)
                    .build();

            Product savedProduct = productRepository.save(product);

            List<Long> orderIds = new ArrayList<>();
            Address address = new Address(
                    "기본 주소",
                    "상세 주소",
                    "12345"
            );

            for (int i = 0; i < threadCount; i++) {

                Order order = Order.builder()
                        .userId((long) i + 1)
                        .status(OrderStatus.PENDING)
                        .totalPrice(30000L)
                        .discountAmount(0L)
                        .finalPrice(30000L)
                        .shippingAddress(address)
                        .recipientNumber("010-1234-567" + i)
                        .version(0L)
                        .build();

                Order savedOrder = orderRepository.save(order);
                orderIds.add(savedOrder.getId());

                entityManager.createQuery(
                                "UPDATE OrderEntity o SET o.createdAt = :createdAt WHERE o.id = :id")
                        .setParameter("createdAt", thirtyOneMinutesAgo)
                        .setParameter("id", savedOrder.getId())
                        .executeUpdate();
            }

            List<OrderProduct> orderProducts = new ArrayList<>();
            for (Long orderId : orderIds) {
                OrderProduct orderProduct = OrderProduct.builder()
                        .orderId(orderId)
                        .productId(savedProduct.getId())
                        .quantity(quantityPerOrder)
                        .price(10000L)
                        .build();
                orderProducts.add(orderProduct);
            }

            orderProductRepository.saveAll(orderProducts);

            TestTransaction.flagForCommit();
            TestTransaction.end();

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completeLatch = new CountDownLatch(threadCount);
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        orderCancelUseCase.execute();
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        completeLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = completeLatch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(completed).isTrue();
            assertThat(exceptions).isEmpty();

            List<Order> allOrders = orderRepository.findAll();
            System.out.println("전체 주문 수: " + allOrders.size());

            long canceledCount = allOrders.stream()
                    .peek(order -> System.out.println("주문 ID: " + order.getId() + ", 상태: " + order.getStatus() + ", 생성일: " + order.getCreatedAt()))
                    .filter(order -> order.getStatus() == OrderStatus.CANCELED)
                    .count();

            System.out.println("취소된 주문 수: " + canceledCount);

            assertThat(canceledCount).isEqualTo(threadCount);

            Product finalProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
            System.out.println("최종 재고: " + finalProduct.getStock() + ", 예상 재고: " + expectedFinalStock);

            assertThat(finalProduct.getStock())
                    .as("초기 재고(%d) + 복원된 재고(%d * %d) = %d",
                            initialStock, threadCount, quantityPerOrder, expectedFinalStock)
                    .isEqualTo(expectedFinalStock);
        }

        @Test
        @DisplayName("여러 상품의 동시 주문 취소 시 각 상품별 재고가 정확히 복원")
        void 여러_상품_동시_취소() throws InterruptedException {
            // given
            final int threadCount = 3;
            final int productCount = 3;
            LocalDateTime thirtyOneMinutesAgo = LocalDateTime.now().minusMinutes(31);

            List<Product> products = new ArrayList<>();
            for (int i = 0; i < productCount; i++) {
                Product product = Product.builder()
                        .name("테스트 상품" + (i + 1))
                        .description("테스트 설명" + (i + 1))
                        .price(10000L * (i + 1))
                        .stock(50)
                        .build();
                products.add(product);
            }

            List<Product> savedProducts = productRepository.saveAll(products);

            List<Order> orders = new ArrayList<>();
            List<OrderProduct> orderProducts = new ArrayList<>();
            Address address = new Address(
                    "기본 주소",
                    "상세 주소",
                    "12345"
            );

            for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                Order order = Order.builder()
                        .userId((long) (threadIndex + 1))
                        .status(OrderStatus.PENDING)
                        .totalPrice(60000L)
                        .discountAmount(0L)
                        .finalPrice(60000L)
                        .shippingAddress(address)
                        .recipientNumber("test-phone" + threadIndex)
                        .build();

                Order savedOrder = orderRepository.save(order);
                orders.add(savedOrder);

                entityManager.createQuery(
                                "UPDATE OrderEntity o SET o.createdAt = :createdAt WHERE o.id = :id")
                        .setParameter("createdAt", thirtyOneMinutesAgo)
                        .setParameter("id", savedOrder.getId())
                        .executeUpdate();
            }

            List<Order> savedOrders = orderRepository.saveAll(orders);

            for (int orderIndex = 0; orderIndex < savedOrders.size(); orderIndex++) {
                Order savedOrder = savedOrders.get(orderIndex);

                for (int productIndex = 0; productIndex < savedProducts.size(); productIndex++) {
                    Product savedProduct = savedProducts.get(productIndex);

                    OrderProduct orderProduct = OrderProduct.builder()
                            .orderId(savedOrder.getId())
                            .productId(savedProduct.getId())
                            .quantity(2)
                            .price(savedProduct.getPrice())
                            .build();

                    orderProducts.add(orderProduct);
                }
            }

            orderProductRepository.saveAll(orderProducts);

            for (Product product : savedProducts) {
                product.decreaseStock(6);
                productRepository.save(product);
            }

            TestTransaction.flagForCommit();
            TestTransaction.end();

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        orderCancelUseCase.execute();
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
            assertThat(exceptions).isEmpty();

            List<Order> canceledOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.CANCELED)
                    .toList();
            assertThat(canceledOrders).hasSize(threadCount);

            for (Product originalProduct : savedProducts) {
                Product finalProduct = productRepository.findById(originalProduct.getId()).orElseThrow();
                assertThat(finalProduct.getStock()).isEqualTo(50);
            }
        }

    }
}
