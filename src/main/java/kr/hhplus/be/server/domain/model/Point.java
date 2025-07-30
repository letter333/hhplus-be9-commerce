package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Point {
    private Long id;
    private Long userId;
    private Long amount;

    private static final Long MAX_AMOUNT_PER_CHARGE = 1_000_000L;
    private static final Long MAX_TOTAL_AMOUNT = 10_000_000L;
    private static final Long MIN_AMOUNT_PER_CHARGE = 1L;
    private static final Long MIN_AMOUNT_PER_USE = 1L;


    @Builder
    public Point(Long id, Long userId, Long amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
    }

    public void charge(Long chargeAmount) {
        validateCharge(chargeAmount);
        this.amount = this.amount + chargeAmount;
    }

    public void use(Long useAmount) {
        validateUse(useAmount);
        this.amount = this.amount - useAmount;
    }

    private void validateCharge(Long chargeAmount) {
        if (chargeAmount == null) {
            throw new IllegalArgumentException("충전 금액은 필수입니다.");
        }

        if(chargeAmount > MAX_AMOUNT_PER_CHARGE) {
            throw new IllegalArgumentException(String.format("1회 충전 가능한 최대 금액은 %,d원입니다.", MAX_AMOUNT_PER_CHARGE));
        }

        if(chargeAmount <= 0) {
            throw new IllegalArgumentException(String.format("충전 금액은 %d원 이상이어야 합니다.", MIN_AMOUNT_PER_CHARGE));
        }

        if(this.amount + chargeAmount > MAX_TOTAL_AMOUNT) {
            throw new IllegalArgumentException(String.format("보유 가능한 최대 포인트는 %,d원입니다.", MAX_TOTAL_AMOUNT));
        }
    }

    private void validateUse(Long useAmount) {
        if(useAmount < MIN_AMOUNT_PER_USE) {
            throw new IllegalArgumentException(String.format("최소 사용 금액은 %d원입니다.", MIN_AMOUNT_PER_USE));
        }
        if(useAmount > this.amount) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
    }
}
