package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderProduct {
    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private Long price;

    @Builder
    public OrderProduct(Long id, Long orderId, Long productId, int quantity, Long price) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}
