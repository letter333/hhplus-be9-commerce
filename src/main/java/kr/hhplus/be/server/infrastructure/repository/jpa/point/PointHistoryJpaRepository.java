package kr.hhplus.be.server.infrastructure.repository.jpa.point;

import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.infrastructure.entity.PointHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryEntity, Long> {
    List<PointHistory> findByUserId(Long userId);
}
