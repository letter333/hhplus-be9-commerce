package kr.hhplus.be.server.mock.product.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Long price,
        int stock
) {
}
