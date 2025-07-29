package kr.hhplus.be.server.application.usecase.dto.command;

import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.OrderProduct;

import java.util.List;

public record OrderCreateCommand(
        Long userId,
        List<OrderProduct> orderProductList,
        Address shippingAddress,
        String recipientNumber
) {
}
