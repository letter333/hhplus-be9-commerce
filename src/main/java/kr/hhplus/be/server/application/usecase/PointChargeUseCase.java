package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointChargeUseCase {
    private final PointRepository pointRepository;

    @Transactional
    public Point execute(Long userId, Long amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("충전 금액은 필수입니다.");
        }

        Point point = pointRepository.findByUserId(userId).orElse(Point.builder()
                .userId(userId)
                .amount(0L)
                .build());

        point.charge(amount);

        return pointRepository.save(point);
    }
}