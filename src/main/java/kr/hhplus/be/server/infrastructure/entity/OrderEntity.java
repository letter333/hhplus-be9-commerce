package kr.hhplus.be.server.infrastructure.entity;

import kr.hhplus.be.server.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderEntity extends BaseEntity {
    private Long id;
    private Long userId;
    private Long userCouponId;
    private OrderStatus status;
    private Long totalPrice;
    private Long discountAmount;
    private Long finalPrice;
    private String shippingAddress1;
    private String shippingAddress2;
    private String shippingZipCode;
    private String recipientNumber;

    @Builder
    public OrderEntity(Long id, Long userId, Long userCouponId, OrderStatus status, Long totalPrice, Long discountAmount, Long finalPrice, String shippingAddress1, String shippingAddress2, String shippingZipCode, String recipientNumber) {
        this.id = id;
        this.userId = userId;
        this.userCouponId = userCouponId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.shippingAddress1 = shippingAddress1;
        this.shippingAddress2 = shippingAddress2;
        this.shippingZipCode = shippingZipCode;
        this.recipientNumber = recipientNumber;
    }
}
