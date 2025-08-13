package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.User;

public interface UserRepository {
    User save(User user);
    void deleteAll();
}
