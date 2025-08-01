package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.infrastructure.entity.OrderProductEntity;

public class OrderProductMapper {
    public static OrderProductEntity toOrderProductEntity(OrderProduct orderProduct) {
        return OrderProductEntity.builder()
                .id(orderProduct.getId())
                .orderId(orderProduct.getOrderId())
                .productId(orderProduct.getProductId())
                .quantity(orderProduct.getQuantity())
                .price(orderProduct.getPrice())
                .build();
    }

    public static OrderProduct toOrderProduct(OrderProductEntity orderProductEntity) {
        return OrderProduct.builder()
                .id(orderProductEntity.getId())
                .orderId(orderProductEntity.getOrderId())
                .productId(orderProductEntity.getProductId())
                .quantity(orderProductEntity.getQuantity())
                .price(orderProductEntity.getPrice())
                .build();
    }
}
