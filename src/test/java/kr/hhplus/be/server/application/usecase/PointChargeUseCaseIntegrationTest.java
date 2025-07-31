package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.repository.PointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class PointChargeUseCaseIntegrationTest {
    @Autowired
    private PointChargeUseCase pointChargeUseCase;

    @Autowired
    private PointRepository pointRepository;

    @Test
    @DisplayName("포인트 충전 성공")
    void 포인트_충전() {
        // given
        Long userId = 1L;
        Long initialBalance = 5000L;
        Long chargeAmount = 10000L;

        Point existingPoint = Point.builder()
                .userId(userId)
                .balance(initialBalance)
                .build();
        pointRepository.save(existingPoint);

        // when
        Point result = pointChargeUseCase.execute(userId, chargeAmount);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance()).isEqualTo(initialBalance + chargeAmount);

        Point savedPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(savedPoint.getBalance()).isEqualTo(initialBalance + chargeAmount);
    }

    @Test
    @DisplayName("충전 금액이 null일 때 예외 발생")
    void 충전_금액_null() {
        // given
        Long userId = 1L;
        Long chargeAmount = null;

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("충전 금액이 0 이하일 때 예외 발생")
    void 충전_금액_0이하() {
        // given
        Long userId = 1L;
        Long chargeAmount = 0L;

        Point existingPoint = Point.builder()
                .userId(userId)
                .balance(1000L)
                .build();
        pointRepository.save(existingPoint);

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("1회 최대 충전 가능 금액을 초과할 때 예외 발생")
    void 최대_충전금액_초과() {
        // given
        Long userId = 1L;
        Long chargeAmount = 1_000_001L;

        Point existingPoint = Point.builder()
                .userId(userId)
                .balance(1000L)
                .build();
        pointRepository.save(existingPoint);

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("충전 후 최대 보유 가능 포인트를 초과할 때 예외 발생")
    void 최대_보유포인트_초과_예외() {
        // given
        Long userId = 1L;
        Long chargeAmount = 1_000_000L;
        Long existingBalance = 9_500_000L;

        Point existingPoint = Point.builder()
                .userId(userId)
                .balance(existingBalance)
                .build();
        pointRepository.save(existingPoint);

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
