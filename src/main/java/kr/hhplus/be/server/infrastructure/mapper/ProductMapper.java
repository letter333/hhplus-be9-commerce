package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.infrastructure.entity.ProductEntity;

public class ProductMapper {
    public static Product toProduct(ProductEntity productEntity) {
        return Product.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .description(productEntity.getDescription())
                .price(productEntity.getPrice())
                .stock(productEntity.getStock())
                .build();
    }

    public static ProductEntity toProductEntity(Product product) {
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}
