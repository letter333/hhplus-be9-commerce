package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Order {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private List<OrderProduct> orderProducts;
    private Long totalPrice;
    private Long discountAmount;
    private Long finalPrice;
    private Address shippingAddress;
    private String recipientNumber;

    @Builder
    public Order(Long id, Long userId, OrderStatus status, List<OrderProduct> orderProducts, Long totalPrice, Long discountPrice, Long finalPrice, Address shippingAddress, String recipientNumber) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.orderProducts = orderProducts;
        this.totalPrice = totalPrice;
        this.discountAmount = discountPrice;
        this.finalPrice = finalPrice;
        this.shippingAddress = shippingAddress;
        this.recipientNumber = recipientNumber;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
