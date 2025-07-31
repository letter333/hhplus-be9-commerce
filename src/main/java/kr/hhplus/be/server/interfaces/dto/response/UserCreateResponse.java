package kr.hhplus.be.server.interfaces.dto.response;

public record UserCreateResponse(
        Long id,
        String name,
        String phoneNumber
) {
}
