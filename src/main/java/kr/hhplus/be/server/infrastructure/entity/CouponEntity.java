package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.model.CouponType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor
public class CouponEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 20)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    CouponType type;

    @Column
    Long discountAmount;

    @Column(nullable = false)
    int quantity;

    @Column
    int issuedQuantity;

    @Column(nullable = false)
    LocalDateTime expiredAt;

    @Builder
    public CouponEntity(Long id, String name, CouponType type, Long discountAmount, int quantity, int issuedQuantity, LocalDateTime expiredAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.discountAmount = discountAmount;
        this.quantity = quantity;
        this.issuedQuantity = issuedQuantity;
        this.expiredAt = expiredAt;
    }
}
