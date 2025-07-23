package kr.hhplus.be.server.domain.model;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Point 도메인 테스트")
class PointTest {
    @Nested
    @DisplayName("포인트 충전 시")
    class ChargePoint {
        @Test
        @DisplayName("포인트 충전 성공")
        void 포인트_충전() {
            //given
            Point point = new Point(1L, 1L, 10000L);
            Long chargeAmount = 10000L;
            Long expectedAmount = 20000L;

            //when
            point.charge(chargeAmount);

            //then
            assertThat(point.getAmount()).isEqualTo(expectedAmount);
        }

        @ParameterizedTest()
        @ValueSource(longs = {0, -1, 1_000_001L, 10_000_001L})
        @DisplayName("유효하지 않은 입력값이면 예외 처리")
        void 유효하지_않은_입력값(Long chargeAmount) {
            //given
            Point point = new Point(1L, 1L, 0L);

            //when & then
            assertThatThrownBy(() -> point.charge(chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("포인트 사용 시")
    class UsePoint {
        @Test
        @DisplayName("포인트 사용 성공")
        void 포인트_사용() {
            //given
            Point point = new Point(1L, 1L, 10000L);
            Long useAmount = 10000L;
            Long expectedAmount = 0L;

            //when
            point.use(useAmount);

            //then
            assertThat(point.getAmount()).isEqualTo(expectedAmount);
        }

        @ParameterizedTest()
        @ValueSource(longs = {0L, 10_000_000L})
        @DisplayName("유효하지 않은 입력값이면 예외 처리")
        void 유효하지_않은_입력값(Long useAmount) {
            //given
            Point point = new Point(1L, 1L, 10000L);

            //when & then
            assertThatThrownBy(() -> point.use(useAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}