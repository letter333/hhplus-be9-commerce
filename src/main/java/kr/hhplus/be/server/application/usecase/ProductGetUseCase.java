package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
    private final ProductRepository productRepository;


    @Cacheable(value = "product", key = "#id", unless = "#result == null")
    public Product execute(Long id) {
        log.info("===========================캐시 적용 안됨");
        if(id == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }

        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }
}
