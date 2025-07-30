package kr.hhplus.be.server.interfaces.mapper;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.interfaces.dto.response.ProductResponse;

public class ProductResponseMapper {
    public static ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }
}
