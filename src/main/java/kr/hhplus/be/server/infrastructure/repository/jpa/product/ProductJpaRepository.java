package kr.hhplus.be.server.infrastructure.repository.jpa.product;

import kr.hhplus.be.server.infrastructure.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
}
