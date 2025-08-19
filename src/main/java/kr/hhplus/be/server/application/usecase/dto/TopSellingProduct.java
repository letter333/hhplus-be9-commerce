package kr.hhplus.be.server.application.usecase.dto;

public record TopSellingProduct(
        Long productId,
        String productName,
        Long totalQuantity,
        Long salesRank
) {
}
