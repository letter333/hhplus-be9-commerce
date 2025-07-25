package kr.hhplus.be.server.infrastructure.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity extends BaseEntity {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private int stock;

    @Builder
    public ProductEntity(Long id, String name, String description, Long price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }
}
