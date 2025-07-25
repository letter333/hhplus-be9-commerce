package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAll();
    Product save(Product product);
    List<Product> saveAll(List<Product> productList);
    List<Product> findAllById(List<Long> ids);
}
