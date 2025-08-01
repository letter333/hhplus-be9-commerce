package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    Optional<Product> findByIdWithLock(Long id);
    List<Product> findByIdsWithLock(List<Long> ids);
    List<Product> findAll();
    Product save(Product product);
    List<Product> saveAll(List<Product> productList);
    List<Product> findAllByIdIn(List<Long> ids);
    void deleteAll();
}
