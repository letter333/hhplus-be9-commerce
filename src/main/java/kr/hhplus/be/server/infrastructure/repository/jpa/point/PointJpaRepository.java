package kr.hhplus.be.server.infrastructure.repository.jpa.point;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.infrastructure.entity.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {
    Optional<Point> findByUserId(Long userId);
}
