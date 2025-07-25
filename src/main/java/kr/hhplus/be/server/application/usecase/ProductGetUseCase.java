package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
    private final ProductRepository productRepository;

    public Product execute(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }

        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }
}
