package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.domain.model.PointHistoryType;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointChargeUseCase {
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public Point execute(Long userId, Long amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("충전 금액은 필수입니다.");
        }

        Point point = pointRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        point.charge(amount);

        PointHistory pointHistory = PointHistory.builder()
                .userId(userId)
                .type(PointHistoryType.CHARGE)
                .amount(amount)
                .balanceAfter(point.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryRepository.save(pointHistory);

        return pointRepository.save(point);
    }
}