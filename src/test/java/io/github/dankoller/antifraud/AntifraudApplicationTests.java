package io.github.dankoller.antifraud;

import io.github.dankoller.antifraud.controller.AuthorizationController;
import io.github.dankoller.antifraud.controller.TransactionController;
import io.github.dankoller.antifraud.controller.ValidationController;
import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.persistence.TransactionRepository;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    private TransactionRepository transactionRepository;

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

    // Test if the (admin) user can log in
    @Test
    @Order(3)
    void loginAdminUser() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + admin.getUsername() + "\",\"password\":\"" + admin.getPassword() + "\"}"))
                .andExpect(status().isOk())
                // Response should contain name, username and role
                .andExpect(content().string(containsString(admin.getName())))
                .andExpect(content().string(containsString(admin.getUsername())))
                .andExpect(content().string(containsString(admin.getRoleWithoutPrefix())));
    }

    // Test if a non-existing user can't log in
    @Test
    @Order(4)
    void loginNonExistingUser() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexisting\",\"password\":\"password\"}"))
                .andExpect(status().isNotFound());
    }

    // Test if a merchant user can be created
    @Test
    @Order(5)
    void testMerchantCreation() throws Exception {
        String userAsJson = "{\"name\":\"" + "Test Merchant" +
                "\",\"username\":\"" + "testmerchant" +
                "\",\"password\":\"" + "password" +
                "\"}";

        mvc
                .perform(post("/api/auth/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAsJson))
                // Check the response status code
                .andExpect(status().isCreated())
                // Check that the response contains id, name, username, and role
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("name")))
                // Name should be the same as the one we sent
                .andExpect(content().string(containsString("Test Merchant")))
                .andExpect(content().string(containsString("username")))
                // Username should be the same as the one we sent
                .andExpect(content().string(containsString("testmerchant")))
                .andExpect(content().string(containsString("role")))
                // Role should be MERCHANT by default
                .andExpect(content().string(containsString("MERCHANT")));
    }

    // Test if a support user can be created
    @Test
    @Order(6)
    void testSupportCreation() throws Exception {
        String userAsJson = "{\"name\":\"" + "Test Support" +
                "\",\"username\":\"" + "testsupport" +
                "\",\"password\":\"" + "password" +
                "\"}";

        mvc
                .perform(post("/api/auth/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAsJson))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("name")))
                .andExpect(content().string(containsString("Test Support")))
                .andExpect(content().string(containsString("username")))
                .andExpect(content().string(containsString("testsupport")))
                .andExpect(content().string(containsString("role")))
                .andExpect(content().string(containsString("MERCHANT"))); // MERCHANT by default
    }

    // Test if users can be unlocked
    @Test
    @Order(7)
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
                .andExpect(status().isOk())
                // Response should contain status field
                .andExpect(content().string(containsString("status")))
                // Status should be 'User <username> unlocked!'
                .andExpect(content().string(containsString("User testmerchant unlocked!")));

        mvc
                .perform(put("/api/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unlockSupportAsJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("status")))
                .andExpect(content().string(containsString("User testsupport unlocked!")));
    }

    // Check if a role can be changed
    @Test
    @Order(8)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testChangeRole() throws Exception {
        String changeRoleAsJson = "{\"username\":\"" + "testsupport" +
                "\",\"role\":\"" + "SUPPORT" +
                "\"}";

        mvc
                .perform(put("/api/auth/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeRoleAsJson))
                .andExpect(status().isOk())
                // Response should contain id, name, username, and role
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("name")))
                .andExpect(content().string(containsString("Test Support")))
                .andExpect(content().string(containsString("username")))
                .andExpect(content().string(containsString("testsupport")))
                .andExpect(content().string(containsString("role")))
                // Role should be SUPPORT
                .andExpect(content().string(containsString("SUPPORT")));
    }

    // Test if the support can get a list of all users
    @Test
    @Order(9)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetAllUsers() throws Exception {
        mvc
                .perform(get("/api/auth/list"))
                .andExpect(status().isOk())
                // The response should contain a list of users with the testadmin, testmerchant, and testsupport users
                .andExpect(content().string(containsString("testadmin")))
                .andExpect(content().string(containsString("testmerchant")))
                .andExpect(content().string(containsString("testsupport")));
    }

    // Test if the admin can get a list of all users
    @Test
    @Order(10)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testGetAllUsersAdmin() throws Exception {
        mvc
                .perform(get("/api/auth/list"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("testadmin")))
                .andExpect(content().string(containsString("testmerchant")))
                .andExpect(content().string(containsString("testsupport")));
    }

    // Test if the merchant can't get a list of all users
    @Test
    @Order(11)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testGetAllUsersMerchant() throws Exception {
        mvc
                .perform(get("/api/auth/list"))
                .andExpect(status().isForbidden());
    }

    // Test if the merchant can post a new transaction
    @Test
    @Order(12)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostTransaction() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "800" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "4000008449433403" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13T14:34:41" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isOk())
                // The response should contain the result and info fields
                .andExpect(content().string(containsString("result")))
                // Should be ALLOWED
                .andExpect(content().string(containsString("PROHIBITED")))
                .andExpect(content().string(containsString("info")))
                // Should be 'amount'
                .andExpect(content().string(containsString("amount")));
    }

    // Test if the merchant can't post a new transaction with an invalid date
    @Test
    @Order(13)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostTransactionInvalidDate() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "100" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "4000008449433403" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't post a new transaction with an invalid amount
    @Test
    @Order(14)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostTransactionInvalidAmount() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "4000008449433403" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't post a new transaction with an invalid ip
    @Test
    @Order(15)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostTransactionInvalidIp() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "100" +
                "\",\"ip\":\"" + "" +
                "\",\"number\":\"" + "4000008449433403" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't post a new transaction with an invalid number
    @Test
    @Order(16)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostTransactionInvalidNumber() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "100" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "1234567891011121" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't post a new transaction with an invalid region
    @Test
    @Order(17)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostTransactionInvalidRegion() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "100" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "4000008449433403" +
                "\",\"region\":\"" + "ABC" +
                "\",\"date\":\"" + "2022-10-13" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the support can't post a new transaction
    @Test
    @Order(18)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostTransactionSupport() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "100" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "4000008449433403" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13T14:34:41" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the admin can't post a new transaction
    @Test
    @Order(19)
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void testPostTransactionAdmin() throws Exception {
        String transactionAsJson = "{" +
                "\"amount\":\"" + "100" +
                "\",\"ip\":\"" + "127.0.0.1" +
                "\",\"number\":\"" + "1298979375130220" +
                "\",\"region\":\"" + "ECA" +
                "\",\"date\":\"" + "2022-10-13T14:34:41" +
                "\"}";

        mvc
                .perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the support can post a new suspicious ip
    @Test
    @Order(20)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostSuspiciousIpSupport() throws Exception {
        String suspiciousIpAsJson = "{" +
                "\"ip\":\"" + "127.127.127.127"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suspiciousIpAsJson))
                .andExpect(status().isOk())
                // Response should contain an id and the ip field
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("ip")))
                // Response should contain the ip we sent
                .andExpect(content().string(containsString("127.127.127.127")));
    }

    // Test if the support can't post a new suspicious ip with an invalid ip
    @Test
    @Order(21)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostSuspiciousIpInvalidIp() throws Exception {
        String suspiciousIpAsJson = "{" +
                "\"ip\":\"" + ""
                + "\"}";

        mvc
                .perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suspiciousIpAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the support can get a list of suspicious ips
    @Test
    @Order(22)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetSuspiciousIpsSupport() throws Exception {
        mvc
                .perform(get("/api/antifraud/suspicious-ip"))
                .andExpect(status().isOk())
                // Response should contain an id and the ip field
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("ip")))
                // Response should contain the ip we sent earlier
                .andExpect(content().string(containsString("127.127.127.127")));
    }

    // Test if the support can delete a suspicious ip
    @Test
    @Order(23)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteSuspiciousIpSupport() throws Exception {
        String badIp = "127.127.127.127";

        mvc
                .perform(delete("/api/antifraud/suspicious-ip/" + badIp))
                .andExpect(status().isOk())
                // Response should contain a status field
                .andExpect(content().string(containsString("status")))
                // Response should contain the status "IP <ip> successfully removed!"
                .andExpect(content().string(containsString("IP " + badIp + " successfully removed!")));
    }

    // Test if the support can't delete a suspicious ip with an invalid ip
    @Test
    @Order(24)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteSuspiciousIpInvalidIp() throws Exception {
        String emptyIp = "";
        String badIp = "123";

        mvc
                .perform(delete("/api/antifraud/suspicious-ip/" + emptyIp))
                .andExpect(status().isMethodNotAllowed());

        mvc
                .perform(delete("/api/antifraud/suspicious-ip/" + badIp))
                .andExpect(status().isBadRequest());
    }

    // Test if the support can't delete a suspicious ip with an ip that doesn't exist
    @Test
    @Order(25)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteSuspiciousIpIpDoesntExist() throws Exception {
        String badIp = "10.11.12.13";

        mvc
                .perform(delete("/api/antifraud/suspicious-ip/" + badIp))
                .andExpect(status().isNotFound());
    }

    // Test if the merchant can't post a new suspicious ip
    @Test
    @Order(26)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostSuspiciousIpMerchant() throws Exception {
        String suspiciousIpAsJson = "{" +
                "\"ip\":\"" + "127.127.127.127"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suspiciousIpAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the admin can't post a new suspicious ip
    @Test
    @Order(27)
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void testPostSuspiciousIpAdmin() throws Exception {
        String suspiciousIpAsJson = "{" +
                "\"ip\":\"" + "127.127.127.127"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suspiciousIpAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the support can post a new stolen card number
    @Test
    @Order(28)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostStolenCardNumberSupport() throws Exception {
        String stolenCardNumberAsJson = "{" +
                "\"number\":\"" + "3151853279026036"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stolenCardNumberAsJson))
                .andExpect(status().isOk())
                // Response should contain an id and the number field
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("number")))
                // Response should contain the number we sent
                .andExpect(content().string(containsString("3151853279026036")));
    }

    // Test if the support can't post a new stolen card number with an invalid card number
    @Test
    @Order(29)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostStolenCardNumberInvalidCardNumber() throws Exception {
        String stolenCardNumberAsJson = "{" +
                "\"number\":\"" + "1234567891234567"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stolenCardNumberAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the support can get a list of stolen card numbers
    @Test
    @Order(30)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetStolenCardNumbersSupport() throws Exception {
        mvc
                .perform(get("/api/antifraud/stolencard"))
                .andExpect(status().isOk())
                // Response should contain an id and the number field
                .andExpect(content().string(containsString("id")))
                .andExpect(content().string(containsString("number")))
                // Response should contain the number we sent earlier
                .andExpect(content().string(containsString("3151853279026036")));
    }

    // Test if the support can delete a stolen card number
    @Test
    @Order(31)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteStolenCardNumberSupport() throws Exception {
        String badCardNumber = "3151853279026036";

        mvc
                .perform(delete("/api/antifraud/stolencard/" + badCardNumber))
                .andExpect(status().isOk())
                // Response should contain a status field
                .andExpect(content().string(containsString("status")))
                // Response should contain the status "Card <number> successfully removed!"
                .andExpect(content().string(containsString("Card " + badCardNumber + " successfully removed!")));
    }

    // Test if the support can't delete a stolen card number with an invalid card number
    @Test
    @Order(32)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteStolenCardNumberInvalidCardNumber() throws Exception {
        String emptyCardNumber = "";
        String badCardNumber = "1234567891234567";

        mvc
                .perform(delete("/api/antifraud/stolencard/" + emptyCardNumber))
                .andExpect(status().isMethodNotAllowed());

        mvc
                .perform(delete("/api/antifraud/stolencard/" + badCardNumber))
                .andExpect(status().isBadRequest());
    }

    // Test if the support can't delete a stolen card number with a card number that doesn't exist
    @Test
    @Order(33)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteStolenCardNumberCardNumberDoesntExist() throws Exception {
        String badCardNumber = "1234567899876543";

        mvc
                .perform(delete("/api/antifraud/stolencard/" + badCardNumber))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't post a new stolen card number
    @Test
    @Order(34)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPostStolenCardNumberMerchant() throws Exception {
        String stolenCardNumberAsJson = "{" +
                "\"number\":\"" + "3151853279026036"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stolenCardNumberAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the admin can't post a new stolen card number
    @Test
    @Order(35)
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void testPostStolenCardNumberAdmin() throws Exception {
        String stolenCardNumberAsJson = "{" +
                "\"number\":\"" + "3151853279026036"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stolenCardNumberAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the support can provide feedback on a transaction
    @Test
    @Order(36)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPutFeedbackSupport() throws Exception {
        // Set the latest transaction in the database as transaction id
        String transactionId = String.valueOf(transactionRepository
                .findAll().get(transactionRepository.findAll().size() - 1).getId());

        String feedbackAsJson = "{" +
                "\"transactionId\":\"" + transactionId +
                "\",\"feedback\":\"" + "ALLOWED" +
                "\"}";

        mvc
                .perform(put("/api/antifraud/transaction/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackAsJson))
                .andExpect(status().isOk())
                // Response should contain the
                // transactionId, amount, ip, number, region, date, result and feedback fields
                .andExpect(content().string(containsString("transactionId")))
                .andExpect(content().string(containsString("amount")))
                .andExpect(content().string(containsString("ip")))
                .andExpect(content().string(containsString("number")))
                .andExpect(content().string(containsString("region")))
                .andExpect(content().string(containsString("date")))
                .andExpect(content().string(containsString("result")))
                .andExpect(content().string(containsString("feedback")))
                // Response should contain the feedback we sent earlier
                .andExpect(content().string(containsString("ALLOWED")));
    }

    // Test if the support can't provide feedback on a transaction with an invalid transaction id
    @Test
    @Order(37)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPutFeedbackInvalidTransactionId() throws Exception {
        String transactionId = "0";
        String feedbackAsJson = "{" +
                "\"transactionId\":\"" + transactionId +
                "\",\"feedback\":\"" + "PROHIBITED" +
                "\"}";

        mvc
                .perform(put("/api/antifraud/transaction/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackAsJson))
                .andExpect(status().isNotFound());
    }

    // Test if the support can't provide feedback on a transaction with an invalid feedback
    @Test
    @Order(38)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPutFeedbackInvalidFeedback() throws Exception {
        // Set the latest transaction in the database as transaction id
        String transactionId = String.valueOf(transactionRepository
                .findAll().get(transactionRepository.findAll().size() - 1).getId());

        String feedbackAsJson = "{" +
                "\"transactionId\":\"" + transactionId +
                "\",\"feedback\":\"" + "INVALID" +
                "\"}";

        mvc
                .perform(put("/api/antifraud/transaction/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackAsJson))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't provide feedback on a transaction
    @Test
    @Order(39)
    @WithMockUser(username = "testmerchant", roles = {"MERCHANT"})
    void testPutFeedbackMerchant() throws Exception {
        // Set the latest transaction in the database as transaction id
        String transactionId = String.valueOf(transactionRepository
                .findAll().get(transactionRepository.findAll().size() - 1).getId());

        String feedbackAsJson = "{" +
                "\"transactionId\":\"" + transactionId +
                "\",\"feedback\":\"" + "ALLOWED" +
                "\"}";

        mvc
                .perform(put("/api/antifraud/transaction/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the admin can't provide feedback on a transaction
    @Test
    @Order(40)
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void testPutFeedbackAdmin() throws Exception {
        // Set the latest transaction in the database as transaction id
        String transactionId = String.valueOf(transactionRepository
                .findAll().get(transactionRepository.findAll().size() - 1).getId());

        String feedbackAsJson = "{" +
                "\"transactionId\":\"" + transactionId +
                "\",\"feedback\":\"" + "ALLOWED" +
                "\"}";

        mvc
                .perform(put("/api/antifraud/transaction/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackAsJson))
                .andExpect(status().isForbidden());
    }

    // Test if the support can get all transactions
    @Test
    @Order(41)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetAllTransactionsSupport() throws Exception {
        mvc
                .perform(get("/api/antifraud/history/"))
                .andExpect(status().isOk())
                // Response should contain the
                // transactionId, amount, ip, number, region, date, result and feedback fields
                .andExpect(content().string(containsString("transactionId")))
                .andExpect(content().string(containsString("amount")))
                .andExpect(content().string(containsString("ip")))
                .andExpect(content().string(containsString("number")))
                .andExpect(content().string(containsString("region")))
                .andExpect(content().string(containsString("date")))
                .andExpect(content().string(containsString("result")))
                .andExpect(content().string(containsString("feedback")));
    }

    // Test if the support can get all transactions for a specific card number
    @Test
    @Order(42)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetAllTransactionsForCardNumberSupport() throws Exception {
        mvc
                .perform(get("/api/antifraud/history/4000008449433403"))
                .andExpect(status().isOk())
                // Response should contain the transactionId, amount, ip, number, region, date, result and feedback fields
                .andExpect(content().string(containsString("transactionId")))
                .andExpect(content().string(containsString("amount")))
                // Should contain the card number we sent earlier in test #15
                .andExpect(content().string(containsString("800")))
                .andExpect(content().string(containsString("ip")))
                // Should contain the ip we sent earlier in test #15
                .andExpect(content().string(containsString("127.0.0.1")))
                .andExpect(content().string(containsString("number")))
                // Should contain the card number we sent earlier in test #15
                .andExpect(content().string(containsString("4000008449433403")))
                .andExpect(content().string(containsString("region")))
                // Should contain the region we sent earlier in test #15
                .andExpect(content().string(containsString("ECA")))
                .andExpect(content().string(containsString("date")))
                // Should contain the date we sent earlier in test #15
                .andExpect(content().string(containsString("2022-10-13T14:34:41")))
                .andExpect(content().string(containsString("result")))
                // Should contain the result we sent earlier in test #34
                .andExpect(content().string(containsString("ALLOWED")))
                .andExpect(content().string(containsString("feedback")))
                // Should contain the feedback we sent earlier in test #35
                .andExpect(content().string(containsString("amount")));
    }

    // Test if a user can be deleted and clean up the database
    @Test
    @Order(43)
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRATOR"})
    void testUserDeletion() throws Exception {
        mvc
                .perform(delete("/api/auth/user/testmerchant"))
                .andExpect(status().isOk())
                // Response should contain username and status fields
                .andExpect(content().string(containsString("username")))
                // Username should be the same as the one we sent earlier
                .andExpect(content().string(containsString("testmerchant")))
                .andExpect(content().string(containsString("status")))
                // Status should be "Deleted successfully!"
                .andExpect(content().string(containsString("Deleted successfully!")));

        mvc
                .perform(delete("/api/auth/user/testsupport"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("username")))
                .andExpect(content().string(containsString("testsupport")))
                .andExpect(content().string(containsString("status")))
                .andExpect(content().string(containsString("Deleted successfully!")));
    }

    // Test if the latest transaction in the database is deleted (for cleanup)
    @Test
    @Order(44)
    void testTransactionDeletion() {
        // Set the latest transaction in the database as transaction id
        String transactionId = String.valueOf(transactionRepository
                .findAll().get(transactionRepository.findAll().size() - 1).getId());

        transactionRepository.deleteById(Long.parseLong(transactionId));
        assertThat(transactionRepository.findById(Long.parseLong(transactionId))).isEmpty();
    }

    // Test if the admin can be removed from the database (for cleanup)
    @Test
    @Order(45)
    void removeAdminUser() {
        userService.deleteUser(admin.getUsername());
        assertThat(userRepository.findByUsername(admin.getUsername())).isNull();
    }
}
