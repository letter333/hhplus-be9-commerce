package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.OrderCreateCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCreateUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT_SECONDS = 10L;

    @Transactional
    public Order execute(OrderCreateCommand command) {
        List<Long> sortedProductIds = command.orderProductList().stream()
                .map(OrderProduct::getProductId)
                .distinct()
                .sorted()
                .toList();

        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (Long productId : sortedProductIds) {
                ReentrantLock lock = lockMap.computeIfAbsent(productId, k -> new ReentrantLock(true));
                if (!lock.tryLock(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("주문 처리량이 많아 요청에 실패했습니다. 잠시 후 다시 시도해주세요.");
                }
                acquiredLocks.add(lock);
            }

            List<Product> toUpdateProducts = new ArrayList<>();
            Long totalPrice = 0L;

            for (OrderProduct orderProduct : command.orderProductList()) {
                Product product = productRepository.findById(orderProduct.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

                product.decreaseStock(orderProduct.getQuantity());
                toUpdateProducts.add(product);
                totalPrice += product.getPrice() * orderProduct.getQuantity();
            }
            productRepository.saveAll(toUpdateProducts);

            Long discountAmount = 0L;
            Long finalPrice = totalPrice - discountAmount;

            Order order = Order.builder()
                    .userId(command.userId())
                    .status(OrderStatus.PENDING)
                    .orderProducts(command.orderProductList())
                    .totalPrice(totalPrice)
                    .finalPrice(finalPrice)
                    .shippingAddress(command.shippingAddress())
                    .recipientNumber(command.recipientNumber())
                    .build();

            return orderRepository.save(order);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("주문 처리 중 문제가 발생했습니다.", e);
        } finally {
            for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                acquiredLocks.get(i).unlock();
            }
        }
    }
}
