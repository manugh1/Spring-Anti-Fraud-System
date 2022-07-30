package io.github.dankoller.antifraud.response;

import io.github.dankoller.antifraud.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserDataResponse {

    private Long id;
    private final String name;
    private final String username;
    private final String role;

    // Returns user data without sensitive information
    public static UserDataResponse createUserDataResponse(User user) {
        return new UserDataResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRoleWithoutPrefix()
        );
    }
}
