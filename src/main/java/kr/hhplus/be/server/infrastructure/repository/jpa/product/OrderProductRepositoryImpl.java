package kr.hhplus.be.server.infrastructure.repository.jpa.product;

import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.infrastructure.entity.OrderProductEntity;
import kr.hhplus.be.server.infrastructure.mapper.OrderProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class OrderProductRepositoryImpl implements OrderProductRepository {
    private final OrderProductJpaRepository orderProductJpaRepository;

    @Override
    public List<OrderProduct> findByOrderId(Long orderId) {
        return orderProductJpaRepository.findByOrderId(orderId)
                .stream()
                .map(OrderProductMapper::toOrderProduct)
                .toList();
    }

    @Override
    public List<OrderProduct> findByOrderIdIn(List<Long> orderIds) {
        return orderProductJpaRepository.findByOrderIdIn(orderIds)
                .stream()
                .map(OrderProductMapper::toOrderProduct)
                .toList();
    }

    @Override
    public List<OrderProduct> saveAll(List<OrderProduct> orderProducts) {
        List<OrderProductEntity> entities = orderProducts.stream()
                .map(OrderProductMapper::toOrderProductEntity)
                .toList();

        List<OrderProductEntity> savedEntities = orderProductJpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(OrderProductMapper::toOrderProduct)
                .toList();
    }

    @Override
    public void deleteAll() {
        orderProductJpaRepository.deleteAll();
    }
}
