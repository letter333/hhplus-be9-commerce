package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCancelUseCase {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private static final int CANCEL_MINUTE = 30;

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    @Transactional
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

        List<Product> productsToRestore = productRepository.findByIdsWithPessimisticLock(sortedProductIds);

        productsToRestore.forEach(product -> {
            int quantity = stockRestoreMap.get(product.getId());
            product.increaseStock(quantity);
        });
        productRepository.saveAll(productsToRestore);

        targetOrders.forEach(Order::cancel);
        orderRepository.saveAll(targetOrders);
    }
}
