package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCancelUseCase {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final TransactionTemplate transactionTemplate;
    private final RedissonLockManager redissonLockManager;
    private static final int CANCEL_MINUTE = 30;

    public void execute() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(CANCEL_MINUTE);
        List<Order> targetOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, threshold);

        if(targetOrders.isEmpty()) {
            return;
        }

        List<Long> orderIds = targetOrders.stream().map(Order::getId).toList();
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderIdIn(orderIds);

        Map<Long, Integer> stockRestoreMap = orderProducts.stream()
                .collect(Collectors.groupingBy(OrderProduct::getProductId, Collectors.summingInt(OrderProduct::getQuantity)));

        List<Long> sortedProductIds = stockRestoreMap.keySet().stream().sorted().toList();

        List<RLock> productLocks = sortedProductIds.stream()
                .map(productId -> redissonLockManager.getLock("productLock:" + productId))
                .toList();
        RLock multilock = redissonLockManager.getMultiLock(productLocks);
        boolean isLocked = false;

        try {
            isLocked = multilock.tryLock(0, 10, TimeUnit.MINUTES);
            if(!isLocked) {
                return;
            }

            transactionTemplate.executeWithoutResult(status -> {


                List<Product> productsToRestore = productRepository.findByIdsWithPessimisticLock(sortedProductIds);

                productsToRestore.forEach(product -> {
                    int quantity = stockRestoreMap.get(product.getId());
                    product.increaseStock(quantity);
                });
                productRepository.saveAll(productsToRestore);

                targetOrders.forEach(Order::cancel);
                orderRepository.saveAll(targetOrders);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if(isLocked) {
                multilock.unlock();
            }
        }
    }
}
