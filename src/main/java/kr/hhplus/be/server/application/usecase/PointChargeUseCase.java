package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.domain.model.PointHistoryType;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointChargeUseCase {
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;


    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            noRetryFor = {IllegalArgumentException.class},
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    @Transactional
    public Point execute(Long userId, Long amount) {
        validateInputs(userId, amount);

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
    }

    private void validateInputs(Long userId, Long amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("충전 금액은 필수입니다.");
        }
    }

    @Recover
    public Point recoverFromOptimisticLockFailure(ObjectOptimisticLockingFailureException ex,
                                                  Long userId, Long amount) {
        log.error("포인트 충전 최종 실패 - 낙관적 락 충돌로 인한 재시도 한계 도달", ex);
        log.error("실패한 요청 정보 - 사용자 ID: {}, 충전 금액: {}", userId, amount);
        log.error("오류 메시지: {}", ex.getMessage());
        log.error("충돌 발생 엔티티: {}", ex.getPersistentClassName());

        throw new RuntimeException(
                String.format("포인트 충전 중 동시성 충돌이 발생하여 재시도에 실패했습니다. " +
                        "사용자 ID: %d, 충전 금액: %d원", userId, amount), ex);
    }

    @Recover
    public Point recoverFromIllegalArgument(IllegalArgumentException ex,
                                            Long userId, Long amount) {
        throw ex;
    }

    @Recover
    public Point recoverFromAnyException(Exception ex, Long userId, Long amount) {
        log.error("포인트 충전 중 예상치 못한 오류 발생", ex);
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new RuntimeException(ex);
    }
}