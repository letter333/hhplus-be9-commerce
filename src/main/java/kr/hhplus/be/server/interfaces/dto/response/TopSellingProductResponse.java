package kr.hhplus.be.server.interfaces.dto.response;

public record TopSellingProductResponse(
        Long productId,
        String productName,
        Long totalQuantity,
        Long salesRank
) {
}
