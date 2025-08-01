package kr.hhplus.be.server.infrastructure.repository.jpa.order;

import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.infrastructure.entity.OrderEntity;
import kr.hhplus.be.server.infrastructure.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
                .map(OrderMapper::toOrder);
    }

    @Override
    public Optional<Order> findByIdWithLock(Long id) {
        return orderJpaRepository.findByIdWithLock(id).map(OrderMapper::toOrder);
    }

    @Override
    public List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime) {
        return orderJpaRepository.findByStatusAndCreatedAtBefore(status, dateTime)
                .stream()
                .map(OrderMapper::toOrder)
                .toList();
    }

    @Override
    public Order save(Order order) {
        OrderEntity orderEntity = OrderMapper.toOrderEntity(order);
        OrderEntity savedEntity = orderJpaRepository.save(orderEntity);

        return OrderMapper.toOrder(savedEntity);
    }

    @Override
    public List<Order> saveAll(List<Order> orders) {
        List<OrderEntity> orderEntities = orders.stream()
                .map(OrderMapper::toOrderEntity)
                .toList();

        List<OrderEntity> savedEntities = orderJpaRepository.saveAll(orderEntities);

        return savedEntities.stream()
                .map(OrderMapper::toOrder)
                .toList();
    }

    @Override
    public void deleteAll() {
        orderJpaRepository.deleteAll();
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll()
                .stream().map(OrderMapper::toOrder)
                .toList();
    }
}
