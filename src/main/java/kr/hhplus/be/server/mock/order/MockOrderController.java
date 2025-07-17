package kr.hhplus.be.server.mock.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.order.dto.CreateOrderRequest;
import kr.hhplus.be.server.mock.order.dto.CreateOrderResponse;
import kr.hhplus.be.server.mock.order.dto.OrderProductRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@Tag(name = "Mock Order API", description = "상품 주문")
public class MockOrderController {
    @Operation(summary = "상품 주문", description = "상품 목록들을 받아 주문 생성")
    @PostMapping(value = "/api/v1/orders")
    public Response<CreateOrderResponse> create(
            @RequestBody CreateOrderRequest createOrderRequest
    ) {
        createOrderRequest.orderProductList().add(new OrderProductRequest(1L, "고양이 화장실", "살 찐 고양이가 쓰기 좋은 아주 큰 화장실", 100000L, 1));
            createOrderRequest.orderProductList().add(new OrderProductRequest(2L, "숨숨집", "고양이가 안보이면 보통 여기에 숨어있습니다.", 30000L, 1));
        createOrderRequest.orderProductList().add(new OrderProductRequest(3L, "츄르", "자주 주면 살 많이 쪄요", 1000L, 10));

        return Response.ok("주문 성공", new CreateOrderResponse(
                1L,
                1L,
                CreateOrderResponse.status.PENDING,
                140000L,
                10000L,
                130000L,
                "기본 주소",
                "상세 주소",
                "123456",
                ZonedDateTime.now()
        ));
    }
}
