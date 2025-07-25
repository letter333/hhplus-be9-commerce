package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Point;

import java.util.Optional;

public interface PointRepository {
    Point save(Point point);
    Optional<Point> findByUserId(Long userId);
}
