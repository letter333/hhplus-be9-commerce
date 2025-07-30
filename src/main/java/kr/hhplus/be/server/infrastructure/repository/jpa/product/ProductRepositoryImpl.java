package kr.hhplus.be.server.infrastructure.repository.jpa.product;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.entity.ProductEntity;
import kr.hhplus.be.server.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        ProductEntity productEntity = productJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 ID 입니다."));
        return Optional.ofNullable(ProductMapper.toProduct(productEntity));
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll()
                .stream()
                .map(ProductMapper::toProduct)
                .toList();
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = ProductMapper.toProductEntity(product);
        ProductEntity savedEntity = productJpaRepository.save(productEntity);

        return ProductMapper.toProduct(savedEntity);
    }

    @Override
    public List<Product> saveAll(List<Product> productList) {
        List<ProductEntity> productEntities = productList.stream()
                .map(ProductMapper::toProductEntity)
                .toList();

        List<ProductEntity> savedEntities = productJpaRepository.saveAll(productEntities);

        return savedEntities.stream()
                .map(ProductMapper::toProduct)
                .toList();
    }

    @Override
    public List<Product> findAllByIdIn(List<Long> ids) {
        return productJpaRepository.findAllById(ids)
                .stream()
                .map(ProductMapper::toProduct)
                .toList();
    }
}
