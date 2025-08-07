package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.infrastructure.entity.OrderEntity;

public class OrderMapper {
    public static Order toOrder(OrderEntity orderEntity) {
        return Order.builder()
                .id(orderEntity.getId())
                .userId(orderEntity.getUserId())
                .status(orderEntity.getStatus())
                .totalPrice(orderEntity.getTotalPrice())
                .discountAmount(orderEntity.getDiscountAmount())
                .finalPrice(orderEntity.getFinalPrice())
                .shippingAddress(new Address(orderEntity.getShippingAddress1(), orderEntity.getShippingAddress2(), orderEntity.getShippingZipCode()))
                .recipientNumber(orderEntity.getRecipientNumber())
                .version(orderEntity.getVersion())
                .build();
    }

    public static OrderEntity toOrderEntity(Order order) {
        return OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .discountAmount(order.getDiscountAmount())
                .finalPrice(order.getFinalPrice())
                .shippingAddress1(order.getShippingAddress().shippingAddress1())
                .shippingAddress2(order.getShippingAddress().shippingAddress2())
                .shippingZipCode(order.getShippingAddress().shippingZipCode())
                .recipientNumber(order.getRecipientNumber())
                .version(order.getVersion())
                .build();
    }
}
