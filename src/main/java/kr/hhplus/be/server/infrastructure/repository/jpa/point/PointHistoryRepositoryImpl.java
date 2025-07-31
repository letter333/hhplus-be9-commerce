package kr.hhplus.be.server.infrastructure.repository.jpa.point;

import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.infrastructure.entity.PointHistoryEntity;
import kr.hhplus.be.server.infrastructure.mapper.PointHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        PointHistoryEntity pointHistoryEntity = PointHistoryMapper.toPointHistoryEntity(pointHistory);
        PointHistoryEntity savedEntity = pointHistoryJpaRepository.save(pointHistoryEntity);

        return PointHistoryMapper.toPointHistory(savedEntity);
    }

    @Override
    public List<PointHistory> findByUserId(Long userId) {
        return pointHistoryJpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteAll() {

    }
}
