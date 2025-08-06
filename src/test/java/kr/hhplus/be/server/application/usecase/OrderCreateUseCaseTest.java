package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.OrderCreateCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("주문 단위 테스트")
class OrderCreateUseCaseTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private OrderCreateUseCase orderCreateUseCase;

    @Nested
    @DisplayName("주문 생성 테스트")
    class OrderCreateTest {
        @Test
        @DisplayName("주문 생성 성공")
        void 주문_생성() {
            // given
            Long userId = 1L;
            List<OrderProduct> orderProductList = List.of(
                    new OrderProduct(1L, 1L, 1L, 5, 10000L),
                    new OrderProduct(2L, 1L, 2L, 1, 20000L)
            );
            Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
            String recipientNumber = "010-1234-5678";
            OrderCreateCommand command = new OrderCreateCommand(userId, null, orderProductList, shippingAddress, recipientNumber);

            Product product1 = Product.builder().id(1L).name("상품1").price(10000L).stock(10).build();
            Product product2 = Product.builder().id(2L).name("상품2").price(20000L).stock(5).build();
            List<Product> products = List.of(product1, product2);

            when(productRepository.findByIdsWithLock(List.of(1L, 2L))).thenReturn(products);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Order resultOrder = orderCreateUseCase.execute(command);

            // then
            assertThat(resultOrder).isNotNull();
            assertThat(resultOrder.getUserId()).isEqualTo(userId);
            assertThat(resultOrder.getTotalPrice()).isEqualTo(70000L);
            assertThat(resultOrder.getFinalPrice()).isEqualTo(70000L);
            assertThat(product1.getStock()).isEqualTo(5);
            assertThat(product2.getStock()).isEqualTo(4);

            verify(productRepository, times(1)).findByIdsWithLock(List.of(1L, 2L));
            verify(productRepository, times(1)).saveAll(products);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("존재하지 않는 상품이 포함된 경우 예외 처리")
        void 존재하지_않는_상품_주문() {
            // given
            Long userId = 1L;
            List<OrderProduct> orderProductList = List.of(new OrderProduct(1L, 1L, 1L, 1, 10000L));
            Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
            String recipientNumber = "010-1234-5678";
            OrderCreateCommand command = new OrderCreateCommand(userId, null, orderProductList, shippingAddress, recipientNumber);

            when(productRepository.findByIdsWithLock(List.of(1L))).thenReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(productRepository).findByIdsWithLock(List.of(1L));
            verify(orderRepository, never()).save(any(Order.class));
            verify(productRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("재고 부족 시 예외 처리")
        void 재고_부족_주문() {
            // given
            Long userId = 1L;
            List<OrderProduct> orderProductList = List.of(new OrderProduct(1L, 1L, 1L, 10, 10000L));
            Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
            String recipientNumber = "010-1234-5678";
            OrderCreateCommand command = new OrderCreateCommand(userId, null, orderProductList, shippingAddress, recipientNumber);

            Product product = Product.builder().id(1L).name("상품1").price(10000L).stock(5).build();
            when(productRepository.findByIdsWithLock(List.of(1L))).thenReturn(List.of(product));

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalStateException.class);

            verify(productRepository).findByIdsWithLock(List.of(1L));
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 테스트")
    class CouponApplicationTest {
        @Test
        @DisplayName("쿠폰 적용 성공")
        void 쿠폰_적용_성공() {
            // given
            Long userId = 1L;
            Long userCouponId = 1L;
            Long couponId = 1L;
            List<OrderProduct> orderProductList = List.of(new OrderProduct(1L, 1L, 1L, 2, 10000L));
            Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
            String recipientNumber = "010-1111-2222";
            OrderCreateCommand command = new OrderCreateCommand(userId, userCouponId, orderProductList, shippingAddress, recipientNumber);

            Product product = Product.builder().id(1L).name("상품").price(10000L).stock(10).build();
            Coupon coupon = new Coupon(couponId, "3000원 할인 쿠폰", CouponType.FIXED, 3000L, 100, 10, LocalDateTime.now().plusDays(10), LocalDateTime.now());
            UserCoupon userCoupon = new UserCoupon(userCouponId, userId, couponId, "coupon-code", UserCouponStatus.ISSUED, null, null, null);

            given(productRepository.findByIdsWithLock(List.of(1L))).willReturn(List.of(product));
            given(userCouponRepository.findById(userCouponId)).willReturn(Optional.of(userCoupon));
            given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Order order = orderCreateUseCase.execute(command);

            // then
            assertThat(order.getTotalPrice()).isEqualTo(20000L);
            assertThat(order.getFinalPrice()).isEqualTo(17000L);
            assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
            verify(userCouponRepository, times(1)).save(userCoupon);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("주문 실패 시 쿠폰 상태가 복구된다")
        void 주문_실패_시_쿠폰_복구() {
            // given
            Long userId = 1L;
            Long userCouponId = 1L;
            Long couponId = 1L;
            List<OrderProduct> orderProductList = List.of(new OrderProduct(1L, 1L, 1L, 1, 10000L));
            Address shippingAddress = new Address("기본 주소", "상세 주소", "12345");
            String recipientNumber = "010-1111-2222";
            OrderCreateCommand command = new OrderCreateCommand(userId, userCouponId, orderProductList, shippingAddress, recipientNumber);

            Product product = Product.builder().id(1L).name("상품").price(10000L).stock(10).build();
            Coupon coupon = new Coupon(couponId, "10% 할인 쿠폰", CouponType.PERCENTAGE, 10L, 100, 10, LocalDateTime.now().plusDays(10), LocalDateTime.now());
            UserCoupon userCoupon = new UserCoupon(userCouponId, userId, couponId, "coupon-code", UserCouponStatus.ISSUED, null, null, null);

            given(productRepository.findByIdsWithLock(List.of(1L))).willReturn(List.of(product));
            given(userCouponRepository.findById(userCouponId)).willReturn(Optional.of(userCoupon));
            given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
            given(orderRepository.save(any(Order.class))).willThrow(new RuntimeException("DB 저장 실패"));

            // when & then
            assertThatThrownBy(() -> orderCreateUseCase.execute(command))
                    .isInstanceOf(IllegalStateException.class);

            // then
            assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.ISSUED);
            verify(userCouponRepository, times(1)).save(userCoupon);
            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }
}