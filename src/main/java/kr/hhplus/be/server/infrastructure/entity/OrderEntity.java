package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class OrderEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long userCouponId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Long totalPrice;

    @Column
    private Long discountAmount;

    @Column(nullable = false)
    private Long finalPrice;

    @Column(nullable = false)
    private String shippingAddress1;

    @Column(nullable = false)
    private String shippingAddress2;

    @Column(nullable = false)
    private String shippingZipCode;

    @Column(nullable = false)
    private String recipientNumber;

    @Version
    private Long version;

    @Builder
    public OrderEntity(Long id, Long userId, Long userCouponId, OrderStatus status, Long totalPrice, Long discountAmount, Long finalPrice, String shippingAddress1, String shippingAddress2, String shippingZipCode, String recipientNumber, Long version) {
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
        this.version = version;
    }
}
