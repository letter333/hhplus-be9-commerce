package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.domain.repository.ProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductGetRankingUseCase {
    private final ProductRankingRepository productRankingRepository;

    public List<ProductSummary> execute(LocalDate startDate, LocalDate endDate, Long limit) {
        if (ChronoUnit.DAYS.between(startDate, endDate) >= 7) {
            throw new IllegalArgumentException("조회 기간은 최대 7일입니다.");
        }

        return productRankingRepository.getRanking(startDate, endDate, limit);
    }
}
