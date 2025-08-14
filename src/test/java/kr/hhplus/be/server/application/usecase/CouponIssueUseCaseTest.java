package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.CouponType;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("쿠폰 발급 단위 테스트")
class CouponIssueUseCaseTest {
    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private RedissonLockManager redissonLockManager;

    @Mock
    private RLock rLock;

    @InjectMocks
    private CouponIssueUseCase couponIssueUseCase;

    @BeforeEach
    void setUp() throws InterruptedException {
        when(redissonLockManager.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        doAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        }).when(transactionTemplate).execute(any(TransactionCallback.class));
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class CouponIssueTest {
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
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(0)
                    .expiredAt(LocalDateTime.now().plusDays(30))
                    .build();

            UserCoupon userCoupon = UserCoupon.builder()
                    .id(1L)
                    .userId(userId)
                    .couponId(couponId)
                    .couponCode("ABCD-1234-5678-0123")
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(LocalDateTime.now().plusDays(30))
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
        @DisplayName("이미 발급받은 쿠폰 재발급 시도 시 실패")
        void 이미_발급받은_쿠폰_재발급() {
            // given
            Long couponId = 1L;
            Long userId = 100L;
            CouponIssueCommand command = new CouponIssueCommand(couponId, userId);

            Coupon coupon = Coupon.builder()
                    .id(couponId)
                    .name("테스트 쿠폰")
                    .type(CouponType.PERCENTAGE)
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().plusDays(30))
                    .build();

            when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
            when(userCouponRepository.existsByCouponIdAndUserId(couponId, userId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(couponRepository).findById(couponId);
            verify(userCouponRepository).existsByCouponIdAndUserId(couponId, userId);
            verify(couponRepository, never()).save(any());
            verify(userCouponRepository, never()).save(any());
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
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(100)
                    .expiredAt(LocalDateTime.now().plusDays(30))
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
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(50)
                    .expiredAt(LocalDateTime.now().minusDays(1))
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
                    .discountAmount(10L)
                    .quantity(100)
                    .issuedQuantity(0)
                    .expiredAt(LocalDateTime.now().plusDays(30))
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
            assertThat(mockCoupon.getIssuedQuantity()).isEqualTo(10);
        }
    }
}
