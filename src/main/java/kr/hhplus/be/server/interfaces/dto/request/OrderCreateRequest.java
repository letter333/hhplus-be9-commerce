package kr.hhplus.be.server.interfaces.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.OrderProduct;

import java.util.List;

public record OrderCreateRequest(
        @NotNull(message = "사용자 ID는 필수 입력입니다.")
        Long userId,
        Long userCouponId,

        @NotEmpty(message = "주문 상품을 확인해주세요.")
        List<OrderProduct> orderProductList,

        @NotNull(message = "배송주소를 입력해주세요")
        Address shippingAddress,

        @NotEmpty(message = "받는 사람의 전화번호를 입력해주세요.")
        String recipientNumber
) {
}
