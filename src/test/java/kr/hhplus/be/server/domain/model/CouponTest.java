package kr.hhplus.be.server.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Coupon 도메인 테스트")
class CouponTest {
    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class CouponIssueTest {

        @Test
        @DisplayName("정상적인 쿠폰 발급")
        void 쿠폰_발급_성공() {
            // given
            Coupon coupon = Coupon.builder()
                    .id(1L)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(0)
                    .expiredAt(LocalDateTime.now().plusDays(30))
                    .build();

            // when
            coupon.issue();

            // then
            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("만료된 쿠폰 발급 시 예외 처리")
        void 만료된_쿠폰_발급() {
            // given
            Coupon coupon = Coupon.builder()
                    .id(1L)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(0)
                    .expiredAt(LocalDateTime.now().minusDays(30))
                    .build();

            // when & then
            assertThatThrownBy(coupon::issue)
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("이미 발급 종료된 쿠폰이면 예외 처리")
        void 발급_가능한_쿠폰_없음() {
            // given
            Coupon coupon = Coupon.builder()
                    .id(1L)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(100)
                    .expiredAt(LocalDateTime.now().minusDays(30))
                    .build();

            // when & then
            assertThatThrownBy(coupon::issue)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

}