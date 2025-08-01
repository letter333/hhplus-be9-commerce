package kr.hhplus.be.server.application.usecase;


import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.CouponType;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class CouponIssueUseCaseIntegrationTest {

    @Autowired
    private CouponIssueUseCase couponIssueUseCase;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Test
    @DisplayName("쿠폰 발급 성공 테스트")
    void 쿠폰_발급_성공() {
        // given
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.builder()
                .name("할인 쿠폰")
                .type(CouponType.FIXED)
                .discountAmount(1000L)
                .quantity(100)
                .issuedQuantity(0)
                .expiredAt(expiredAt)
                .createdAt(LocalDateTime.now())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        CouponIssueCommand command = new CouponIssueCommand(savedCoupon.getId(), 1L);

        // when
        UserCoupon result = couponIssueUseCase.execute(command);

        // when
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCouponId()).isEqualTo(savedCoupon.getId());
        assertThat(result.getStatus()).isEqualTo(UserCouponStatus.ISSUED);
        assertThat(result.getCouponCode()).isNotNull();
        assertThat(result.getExpiredAt()).isEqualTo(expiredAt);

        Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1);

        Optional<UserCoupon> userCoupon = userCouponRepository.findByCouponIdAndUserId(savedCoupon.getId(), 1L);
        assertThat(userCoupon).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰으로 발급 시도 시 예외 발생")
    void 존재하지_않는_쿠폰_발급() {
        // given
        CouponIssueCommand command = new CouponIssueCommand(999L, 1L);

        // when & then
        assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰으로 중복 발급 시도 시 예외 발생")
    void 쿠폰_중복_발급() {
        // given
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.builder()
                .name("할인 쿠폰")
                .type(CouponType.FIXED)
                .discountAmount(1000L)
                .quantity(100)
                .issuedQuantity(0)
                .expiredAt(expiredAt)
                .createdAt(LocalDateTime.now())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        UserCoupon existingUserCoupon = UserCoupon.builder()
                .userId(1L)
                .couponId(savedCoupon.getId())
                .couponCode("ABCD-1234-5678-0123")
                .status(UserCouponStatus.ISSUED)
                .expiredAt(expiredAt)
                .createdAt(LocalDateTime.now())
                .build();
        userCouponRepository.save(existingUserCoupon);

        CouponIssueCommand command = new CouponIssueCommand(savedCoupon.getId(), 1L);

        // when & then
        assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
