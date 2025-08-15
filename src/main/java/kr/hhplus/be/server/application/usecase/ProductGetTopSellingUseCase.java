package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.TopSellingProduct;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.repository.jpa.product.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductGetTopSellingUseCase {
    private final ProductRepository productRepository;
    private static final Long POPULAR_LIMIT = 5L;
    private static final int RECENT_DAYS = 3;

    @Cacheable(value = "topSellingProducts",
            key = "'topSellingProducts:' + T(java.time.LocalDate).now().toString()",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<TopSellingProduct> execute() {
        log.info("-------------------캐시 처리 안됨");
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
