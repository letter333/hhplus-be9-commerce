package kr.hhplus.be.server.domain.model;


import lombok.Builder;
import lombok.Getter;

@Getter
public class User {
    private Long id;
    private String name;
    private String phoneNumber;

    @Builder
    public User(Long id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
