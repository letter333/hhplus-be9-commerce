package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductGetListUseCase {
    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "'all'")
    public List<Product> execute() {
        return productRepository.findAll();
    }
}
