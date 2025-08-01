package kr.hhplus.be.server.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserCoupon 도메인 테스트")
class UserCouponTest {
    @Test
    @DisplayName("정상적인 할인 계산 - 퍼센트")
    void 정상_할인_퍼센트() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponCode("TEST_COUPON")
                .status(UserCouponStatus.ISSUED)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Long totalPrice = 10000L;
        Long discountAmount = 20L;
        CouponType type = CouponType.PERCENTAGE;

        // when
        Long result = userCoupon.calculateDiscount(type, discountAmount, totalPrice);

        // then
        assertThat(result).isEqualTo(8000L);
    }

    @Test
    @DisplayName("정상적인 할인 계산 - 고정")
    void 정상_할인_고정() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponCode("TEST_COUPON")
                .status(UserCouponStatus.ISSUED)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Long totalPrice = 10000L;
        Long discountAmount = 3000L;
        CouponType type = CouponType.FIXED;

        // when
        Long result = userCoupon.calculateDiscount(type, discountAmount, totalPrice);

        // then
        assertThat(result).isEqualTo(7000L);
    }

    @Test
    @DisplayName("할인 금액이 총 금액보다 큰 경우 0원 반환 - 퍼센트")
    void 할인_금액이_총_금액보다_큼_퍼센트() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponCode("TEST_COUPON")
                .status(UserCouponStatus.ISSUED)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Long totalPrice = 10000L;
        Long discountAmount = 120L;
        CouponType type = CouponType.PERCENTAGE;

        // when
        Long result = userCoupon.calculateDiscount(type, discountAmount, totalPrice);

        // then
        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("할인 금액이 총 금액보다 큰 경우 0원 반환 - 고정")
    void 할인_금액이_총_금액보다_큼_고정() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponCode("TEST_COUPON")
                .status(UserCouponStatus.ISSUED)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Long totalPrice = 10000L;
        Long discountAmount = 15000L;
        CouponType type = CouponType.FIXED;

        // when
        Long result = userCoupon.calculateDiscount(type, discountAmount, totalPrice);

        // then
        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("만료된 쿠폰으로 할인 계산 시 예외 발생")
    void 만료된_쿠폰_사용() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponCode("TEST_COUPON")
                .status(UserCouponStatus.EXPIRED)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        Long totalPrice = 10000L;
        Long discountAmount = 20L;
        CouponType type = CouponType.PERCENTAGE;

        // when & then
        assertThatThrownBy(() -> userCoupon.calculateDiscount(type, discountAmount, totalPrice));
    }

    @Test
    @DisplayName("이미 사용된 쿠폰으로 할인 계산 시 예외 발생")
    void 사용된_쿠폰_사용() {
        // given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .couponCode("TEST_COUPON")
                .status(UserCouponStatus.USED)
                .usedAt(LocalDateTime.now().minusHours(1))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Long totalPrice = 10000L;
        Long discountAmount = 20L;
        CouponType type = CouponType.PERCENTAGE;

        // when & then
        assertThatThrownBy(() -> userCoupon.calculateDiscount(type, discountAmount, totalPrice));
    }
}