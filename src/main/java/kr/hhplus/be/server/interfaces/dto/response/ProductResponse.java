package kr.hhplus.be.server.interfaces.dto.response;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Long price,
        int stock
) {
}
