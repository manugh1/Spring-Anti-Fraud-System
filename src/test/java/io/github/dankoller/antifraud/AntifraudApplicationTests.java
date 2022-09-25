package io.github.dankoller.antifraud;

import io.github.dankoller.antifraud.controller.AuthorizationController;
import io.github.dankoller.antifraud.controller.TransactionController;
import io.github.dankoller.antifraud.controller.ValidationController;
import io.github.dankoller.antifraud.controller.WebController;
import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SuppressWarnings("unused")
class AntifraudApplicationTests {

    private final User user = new User("Test User",
            "testuser",
            "password",
            "ROLE_MERCHANT",
            true);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorizationController authorizationController;

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private ValidationController validationController;

    @Autowired
    private WebController webController;

    // Test if the controllers are initialized
    @Test
    void contextLoads() {
        assertThat(authorizationController).isNotNull();
        assertThat(transactionController).isNotNull();
        assertThat(validationController).isNotNull();
        assertThat(webController).isNotNull();
    }

    // Test if a user can be created
    @Test
    void testUserCreation() {
        userRepository.save(user);
        assertThat(userRepository.findByUsername("testuser")).isNotNull();
    }

    // Test if a user can be deleted
    @Test
    void testUserDeletion() {
        userRepository.delete(user);
        assertThat(userRepository.findByUsername("testuser")).isNull();
    }

    // TODO: Test endpoints
}
