package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderProductInMemoryRepositoryImpl implements OrderProductRepository {
    @Override
    public List<OrderProduct> findByOrderId(Long orderId) {
        return List.of();
    }

    @Override
    public List<OrderProduct> findByOrderIdIn(List<Long> orderIds) {
        return List.of();
    }

    @Override
    public List<OrderProduct> saveAll(List<OrderProduct> orderProducts) {
        return List.of();
    }

    @Override
    public void deleteAll() {

    }
}
