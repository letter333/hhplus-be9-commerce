package kr.hhplus.be.server.application.usecase.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record ProductSummary(
        Long id,
        String name,
        Long price,
        String description,
        LocalDateTime updatedAt
) implements Serializable {}
