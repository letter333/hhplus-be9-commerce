package kr.hhplus.be.server.mock.order.dto;

public record OrderProductRequest(
    Long id,
    String name,
    String description,
    Long price,
    int stock
) {
}
