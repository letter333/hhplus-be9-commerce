package kr.hhplus.be.server.application.usecase.dto.command;

public record ProductRegisterCommand(
        String name,
        String description,
        Long price,
        int stock
) {
}
