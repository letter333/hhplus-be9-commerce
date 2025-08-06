package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.UserRegisterCommand;
import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterUseCase {
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    public User execute(UserRegisterCommand command) {
        User user = User.builder()
                .name(command.name())
                .phoneNumber(command.phoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        Point point = Point.builder()
                .userId(savedUser.getId())
                .balance(0L)
                .build();

        pointRepository.save(point);

        return savedUser;
    }
}
