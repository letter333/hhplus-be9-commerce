package kr.hhplus.be.server.interfaces.api;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.usecase.UserRegisterUseCase;
import kr.hhplus.be.server.application.usecase.dto.command.UserRegisterCommand;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.interfaces.dto.request.UserCreateRequest;
import kr.hhplus.be.server.interfaces.dto.response.UserCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserRegisterUseCase userCreateUseCase;

    @PostMapping("/api/v1/users")
    public CommonResponse<UserCreateResponse> registerUser(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        UserRegisterCommand command = new UserRegisterCommand(userCreateRequest.name(), userCreateRequest.phoneNumber());
        User createdUser = userCreateUseCase.execute(command);

        UserCreateResponse userCreateResponse = new UserCreateResponse(createdUser.getId(), createdUser.getName(), createdUser.getPhoneNumber());

        return CommonResponse.ok("사용자 생성에 성공했습니다.", userCreateResponse);
    }
}
