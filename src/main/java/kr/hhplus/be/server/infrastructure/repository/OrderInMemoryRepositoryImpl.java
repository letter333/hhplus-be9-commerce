package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.infrastructure.entity.OrderEntity;
import kr.hhplus.be.server.infrastructure.mapper.OrderMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderInMemoryRepositoryImpl implements OrderRepository {
    private final Map<Long, OrderEntity> table = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(table.get(id))
                .map(OrderMapper::toOrder);
    }

    @Override
    public List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime) {
        return List.of();
    }

    @Override
    public Order save(Order order) {
        OrderEntity orderEntity = OrderMapper.toOrderEntity(order);
        OrderEntity savedEntity;

        if(orderEntity.getId() == null || table.get(orderEntity.getId()) == null) {
            Long newId = idGenerator.incrementAndGet();
            savedEntity = OrderEntity.builder()
                    .id(newId)
                    .userId(orderEntity.getUserId())
                    .status(orderEntity.getStatus())
                    .totalPrice(orderEntity.getTotalPrice())
                    .discountAmount(orderEntity.getDiscountAmount())
                    .finalPrice(orderEntity.getFinalPrice())
                    .shippingAddress1(orderEntity.getShippingAddress1())
                    .shippingAddress2(orderEntity.getShippingAddress2())
                    .shippingZipCode(orderEntity.getShippingZipCode())
                    .recipientNumber(orderEntity.getRecipientNumber())
                    .build();

            table.put(newId, savedEntity);
        } else {
            table.put(orderEntity.getId(), orderEntity);
            savedEntity = orderEntity;
        }

        return OrderMapper.toOrder(savedEntity);
    }

    @Override
    public List<Order> saveAll(List<Order> orders) {
        return List.of();
    }
}
