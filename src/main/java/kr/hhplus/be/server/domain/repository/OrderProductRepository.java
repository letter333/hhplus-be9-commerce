package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.OrderProduct;

import java.util.List;

public interface OrderProductRepository {
    List<OrderProduct> findByOrderId(Long orderId);
    List<OrderProduct> findByOrderIdIn(List<Long> orderIds);
    List<OrderProduct> saveAll(List<OrderProduct> orderProducts);
    void deleteAll();
}
