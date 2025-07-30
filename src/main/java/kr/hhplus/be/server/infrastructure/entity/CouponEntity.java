package kr.hhplus.be.server.infrastructure.entity;

import kr.hhplus.be.server.domain.model.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponEntity extends BaseEntity{
    Long id;
    String name;
    CouponType type;
    Long discountAmount;
    Long discountPercentage;
    int quantity;
    int issuedQuantity;
    LocalDateTime expiredAt;

    @Builder
    public CouponEntity(Long id, String name, CouponType type, Long discountAmount, Long discountPercentage, int quantity, int issuedQuantity, LocalDateTime expiredAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
        this.quantity = quantity;
        this.issuedQuantity = issuedQuantity;
        this.expiredAt = expiredAt;
    }
}
