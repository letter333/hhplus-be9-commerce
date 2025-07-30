package kr.hhplus.be.server.interfaces.dto.response;

import kr.hhplus.be.server.domain.model.Product;

import java.util.List;

public record ProductListResponse(
        List<Product> productList
) {
}
