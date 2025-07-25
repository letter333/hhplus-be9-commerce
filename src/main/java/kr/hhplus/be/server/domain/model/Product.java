package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Product {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private int stock;

    private static final int MIN_ORDER_AMOUNT = 1;
    private static final int MIN_INCREASE_AMOUNT = 1;

    @Builder
    public Product(Long id, String name, String description, Long price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void decreaseStock(int decreaseAmount) {
        validateDecreaseStock(decreaseAmount);
        this.stock -= decreaseAmount;
    }

    public void increaseStock(int increaseAmount) {
        validateIncreaseStock(increaseAmount);
        this.stock += increaseAmount;
    }

    private void validateDecreaseStock(int decreaseAmount) {
        if(decreaseAmount <= 0) {
            throw new IllegalArgumentException(String.format("주문 수량은 최소 %,d개 이상이어야 됩니다.", MIN_ORDER_AMOUNT));
        }

        if(decreaseAmount > this.stock) {
            throw new IllegalArgumentException(String.format("상품 재고보다 주문량이 많습니다. 현재 재고: %,d", this.stock));
        }
    }

    private void validateIncreaseStock(int increaseAmount) {
        if(increaseAmount <= 0) {
            throw new IllegalArgumentException(String.format("재고 최소 증가량은 %,d개 입니다.", MIN_INCREASE_AMOUNT));
        }
    }
}
