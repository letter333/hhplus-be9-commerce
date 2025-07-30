package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
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
    private ZonedDateTime createdAt;

    @Builder
    public Order(Long id, Long userId, OrderStatus status, List<OrderProduct> orderProducts, Long totalPrice, Long discountAmount, Long finalPrice, Address shippingAddress, String recipientNumber, ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.orderProducts = orderProducts;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.shippingAddress = shippingAddress;
        this.recipientNumber = recipientNumber;
        this.createdAt = createdAt;
    }

    public void cancel() {
        if(this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태인 주문만 취소할 수 있습니다.");
        }
        this.status = OrderStatus.CANCELED;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
