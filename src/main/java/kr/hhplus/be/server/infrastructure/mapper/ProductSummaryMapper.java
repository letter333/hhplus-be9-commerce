package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.domain.model.Product;

public class ProductSummaryMapper {
    public static ProductSummary toProductSummary(Product product) {
        return ProductSummary.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }
}
