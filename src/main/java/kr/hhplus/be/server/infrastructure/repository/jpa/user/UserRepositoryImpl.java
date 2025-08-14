package kr.hhplus.be.server.infrastructure.repository.jpa.user;

import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import kr.hhplus.be.server.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        UserEntity userEntity = UserMapper.toUserEntity(user);
        UserEntity savedEntity = userJpaRepository.save(userEntity);

        return UserMapper.toUser(savedEntity);
    }

    @Override
    public void deleteAll() {
        userJpaRepository.deleteAll();
    }
}
