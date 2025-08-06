package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.model.PointHistoryType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor
public class PointHistoryEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointHistoryType type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAfter;

    @Builder
    public PointHistoryEntity(Long id, Long userId, Long orderId, PointHistoryType type, Long amount, Long balanceAfter) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }
}
