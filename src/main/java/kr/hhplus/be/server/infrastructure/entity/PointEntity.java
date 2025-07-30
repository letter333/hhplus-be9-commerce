package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    @Builder
    public PointEntity(Long id, Long userId, Long balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }
}
