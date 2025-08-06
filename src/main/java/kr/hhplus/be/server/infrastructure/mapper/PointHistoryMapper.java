package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.infrastructure.entity.PointHistoryEntity;

public class PointHistoryMapper {
    public static PointHistoryEntity toPointHistoryEntity(PointHistory pointHistory) {
        return PointHistoryEntity.builder()
                .id(pointHistory.getId())
                .userId(pointHistory.getUserId())
                .orderId(pointHistory.getOrderId())
                .type(pointHistory.getType())
                .amount(pointHistory.getAmount())
                .balanceAfter(pointHistory.getBalanceAfter())
                .build();
    }

    public static PointHistory toPointHistory(PointHistoryEntity pointHistoryEntity) {
        return PointHistory.builder()
                .id(pointHistoryEntity.getId())
                .userId(pointHistoryEntity.getUserId())
                .orderId(pointHistoryEntity.getOrderId())
                .type(pointHistoryEntity.getType())
                .amount(pointHistoryEntity.getAmount())
                .balanceAfter(pointHistoryEntity.getBalanceAfter())
                .createdAt(pointHistoryEntity.getCreatedAt())
                .build();
    }
}

