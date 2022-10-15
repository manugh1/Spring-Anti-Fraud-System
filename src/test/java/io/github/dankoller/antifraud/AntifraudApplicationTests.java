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

    // Test if the merchant can post a new transaction
    @Test
    @Order(10)
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
                .andExpect(status().isOk());
    }

    // Test if the merchant can't post a new transaction with an invalid date
    @Test
    @Order(11)
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
    @Order(12)
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
    @Order(13)
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
    @Order(14)
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
    @Order(15)
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
    @Order(16)
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
    @Order(17)
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
    @Order(18)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostSuspiciousIpSupport() throws Exception {
        String suspiciousIpAsJson = "{" +
                "\"ip\":\"" + "127.127.127.127"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(suspiciousIpAsJson))
                .andExpect(status().isOk());
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
    @Order(19)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetSuspiciousIpsSupport() throws Exception {
        mvc
                .perform(get("/api/antifraud/suspicious-ip"))
                .andExpect(status().isOk());
    }

    // Test if the support can delete a suspicious ip
    @Test
    @Order(20)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteSuspiciousIpSupport() throws Exception {
        String badIp = "127.127.127.127";

        mvc
                .perform(delete("/api/antifraud/suspicious-ip/" + badIp))
                .andExpect(status().isOk());
    }

    // Test if the support can't delete a suspicious ip with an invalid ip
    @Test
    @Order(22)
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
    @Order(23)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteSuspiciousIpIpDoesntExist() throws Exception {
        String badIp = "10.11.12.13";

        mvc
                .perform(delete("/api/antifraud/suspicious-ip/" + badIp))
                .andExpect(status().isNotFound());
    }

    // Test if the merchant can't post a new suspicious ip
    @Test
    @Order(24)
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
    @Order(25)
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
    @Order(26)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testPostStolenCardNumberSupport() throws Exception {
        String stolenCardNumberAsJson = "{" +
                "\"number\":\"" + "3151853279026036"
                + "\"}";

        mvc
                .perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stolenCardNumberAsJson))
                .andExpect(status().isOk());
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
    @Order(27)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testGetStolenCardNumbersSupport() throws Exception {
        mvc
                .perform(get("/api/antifraud/stolencard"))
                .andExpect(status().isOk());
    }

    // Test if the support can delete a stolen card number
    @Test
    @Order(28)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteStolenCardNumberSupport() throws Exception {
        String badCardNumber = "3151853279026036";

        mvc
                .perform(delete("/api/antifraud/stolencard/" + badCardNumber))
                .andExpect(status().isOk());
    }

    // Test if the support can't delete a stolen card number with an invalid card number
    @Test
    @Order(30)
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
    @Order(31)
    @WithMockUser(username = "testsupport", roles = {"SUPPORT"})
    void testDeleteStolenCardNumberCardNumberDoesntExist() throws Exception {
        String badCardNumber = "1234567899876543";

        mvc
                .perform(delete("/api/antifraud/stolencard/" + badCardNumber))
                .andExpect(status().isBadRequest());
    }

    // Test if the merchant can't post a new stolen card number
    @Test
    @Order(32)
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
    @Order(33)
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
    @Order(34)
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
                .andExpect(status().isOk());
    }

    // Test if the support can't provide feedback on a transaction with an invalid transaction id
    @Test
    @Order(35)
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
    @Order(36)
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
    @Order(37)
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
    @Order(38)
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

    // TODO: Add tests for the response bodies of the endpoints

    // Test if a user can be deleted and clean up the database
    @Test
    @Order(39)
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

    // Test if the latest transaction in the database is deleted (for cleanup)
    @Test
    @Order(40)
    void testTransactionDeletion() {
        // Set the latest transaction in the database as transaction id
        String transactionId = String.valueOf(transactionRepository
                .findAll().get(transactionRepository.findAll().size() - 1).getId());

        transactionRepository.deleteById(Long.parseLong(transactionId));
        assertThat(transactionRepository.findById(Long.parseLong(transactionId))).isEmpty();
    }

    // Test if the admin can be removed from the database (for cleanup)
    @Test
    @Order(41)
    void removeAdminUser() {
        userService.deleteUser(admin.getUsername());
        assertThat(userRepository.findByUsername(admin.getUsername())).isNull();
    }
}
