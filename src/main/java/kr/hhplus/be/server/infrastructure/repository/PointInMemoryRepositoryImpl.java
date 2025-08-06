package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.infrastructure.entity.PointEntity;
import kr.hhplus.be.server.infrastructure.mapper.PointMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PointInMemoryRepositoryImpl implements PointRepository {
    private final Map<Long, PointEntity> table = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public PointInMemoryRepositoryImpl() {
        initTable();
    }

    public void initTable() {
        if(table.isEmpty()) {
            table.put(1L, PointEntity.builder()
                    .id(1L)
                    .userId(1L)
                    .balance(1000L)
                    .build());
        }
    }

    @Override
    public Point save(Point point) {
        PointEntity pointEntity = PointMapper.toPointEntity(point);
        PointEntity savedEntity;

        if(pointEntity.getId() == null || table.get(pointEntity.getId()) == null) {
            Long newId = idGenerator.incrementAndGet();
            savedEntity = PointEntity.builder()
                    .id(newId)
                    .userId(pointEntity.getUserId())
                    .balance(pointEntity.getBalance())
                    .build();

            table.put(newId, savedEntity);
        } else {
            table.put(pointEntity.getId(), pointEntity);
            savedEntity = pointEntity;
        }
        return PointMapper.toPoint(savedEntity);
    }

    @Override
    public Optional<Point> findByUserId(Long userId) {
        return table.values().stream()
                .filter(pointEntity -> pointEntity.getUserId().equals(userId))
                .findFirst()
                .map(PointMapper::toPoint);
    }

    @Override
    public void deleteAll() {

    }
}
