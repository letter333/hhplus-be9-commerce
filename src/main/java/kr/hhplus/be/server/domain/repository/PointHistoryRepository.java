package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.PointHistory;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);
    List<PointHistory> findByUserId(Long userId);
    void deleteAll();
}
