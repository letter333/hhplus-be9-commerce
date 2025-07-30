package kr.hhplus.be.server.infrastructure.repository.jpa.point;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.infrastructure.entity.PointEntity;
import kr.hhplus.be.server.infrastructure.mapper.PointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Point save(Point point) {
        PointEntity pointEntity = PointMapper.toPointEntity(point);
        PointEntity savedEntity = pointJpaRepository.save(pointEntity);

        return PointMapper.toPoint(savedEntity);
    }

    @Override
    public Optional<Point> findByUserId(Long userId) {
        PointEntity pointEntity = pointJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return Optional.ofNullable(PointMapper.toPoint(pointEntity));
    }
}
