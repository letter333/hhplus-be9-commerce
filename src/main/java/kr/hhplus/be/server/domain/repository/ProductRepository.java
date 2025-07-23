package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Product;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
}
