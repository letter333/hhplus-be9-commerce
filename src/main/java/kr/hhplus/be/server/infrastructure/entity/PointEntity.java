package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "points")
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

    @Version
    private Long version;

    @Builder
    public PointEntity(Long id, Long userId, Long balance, Long version) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.version = version;
    }
}
