package kr.hhplus.be.server.infrastructure.repository.jpa.user;

import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
}
