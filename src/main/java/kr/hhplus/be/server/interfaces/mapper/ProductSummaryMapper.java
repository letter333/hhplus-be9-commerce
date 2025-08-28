package kr.hhplus.be.server.interfaces.mapper;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.interfaces.dto.ProductSummaryResponse;

public class ProductSummaryMapper {
    public static ProductSummaryResponse toProductSummaryResponse(ProductSummary productSummary) {
        return new ProductSummaryResponse(
                productSummary.id(),
                productSummary.name(),
                productSummary.price(),
                productSummary.description()
        );
    }
}
