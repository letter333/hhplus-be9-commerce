package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.entity.ProductEntity;
import kr.hhplus.be.server.infrastructure.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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
}
