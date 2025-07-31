package kr.hhplus.be.server.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointHistory {
    private Long id;
    private Long userId;
    private Long orderId;
    private PointHistoryType type;
    private Long amount;
    private Long balanceAfter;
    private LocalDateTime createdAt;

    @Builder
    public PointHistory(Long id, Long userId, Long orderId, PointHistoryType type, Long amount, Long balanceAfter, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }
}
