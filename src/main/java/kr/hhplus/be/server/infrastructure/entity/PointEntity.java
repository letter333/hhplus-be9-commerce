package kr.hhplus.be.server.infrastructure.entity;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEntity extends BaseEntity {
    private Long id;
    private Long userId;
    private Long balance;

    @Builder
    public PointEntity(Long id, Long userId, Long balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }
}
