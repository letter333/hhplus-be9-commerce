package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.ProductRegisterCommand;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRegisterUseCase {
    private final ProductRepository productRepository;

    @CacheEvict(value = "products", key = "'all'")
    public Product execute(ProductRegisterCommand command) {
        Product product = Product.builder()
                .name(command.name())
                .description(command.description())
                .price(command.price())
                .stock(command.stock())
                .build();

        return productRepository.save(product);
    }
}
