package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.entity.ProductEntity;
import kr.hhplus.be.server.infrastructure.mapper.ProductMapper;
import kr.hhplus.be.server.infrastructure.repository.jpa.product.ProductJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ProductInMemoryRepositoryImpl implements ProductRepository {
    private final Map<Long, ProductEntity> table = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public ProductInMemoryRepositoryImpl() {
        initTable();
    }

    private void initTable() {
        table.put(1L, ProductEntity.builder()
                .id(1L)
                .name("츄르")
                .description("고양이 간식")
                .price(1000L)
                .stock(50)
                .build());

        table.put(2L, ProductEntity.builder()
                .id(2L)
                .name("습식 사료")
                .description("고양이 밥")
                .price(20_000L)
                .stock(30)
                .build());

        table.put(3L, ProductEntity.builder()
                .id(3L)
                .name("화장실 모래")
                .description("화장실에 쓰는 모래")
                .price(15_000L)
                .stock(55)
                .build());

        idGenerator.set(3L);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(table.get(id))
                .map(ProductMapper::toProduct);
    }

    @Override
    public Optional<Product> findByIdWithLock(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Product> findByIdsWithPessimisticLock(List<Long> ids) {
        return List.of();
    }

    @Override
    public List<Product> findAll() {
        return table.values().stream()
                .map(ProductMapper::toProduct)
                .collect(Collectors.toList());
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = ProductMapper.toProductEntity(product);
        ProductEntity savedEntity;

        if(productEntity.getId() == null || table.get(productEntity.getId()) == null) {
            Long newId = idGenerator.incrementAndGet();
            savedEntity = ProductEntity.builder()
                    .id(newId)
                    .name(productEntity.getName())
                    .description(productEntity.getDescription())
                    .price(productEntity.getPrice())
                    .stock(productEntity.getStock())
                    .build();

            table.put(newId, savedEntity);
        } else {
            table.put(productEntity.getId(), productEntity);
            savedEntity = productEntity;
        }

        return ProductMapper.toProduct(savedEntity);
    }

    @Override
    public List<Product> saveAll(List<Product> productList) {
        if (productList == null || productList.isEmpty()) {
            return List.of();
        }

        return productList.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAllByIdIn(List<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(table::get)
                .filter(productEntity -> productEntity != null)
                .map(ProductMapper::toProduct)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductJpaRepository.TopSellingProductView> findTopSellingProducts(LocalDateTime threeDaysAgoAtMidnight, Long limit) {
        return List.of();
    }

    @Override
    public void deleteAll() {

    }
}
