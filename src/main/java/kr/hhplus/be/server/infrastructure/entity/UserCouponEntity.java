package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.model.UserCouponStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor
public class UserCouponEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long couponId;

    @Column(nullable = false, length = 20, unique = true)
    String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserCouponStatus status;

    @Column
    LocalDateTime usedAt;

    @Column
    LocalDateTime expiredAt;

    @Builder
    public UserCouponEntity(Long id, Long userId, Long couponId, String couponCode, UserCouponStatus status, LocalDateTime usedAt, LocalDateTime expiredAt) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.status = status;
        this.usedAt = usedAt;
        this.expiredAt = expiredAt;
    }
}
