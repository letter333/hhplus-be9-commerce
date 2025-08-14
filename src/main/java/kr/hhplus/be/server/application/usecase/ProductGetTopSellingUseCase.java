package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.TopSellingProduct;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.repository.jpa.product.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductGetTopSellingUseCase {
    private final ProductRepository productRepository;
    private static final Long POPULAR_LIMIT = 5L;
    private static final int RECENT_DAYS = 3;

    public List<TopSellingProduct> execute() {
        LocalDateTime threeDaysAgoAtMidnight = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(RECENT_DAYS);

        List<ProductJpaRepository.TopSellingProductView> topProducts = productRepository.findTopSellingProducts(threeDaysAgoAtMidnight, POPULAR_LIMIT);

        return topProducts.stream()
                .map(product -> new TopSellingProduct(
                        product.getProductId(),
                        product.getProductName(),
                        product.getTotalQuantity(),
                        product.getSalesRank()
                )).toList();
    }
}
