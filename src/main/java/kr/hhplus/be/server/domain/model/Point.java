package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Point {
    private Long id;
    private Long userId;
    private Long amount;

    public static Long MAX_AMOUNT_PER_CHARGE = 1_000_000L;
    public static Long MAX_TOTAL_AMOUNT = 10_000_000L;
    public static Long MIN_AMOUNT_PER_USE = 1L;


    @Builder
    public Point(Long id, Long userId, Long amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
    }

    public void charge(Long chargeAmount) {
        validationCharge(chargeAmount);
        this.amount = this.amount + chargeAmount;
    }

    public void use(Long useAmount) {
        validationUse(useAmount);
        this.amount = this.amount - useAmount;
    }

    public void validationCharge(Long chargeAmount) {
        if(chargeAmount > MAX_AMOUNT_PER_CHARGE || chargeAmount <= 0 || this.amount + chargeAmount > MAX_TOTAL_AMOUNT) {
            throw new IllegalArgumentException("유효하지 않은 입력값 입니다.");
        }
    }

    public void validationUse(Long useAmount) {
        if(useAmount < MIN_AMOUNT_PER_USE || useAmount > this.amount) {
            throw new IllegalArgumentException("유효하지 않은 입력값 입니다.");
        }
    }
}
