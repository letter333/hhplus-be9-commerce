package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Coupon {
    Long id;
    String name;
    CouponType type;
    Long discountAmount;
    Long discountPercentage;
    int quantity;
    int issuedQuantity;
    LocalDateTime expiredAt;

    @Builder
    public Coupon(Long id, String name, CouponType type, Long discountAmount, Long discountPercentage, int quantity, int issuedQuantity, LocalDateTime expiredAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
        this.quantity = quantity;
        this.issuedQuantity = issuedQuantity;
        this.expiredAt = expiredAt;
    }

    public void issue() {
        validateIssue();
        this.issuedQuantity++;
    }

    public void validateIssue() {
        if(this.expiredAt != null && LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new IllegalArgumentException("이미 만료된 쿠폰입니다.");
        }

        if(this.quantity - this.issuedQuantity <= 0) {
            throw new IllegalArgumentException("발급 가능한 쿠폰이 없습니다.");
        }
    }
}
