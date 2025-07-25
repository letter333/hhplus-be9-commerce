package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointGetUseCase {
    private final PointRepository pointRepository;

    public Point execute(Long userId) {
        return pointRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
