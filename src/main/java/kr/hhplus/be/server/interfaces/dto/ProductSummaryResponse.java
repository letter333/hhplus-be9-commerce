package kr.hhplus.be.server.interfaces.dto;

public record ProductSummaryResponse(
        Long id,
        String name,
        Long price,
        String description
) {
}
