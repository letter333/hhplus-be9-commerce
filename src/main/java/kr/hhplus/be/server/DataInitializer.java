package kr.hhplus.be.server;

import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Profile("local")
public class DataInitializer implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final PaymentRepository paymentRepository;

    private static final int BATCH_SIZE = 1_000;
    private static final long TOTAL_COUNT = 1_000_000;

    @Override
    public void run(String... args) {
//        System.out.println("병렬 시딩 시작");
//
//        CompletableFuture<Void> p1 = CompletableFuture.runAsync(this::insertProducts);
//        CompletableFuture<Void> p2 = CompletableFuture.runAsync(this::insertOrders)
//                .thenRun(this::insertPayments);
//
//        CompletableFuture.allOf(p1, p2).join();
//        System.out.println("전체 시딩 완료");
    }

    private void insertProducts() {
        List<Product> bulk = new ArrayList<>();
        for (long i = 1; i <= TOTAL_COUNT; i++) {
            bulk.add(Product.builder()
                    .name("상품" + i)
                    .description("상품 설명")
                    .price(1_000L + i)
                    .stock(100)
                    .build());

            if (bulk.size() == BATCH_SIZE) {
                productRepository.saveAll(bulk);
                bulk.clear();
                log("Product", i);
            }
        }
        if (!bulk.isEmpty()) productRepository.saveAll(bulk);
    }

    private void insertOrders() {
        Address address = new Address(
                "기본 주소",
                "상세 주소",
                "12345"
        );

        List<Order> bulk = new ArrayList<>();
        for (long i = 1; i <= TOTAL_COUNT; i++) {
            bulk.add(Order.builder()
                    .userId(1L + (i % 1000)) // 1~1000 사이의 사용자 ID 순환
                    .userCouponId(null) // 쿠폰 미사용
                    .status(OrderStatus.PAID)
                    .totalPrice(10_000L + i)
                    .discountAmount(0L)
                    .finalPrice(10_000L + i)
                    .shippingAddress(address) // 배송 주소는 null로 설정
                    .recipientNumber("010-1234-5678")
                    .build());

            if (bulk.size() == BATCH_SIZE) {
                orderRepository.saveAll(bulk);
                bulk.clear();
                log("Order", i);
            }
        }
        if (!bulk.isEmpty()) orderRepository.saveAll(bulk);

        // 주문 생성 완료 후 OrderProduct 생성
        insertOrderProducts();
    }

    private void insertOrderProducts() {
        List<OrderProduct> bulk = new ArrayList<>();
        long productCounter = 0;

        for (long orderId = 1; orderId <= TOTAL_COUNT; orderId++) {
            // 각 주문당 1~3개의 상품을 랜덤하게 추가
            int productCount = (int) (1 + (orderId % 3));

            for (int j = 0; j < productCount; j++) {
                productCounter++;
                long productId = 1 + (productCounter % TOTAL_COUNT); // 상품 ID 순환

                bulk.add(OrderProduct.builder()
                        .orderId(orderId)
                        .productId(productId)
                        .quantity(1 + (int) (productCounter % 5)) // 1~5개 수량
                        .price(1_000L + productId)
                        .build());

                if (bulk.size() == BATCH_SIZE) {
                    orderProductRepository.saveAll(bulk);
                    bulk.clear();
                    log("OrderProduct", productCounter);
                }
            }
        }
        if (!bulk.isEmpty()) orderProductRepository.saveAll(bulk);
    }

    private void insertPayments() {
        List<Payment> bulk = new ArrayList<>();
        for (long i = 1; i <= TOTAL_COUNT; i++) {
            bulk.add(Payment.builder()
                    .orderId(i)
                    .paymentMethod(i % 2 == 0 ? PaymentMethod.CARD : PaymentMethod.POINT)
                    .amount(10_000L + i)
                    .status(PaymentStatus.SUCCESS)
                    .build());

            if (bulk.size() == BATCH_SIZE) {
                paymentRepository.saveAll(bulk);
                bulk.clear();
                log("Payment", i);
            }
        }
        if (!bulk.isEmpty()) {
            paymentRepository.saveAll(bulk);
        }
    }

    private void log(String table, long count) {
        if (count % (BATCH_SIZE * 10) == 0) {
            System.out.printf("%s: %,d건 저장 완료%n", table, count);
        }
    }
}

