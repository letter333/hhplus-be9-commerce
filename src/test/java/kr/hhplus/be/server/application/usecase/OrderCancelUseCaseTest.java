package kr.hhplus.be.server.application.usecase;


import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.model.OrderStatus;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelUseCase 단위 테스트")
class OrderCancelUseCaseTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderCancelUseCase orderCancelUseCase;

    @Nested
    @DisplayName("주문 취소 실행 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("취소할 주문이 있을 경우 재고를 복구하고 주문 상태를 변경")
        void 취소할_주문_존재() {
            // given
            ZonedDateTime oldDate = ZonedDateTime.now().minusMinutes(40);
            Order order1 = Order.builder().id(1L).status(OrderStatus.PENDING).createdAt(oldDate).build();
            Order order2 = Order.builder().id(2L).status(OrderStatus.PENDING).createdAt(oldDate).build();
            List<Order> targetOrders = List.of(order1, order2);

            OrderProduct orderProduct1 = OrderProduct.builder().productId(101L).quantity(2).build();
            OrderProduct orderProduct2 = OrderProduct.builder().productId(101L).quantity(3).build();
            OrderProduct orderProduct3 = OrderProduct.builder().productId(102L).quantity(1).build();
            List<OrderProduct> orderProducts = List.of(orderProduct1, orderProduct2, orderProduct3);

            Product product101 = Product.builder().id(101L).stock(10).build();
            Product product102 = Product.builder().id(102L).stock(10).build();
            List<Product> productsToRestore = List.of(product101, product102);

            when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(ZonedDateTime.class)))
                    .thenReturn(targetOrders);
            when(orderProductRepository.findByOrderIdIn(List.of(1L, 2L))).thenReturn(orderProducts);
            when(productRepository.findAllByIdIn(anyList())).thenReturn(productsToRestore);

            // when
            orderCancelUseCase.execute();

            // then
            assertThat(product101.getStock()).isEqualTo(15);
            assertThat(product102.getStock()).isEqualTo(11);

            assertThat(order1.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(order2.getStatus()).isEqualTo(OrderStatus.CANCELED);

            verify(productRepository).saveAll(productsToRestore);
            verify(orderRepository).saveAll(targetOrders);
        }

        @Test
        @DisplayName("취소할 주문이 없을 경우 아무 작업도 수행하지 않음")
        void 취소할_주문_없음() {
            // given
            when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(ZonedDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // when
            orderCancelUseCase.execute();

            // then
            verify(orderProductRepository, never()).findByOrderIdIn(anyList());
            verify(productRepository, never()).findAllByIdIn(anyList());
            verify(productRepository, never()).saveAll(anyList());
            verify(orderRepository, never()).saveAll(anyList());
        }
    }

}