package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.domain.repository.ProductRankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductGetRankingUseCaseTest {
    @Mock
    private ProductRankingRepository productRankingRepository;

    @InjectMocks
    private ProductGetRankingUseCase productGetRankingUseCase;

    @Test
    @DisplayName("상위 N개 인기 상품 목록을 조회")
    void 랭킹_조회() {
        // given
        LocalDate startDate = LocalDate.of(2025, 8, 15);
        LocalDate endDate = LocalDate.of(2025, 8, 21);
        Long limit = 5L;

        List<ProductSummary> expectedRankings = List.of(
                new ProductSummary(1L, "상품 A", 100L, "설명", LocalDateTime.now()),
                new ProductSummary(2L, "상품 B", 200L, "설명", LocalDateTime.now())
        );

        given(productRankingRepository.getRanking(startDate, endDate, limit)).willReturn(expectedRankings);

        // when
        List<ProductSummary> actualRankings = productGetRankingUseCase.execute(startDate, endDate, limit);

        // then
        assertThat(actualRankings).isEqualTo(expectedRankings);
        then(productRankingRepository).should(times(1)).getRanking(startDate, endDate, limit);
    }

    @Test
    @DisplayName("조회 기간이 7일을 초과하면 예외가 발생")
    void 최대_조회_기간_초과() {
        // given
        LocalDate startDate = LocalDate.of(2025, 8, 1);
        LocalDate endDate = LocalDate.of(2025, 8, 9);
        Long limit = 5L;

        // when & then
        assertThatThrownBy(() -> productGetRankingUseCase.execute(startDate, endDate, limit))
                .isInstanceOf(IllegalArgumentException.class);

        then(productRankingRepository).should(never()).getRanking(any(), any(), any());
    }
}