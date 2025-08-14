package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.OrderCreateCommand;
import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCreateUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final TransactionTemplate transactionTemplate;
    private final RedissonLockManager redissonLockManager;

    public Order execute(OrderCreateCommand command) {
        List<Long> sortedProductIds = command.orderProductList().stream()
                .map(OrderProduct::getProductId)
                .distinct()
                .sorted()
                .toList();

        List<RLock> locks = sortedProductIds.stream()
                .map(id -> redissonLockManager.getLock("productLock:" + id))
                .toList();
        RLock multilock = redissonLockManager.getMultiLock(locks);
        boolean isLocked = false;

        try {
            isLocked = multilock.tryLock(3, 3, java.util.concurrent.TimeUnit.SECONDS);
            if(!isLocked) {
                throw new IllegalArgumentException("주문 처리를 위한 락 획득에 실패했습니다.");
            }

            return transactionTemplate.execute(status -> {
                List<Product> products = productRepository.findByIdsWithPessimisticLock(sortedProductIds);

                if (products.size() != sortedProductIds.size()) {
                    throw new IllegalArgumentException("존재하지 않는 상품이 포함되어 있습니다.");
                }

                UserCoupon userCoupon = null;
                Long totalPrice = 0L;
                Long finalPrice;

                Map<Long, Product> productMap = products.stream()
                        .collect(Collectors.toMap(Product::getId, Function.identity()));

                for (OrderProduct orderProduct : command.orderProductList()) {
                    Product product = products.stream()
                            .filter(p -> p.getId().equals(orderProduct.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

                    product.decreaseStock(orderProduct.getQuantity());
                    totalPrice += product.getPrice() * orderProduct.getQuantity();
                }

                productRepository.saveAll(products);

                if (command.userCouponId() != null && command.userCouponId() > 0) {
                    userCoupon = userCouponRepository.findById(command.userCouponId())
                            .orElseThrow(() -> new IllegalArgumentException("잘못된 쿠폰입니다."));
                    Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
                            .orElseThrow(() -> new IllegalArgumentException("잘못된 쿠폰입니다."));

                    finalPrice = userCoupon.calculateDiscount(coupon.getType(), coupon.getDiscountAmount(), totalPrice);
                    userCoupon.use();
                    userCouponRepository.save(userCoupon);
                } else {
                    finalPrice = totalPrice;
                }

                Order order = Order.builder()
                        .userId(command.userId())
                        .status(OrderStatus.PENDING)
                        .orderProducts(command.orderProductList())
                        .totalPrice(totalPrice)
                        .finalPrice(finalPrice)
                        .shippingAddress(command.shippingAddress())
                        .recipientNumber(command.recipientNumber())
                        .build();

                Order savedOrder = orderRepository.save(order);

                List<OrderProduct> orderProducts = command.orderProductList().stream()
                        .map(orderProduct -> {
                            Product product = productMap.get(orderProduct.getProductId());
                            return OrderProduct.builder()
                                    .orderId(savedOrder.getId())
                                    .productId(orderProduct.getProductId())
                                    .quantity(orderProduct.getQuantity())
                                    .price(product.getPrice())
                                    .build();
                        })
                        .toList();

                List<OrderProduct> savedOrderProducts = orderProductRepository.saveAll(orderProducts);

                return savedOrder;
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("주문 처리 중 문제가 발생했습니다.");
        } finally {
            if(isLocked) {
                multilock.unlock();
            }
        }
    }
}
