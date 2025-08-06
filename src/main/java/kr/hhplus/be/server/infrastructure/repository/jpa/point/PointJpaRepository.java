package kr.hhplus.be.server.infrastructure.repository.jpa.point;

import kr.hhplus.be.server.infrastructure.entity.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {

    PointEntity findByUserId(Long userId);
}
