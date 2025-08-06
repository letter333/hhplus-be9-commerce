package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointChargeUseCase 테스트")
class PointChargeUseCaseTest {
    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointChargeUseCase pointChargeUseCase;

    @Test
    @DisplayName("포인트 충전 성공")
    void 포인트_충전() {
        //given
        Long userId = 1L;
        Long chargeAmount = 10000L;
        Point point = Point.builder()
                .id(1L)
                .userId(userId)
                .balance(0L)
                .build();

        Point savedPoint = point.builder()
                .id(1L)
                .userId(userId)
                .balance(chargeAmount)
                .build();

        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));
        when(pointRepository.save(point)).thenReturn(savedPoint);

        //when
        Point result = pointChargeUseCase.execute(userId, chargeAmount);

        //then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance()).isEqualTo(chargeAmount);

        verify(pointRepository).findByUserId(userId);
        verify(pointRepository).save(point);
    }

    @Test
    @DisplayName("사용자 ID가 null이면 예외 발생")
    void 사용자_ID_null() {
        // given
        Long userId = null;
        Long chargeAmount = 10000L;

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);

        verify(pointRepository, never()).findByUserId(any());
        verify(pointRepository, never()).save(any());
    }

    @Test
    @DisplayName("충전 금액이 null이면 예외 발생")
    void 충전_금액_null() {
        // given
        Long userId = 1L;
        Long chargeAmount = null;

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);

        verify(pointRepository, never()).findByUserId(any());
        verify(pointRepository, never()).save(any());
    }

    @Test
    @DisplayName("최대 충전 금액 초과 시 예외 발생")
    void 최대_충전_금액_초과() {
        // given
        Long userId = 1L;
        Long chargeAmount = 1_000_001L;
        Point existingPoint = Point.builder()
                .id(1L)
                .userId(userId)
                .balance(0L)
                .build();

        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(existingPoint));

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);

        verify(pointRepository).findByUserId(userId);
        verify(pointRepository, never()).save(any());
    }


    @Test
    @DisplayName("최대 보유 포인트 초과 시 예외 발생")
    void 최대_보유_포인트_초과() {
        // given
        Long userId = 1L;
        Long chargeAmount = 1_000_000L;
        Point existingPoint = Point.builder()
                .id(1L)
                .userId(userId)
                .balance(9_500_000L)
                .build();

        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(existingPoint));

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);

        verify(pointRepository).findByUserId(userId);
        verify(pointRepository, never()).save(any());
    }

    @Test
    @DisplayName("충전 금액이 0 이하일 때 예외 발생")
    void 충전_금액_0이하() {
        // given
        Long userId = 1L;
        Long chargeAmount = 0L;
        Point existingPoint = Point.builder()
                .id(1L)
                .userId(userId)
                .balance(1000L)
                .build();

        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(existingPoint));

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);

        verify(pointRepository).findByUserId(userId);
        verify(pointRepository, never()).save(any());
    }
}