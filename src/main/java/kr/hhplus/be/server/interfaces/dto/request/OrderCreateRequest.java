package kr.hhplus.be.server.interfaces.dto.request;

import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.OrderProduct;

import java.util.List;

public record OrderCreateRequest(
        Long userId,
        List<OrderProduct> orderProductList,
        Address shippingAddress,
        String recipientNumber
) {
}
