package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;

public class UserMapper {
    public static UserEntity toUserEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public static User toUser(UserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .phoneNumber(userEntity.getPhoneNumber())
                .build();
    }
}
