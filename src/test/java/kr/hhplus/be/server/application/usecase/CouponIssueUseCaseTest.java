package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.CouponType;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CouponIssueUseCaseTest {
    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponIssueUseCase couponIssueUseCase;

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class CouponIssueTest  {
        @Test
        @DisplayName("쿠폰 발급 성공")
        void 쿠폰_발급() {
            // given
            Long couponId = 1L;
            Long userId = 1L;
            CouponIssueCommand command = new CouponIssueCommand(couponId, userId);

            Coupon coupon = Coupon.builder()
                    .id(couponId)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountPercentage(10L)
                    .quantity(500)
                    .issuedQuantity(0)
                    .expiredAt(ZonedDateTime.now().plusDays(30))
                    .build();
            UserCoupon userCoupon = UserCoupon.builder()
                    .id(1L)
                    .userId(userId)
                    .couponId(couponId)
                    .couponCode("ABCD-1234-5678-0123")
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(ZonedDateTime.now().plusDays(30))
                    .build();

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.existsByCouponIdAndUserId(couponId, userId)).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
            when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(userCoupon);

            // when
            UserCoupon result = couponIssueUseCase.execute(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getCouponId()).isEqualTo(couponId);
            assertThat(result.getStatus()).isEqualTo(UserCouponStatus.ISSUED);
            assertThat(result.getCouponCode()).isNotBlank();

            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);

            verify(couponRepository).findById(couponId);
            verify(userCouponRepository).existsByCouponIdAndUserId(couponId, userId);
            verify(couponRepository).save(coupon);
            verify(userCouponRepository).save(any(UserCoupon.class));

        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 발급 시도 시 실패")
        void 존재하지_않는_쿠폰_발급() {
            // given
            Long couponId = 999L;
            Long userId = 100L;
            CouponIssueCommand command = new CouponIssueCommand(couponId, userId);

            when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(couponRepository).findById(couponId);
            verifyNoInteractions(userCouponRepository);
        }

        @Test
        @DisplayName("발급 가능한 쿠폰이 없을 때 실패")
        void 발급_가능한_쿠폰_없음() {
            // given
            Long couponId = 1L;
            Long userId = 100L;
            CouponIssueCommand command = new CouponIssueCommand(couponId, userId);

            Coupon soldOutCoupon = Coupon.builder()
                    .id(couponId)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountPercentage(10L)
                    .quantity(500)
                    .issuedQuantity(500)
                    .expiredAt(ZonedDateTime.now().plusDays(30))
                    .build();

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(soldOutCoupon));
            when(userCouponRepository.existsByCouponIdAndUserId(couponId, userId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(couponRepository).findById(couponId);
            verify(userCouponRepository).existsByCouponIdAndUserId(couponId, userId);
            verify(couponRepository, never()).save(any());
            verify(userCouponRepository, never()).save(any());
        }

        @Test
        @DisplayName("만료된 쿠폰 발급 시도 시 실패")
        void 만료된_쿠폰_발급() {
            // given
            Long couponId = 1L;
            Long userId = 100L;
            CouponIssueCommand command = new CouponIssueCommand(couponId, userId);

            Coupon expiredCoupon = Coupon.builder()
                    .id(couponId)
                    .name("만료된 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountPercentage(10L)
                    .quantity(100)
                    .issuedQuantity(50)
                    .expiredAt(ZonedDateTime.now().minusDays(1))
                    .build();

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(expiredCoupon));
            when(userCouponRepository.existsByCouponIdAndUserId(couponId, userId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(couponRepository).findById(couponId);
            verify(userCouponRepository).existsByCouponIdAndUserId(couponId, userId);
            verify(couponRepository, never()).save(any());
            verify(userCouponRepository, never()).save(any());
        }

        @Test
        @DisplayName("연속 발급 시 쿠폰 코드 중복되지 않음")
        void 연속_발급시_쿠폰_코드_중복_없음() {
            // given
            Long couponId = 1L;
            Coupon mockCoupon = Coupon.builder()
                    .id(couponId)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountPercentage(10L)
                    .quantity(100)
                    .issuedQuantity(50)
                    .expiredAt(ZonedDateTime.now().plusDays(30))
                    .build();

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(mockCoupon));
            when(userCouponRepository.existsByCouponIdAndUserId(eq(couponId), any())).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenReturn(mockCoupon);
            when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Set<String> generatedCodes = new HashSet<>();

            // when
            for (long userId = 1L; userId <= 10L; userId++) {
                CouponIssueCommand command = new CouponIssueCommand(couponId, userId);
                UserCoupon result = couponIssueUseCase.execute(command);
                generatedCodes.add(result.getCouponCode());
            }

            // then
            assertThat(generatedCodes).hasSize(10);
        }
    }

    @Nested
    @DisplayName("동시성 제어 테스트")
    class ConcurrencyControlTest {

        @Test
        @DisplayName("같은 쿠폰에 대한 동시 발급 요청 시 순차 처리")
        void 같은_쿠폰_동시_발급_순차_처리() throws InterruptedException {
            // given
            Long couponId = 1L;
            Coupon mockCoupon = Coupon.builder()
                    .id(couponId)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountPercentage(10L)
                    .quantity(500)
                    .issuedQuantity(0)
                    .expiredAt(ZonedDateTime.now().plusDays(30))
                    .build();

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(mockCoupon));
            when(userCouponRepository.existsByCouponIdAndUserId(eq(couponId), any())).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenReturn(mockCoupon);
            when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

            int threadCount = 500;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<UserCoupon> results = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1L;
                executorService.submit(() -> {
                    try {
                        CouponIssueCommand command = new CouponIssueCommand(couponId, userId);
                        UserCoupon result = couponIssueUseCase.execute(command);
                        results.add(result);
                    } catch (Exception e) {

                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(results).hasSize(threadCount);
            assertThat(mockCoupon.getIssuedQuantity()).isEqualTo(threadCount);
        }
    }
}