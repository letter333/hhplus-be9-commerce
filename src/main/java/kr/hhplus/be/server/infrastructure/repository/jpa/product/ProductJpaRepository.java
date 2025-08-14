package kr.hhplus.be.server.infrastructure.repository.jpa.product;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    interface TopSellingProductView {
        Long getProductId();
        String getProductName();
        Long getTotalQuantity();
        Long getSalesRank();
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithPessimisticLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id IN :ids ORDER BY p.id")
    List<ProductEntity> findByIdsWithPessimisticLock(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT 
            p.id AS productId, 
            p.name AS productName, 
            SUM(op.quantity) AS totalQuantity,
            RANK() OVER (ORDER BY SUM(op.quantity) DESC) AS salesRank
        FROM products p
            INNER JOIN order_products op ON p.id = op.product_id
            INNER JOIN orders o ON op.order_id = o.id
            INNER JOIN payments pay ON o.id = pay.order_id
        WHERE
            o.status = 'PAID'
            AND pay.status = 'SUCCESS'
            AND o.created_at >= :since
        GROUP BY p.id, p.name
        ORDER BY totalQuantity DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<TopSellingProductView> findTopSellingProducts(@Param("since") LocalDateTime since, @Param("limit") Long limit);
}
