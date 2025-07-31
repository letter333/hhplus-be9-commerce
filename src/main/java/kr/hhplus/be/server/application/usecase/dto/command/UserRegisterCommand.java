package kr.hhplus.be.server.application.usecase.dto.command;

public record UserRegisterCommand(
        String name,
        String phoneNumber
) {
}
