package io.github.dankoller.antifraud;

import io.github.dankoller.antifraud.controller.AuthorizationController;
import io.github.dankoller.antifraud.controller.TransactionController;
import io.github.dankoller.antifraud.controller.ValidationController;
import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.persistence.UserRepository;
import io.github.dankoller.antifraud.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("unused")
class AntifraudApplicationTests {

    private final User admin = new User("Test Admin",
            "testadmin",
            "password",
            "ROLE_ADMINISTRATOR",
            true);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationController authorizationController;

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private ValidationController validationController;

    // Test if the controllers are initialized
    @Test
    @Order(1)
    void contextLoads() {
        assertThat(authorizationController).isNotNull();
        assertThat(transactionController).isNotNull();
        assertThat(validationController).isNotNull();
    }

    // Save an admin user to the database
    @Test
    @Order(2)
    void saveAdminUser() {
        // Encode the password
        admin.setPassword(new BCryptPasswordEncoder().encode(admin.getPassword()));

        userRepository.save(admin);
        assertThat(userRepository.findByUsername(admin.getUsername())).isNotNull();
    }

    // Test if a merchant user can be created
    @Test
    @Order(3)
    void testMerchantCreation() throws Exception {
        String userAsJson = "{\"name\":\"" + "Test Merchant" +
                "\",\"username\":\"" + "testmerchant" +
                "\",\"password\":\"" + "password" +
                "\"}";

        mvc
                .perform(post("/api/auth/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAsJson))
                .andExpect(status().isCreated());
    }

    // Test if a support user can be created
    @Test
    @Order(4)
    void testSupportCreation() throws Exception {
        String userAsJson = "{\"name\":\"" + "Test Support" +
                "\",\"username\":\"" + "testsupport" +
                "\",\"password\":\"" + "password" +
                "\"}";

        mvc
                .perform(post("/api/auth/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userAsJson))
                .andExpect(status().isCreated());
    }

    // Test if users can be unlocked
    @Test
    @Order(5)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testUnlockUser() throws Exception {
        String unlockMerchantAsJson = "{\"username\":\"" + "testmerchant" +
                "\",\"operation\":\"" + "UNLOCK" +
                "\"}";

        String unlockSupportAsJson = "{\"username\":\"" + "testsupport" +
                "\",\"operation\":\"" + "UNLOCK" +
                "\"}";

        mvc
                .perform(put("/api/auth/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unlockMerchantAsJson))
                .andExpect(status().isOk());

        mvc
                .perform(put("/api/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unlockSupportAsJson))
                .andExpect(status().isOk());
    }

    // Check if a role can be changed
    @Test
    @Order(6)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testChangeRole() throws Exception {
        String changeRoleAsJson = "{\"username\":\"" + "testsupport" +
                "\",\"role\":\"" + "SUPPORT" +
                "\"}";

        mvc
                .perform(put("/api/auth/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(changeRoleAsJson))
                .andExpect(status().isOk());
    }

    // Test if the support can get a list of all users
    @Test
    @Order(7)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetAllUsers() throws Exception {
        mvc
                .perform(get("/api/auth/list"))
                .andExpect(status().isOk());
    }

    // Test if the admin can get a list of all users
    @Test
    @Order(8)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testGetAllUsersAdmin() throws Exception {
        mvc
                .perform(get("/api/auth/list"))
                .andExpect(status().isOk());
    }

    // Test if the merchant can't get a list of all users
    @Test
    @Order(9)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testGetAllUsersMerchant() throws Exception {
        mvc
                .perform(get("/api/auth/list"))
                .andExpect(status().isForbidden());
    }

    // TODO: Add more integration tests for the endpoints

    // Test if a user can be deleted and clean up the database
    @Test
    @Order(10)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testUserDeletion() throws Exception {
        mvc
                .perform(delete("/api/auth/user/testmerchant"))
                .andExpect(status().isOk());

        mvc
                .perform(delete("/api/auth/user/testsupport"))
                .andExpect(status().isOk());

        System.out.println("Please remove the admin user manually from the database");
    }

    // Test if the admin can be removed from the database (for cleanup)
    @Test
    @Order(11)
    void removeAdminUser() {
        userService.deleteUser(admin.getUsername());
        assertThat(userRepository.findByUsername(admin.getUsername())).isNull();
    }
}
