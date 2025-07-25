package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.OrderCreateCommand;
import kr.hhplus.be.server.domain.model.Address;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.OrderRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderCreateUseCaseTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderCreateUseCase orderCreateUseCase;

    @Test
    @DisplayName("주문 생성 성공")
    void 주문_생성() {
        //given
        Long userId = 1L;
        List<OrderProduct> orderProductList = List.of(
                new OrderProduct(1L, 1L, 5, 1000L),
                new OrderProduct(2L, 2L, 1, 20_000L)
        );
        Address shippingAddress = new Address("기본 주소", "상세 주소", "1234567890");
        String recipientNumber = "010-1234-5678";
        OrderCreateCommand command = new OrderCreateCommand(userId, orderProductList, shippingAddress, recipientNumber);

        Product product1 = Product.builder().id(1L).name("상품1").price(10000L).stock(10).build();
        Product product2 = Product.builder().id(2L).name("상품2").price(5000L).stock(5).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Order resultOrder = orderCreateUseCase.execute(command);

        // then
        assertThat(resultOrder).isNotNull();
        assertThat(resultOrder.getUserId()).isEqualTo(userId);
        assertThat(resultOrder.getTotalPrice()).isEqualTo(55000L);
        assertThat(resultOrder.getFinalPrice()).isEqualTo(55000L);
        assertThat(product1.getStock()).isEqualTo(5);
        assertThat(product2.getStock()).isEqualTo(4);

        verify(productRepository, times(2)).findById(anyLong());
        verify(productRepository, times(1)).saveAll(anyList());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품이면 예외 처리")
    void 존재하지_않는_상품_조회() {
        // given
        Long userId = 1L;
        List<OrderProduct> orderProductList = List.of(new OrderProduct(1L, 1111L, 1, 1000L));
        Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
        String recipientNumber = "010-1234-5678";
        OrderCreateCommand command = new OrderCreateCommand(userId, orderProductList, shippingAddress, recipientNumber);

        when(productRepository.findById(1111L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderCreateUseCase.execute(command)).isInstanceOf(IllegalArgumentException.class);

        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).saveAll(anyList());
    }

}