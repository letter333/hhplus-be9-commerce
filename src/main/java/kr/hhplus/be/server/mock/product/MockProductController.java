package kr.hhplus.be.server.mock.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.product.dto.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Mock Product API", description = "단일 상품 조회, 인기 상품 조회")
public class MockProductController {
//    @Operation(summary = "단일 상품 조회", description = "상품 ID를 이용해 조회")
//    @ApiResponse(
//            responseCode = "200",
//            description = "상품 조회 성공",
//            content = @Content(
//                    schema = @Schema(implementation = Response.class),
//                    examples = @ExampleObject(
//                            name = "단일 상품 조회 예시",
//                            value = """
//                {
//                    "code": "OK",
//                    "message": null,
//                    "data": {
//                        "id": 1,
//                        "name": "고양이 화장실",
//                        "description": "살 찐 고양이가 쓰기 좋은 아주 큰 화장실",
//                        "price": 100000,
//                        "stock": 100
//                    }
//                }
//                """
//                    )
//            )
//    )
//    @GetMapping("/api/v1/products/{id}")
//    public Response<ProductResponse> getProduct(
//            @Parameter(description = "상품 ID", example = "1") @PathVariable Long id
//    ) {
//        return Response.ok(new ProductResponse(1L, "고양이 화장실", "살 찐 고양이가 쓰기 좋은 아주 큰 화장실", 100000L, 100));
//    }

    @Operation(summary = "인기 상품 조회", description = "최근 3일간 가장 많이 팔린 상위 5개 상품 조회")
    @ApiResponse(
            responseCode = "200",
            description = "인기 상품 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "인기 상품 조회 예시",
                            value = """
                                {
                                  "code": "OK",
                                  "message": null,
                                  "data": [
                                    {
                                      "id": 3,
                                      "name": "츄르",
                                      "description": "자주 주면 살 많이 쪄요",
                                      "price": 1000,
                                      "stock": 500
                                    },
                                    {
                                      "id": 1,
                                      "name": "고양이 화장실",
                                      "description": "살 찐 고양이가 쓰기 좋은 아주 큰 화장실",
                                      "price": 100000,
                                      "stock": 100
                                    },
                                    {
                                      "id": 2,
                                      "name": "숨숨집",
                                      "description": "고양이가 안보이면 보통 여기에 숨어있습니다.",
                                      "price": 30000,
                                      "stock": 50
                                    },
                                    {
                                      "id": 4,
                                      "name": "고양이 장난감",
                                      "description": "흔들어 주면 고양이가 행복해져요.",
                                      "price": 10000,
                                      "stock": 300
                                    },
                                    {
                                      "id": 5,
                                      "name": "습식 사료",
                                      "description": "아주 환장하는 습식 사료",
                                      "price": 900,
                                      "stock": 1000
                                    }
                                  ]
                                }
                """
                    )
            )
    )
    @GetMapping("/api/v1/products/popular")
    public Response<List<ProductResponse>> getPopularProduct() {
        List<ProductResponse> productResponses = new ArrayList<>();

        productResponses.add(new ProductResponse(3L, "츄르", "자주 주면 살 많이 쪄요", 1000L, 500));
        productResponses.add(new ProductResponse(1L, "고양이 화장실", "살 찐 고양이가 쓰기 좋은 아주 큰 화장실", 100000L, 100));
        productResponses.add(new ProductResponse(2L, "숨숨집", "고양이가 안보이면 보통 여기에 숨어있습니다.", 30000L, 50));
        productResponses.add(new ProductResponse(4L, "고양이 장난감", "흔들어 주면 고양이가 행복해져요.", 10000L, 300));
        productResponses.add(new ProductResponse(5L, "습식 사료", "아주 환장하는 습식 사료", 900L, 1000));

        return Response.ok(productResponses);
    }
}
