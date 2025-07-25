package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.OrderCreateCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCreateUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order execute(OrderCreateCommand request) {
        List<Product> toUpdateProducts = new ArrayList<>();
        Long totalPrice = 0L;

        for (OrderProduct orderProduct : request.orderProductList()) {
            Product product = productRepository.findById(orderProduct.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 상품입니다. 상품ID=" + orderProduct.getProductId()
                    ));

            product.decreaseStock(orderProduct.getQuantity());
            toUpdateProducts.add(product);
            totalPrice += product.getPrice() * orderProduct.getQuantity();
        }
        productRepository.saveAll(toUpdateProducts);

        Long discountAmount = 0L;
        Long finalPrice = totalPrice - discountAmount;

        Order order = Order.builder()
                .userId(request.userId())
                .status(OrderStatus.PENDING)
                .orderProducts(request.orderProductList())
                .totalPrice(totalPrice)
                .finalPrice(finalPrice)
                .shippingAddress(request.shippingAddress())
                .recipientNumber(request.recipientNumber())
                .build();

        return orderRepository.save(order);
    }
}
