package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.domain.model.Order;

import java.time.LocalDate;
import java.util.List;

public interface ProductRankingRepository {
    void updateRanking(Order order);
    List<ProductSummary> getRanking(LocalDate startDate, LocalDate endDate, Long limit);
}
