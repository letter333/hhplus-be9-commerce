package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.domain.model.PointHistoryType;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointChargeUseCase {
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final RedissonLockManager redissonLockManager;
    private final TransactionTemplate transactionTemplate;


    public Point execute(Long userId, Long amount) {
        validateInputs(userId, amount);

        String lockKey = "pointLock:" + userId;
        RLock lock = redissonLockManager.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(1, 1, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new IllegalStateException("포인트 처리를 위한 락 획득에 실패했습니다.");
            }

            return transactionTemplate.execute(status -> {
                Point point = pointRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

                point.charge(amount);

                PointHistory pointHistory = PointHistory.builder()
                        .userId(userId)
                        .type(PointHistoryType.CHARGE)
                        .amount(amount)
                        .balanceAfter(point.getBalance())
                        .createdAt(LocalDateTime.now())
                        .build();
                pointHistoryRepository.save(pointHistory);

                return pointRepository.save(point);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("포인트 충전 처리 중 문제가 발생했습니다.");
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void validateInputs(Long userId, Long amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("충전 금액은 필수입니다.");
        }
    }
}