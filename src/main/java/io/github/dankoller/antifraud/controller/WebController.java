package io.github.dankoller.antifraud.controller;

import io.github.dankoller.antifraud.entity.Card;
import io.github.dankoller.antifraud.entity.IPAddress;
import io.github.dankoller.antifraud.entity.transaction.Transaction;
import io.github.dankoller.antifraud.entity.user.User;
import io.github.dankoller.antifraud.persistence.TransactionRepository;
import io.github.dankoller.antifraud.response.UserDataResponse;
import io.github.dankoller.antifraud.service.TransactionService;
import io.github.dankoller.antifraud.service.UserService;
import io.github.dankoller.antifraud.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/web")
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    // Using request mapping instead of get mapping because of the redirects in the html

    @RequestMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/about")
    public String getAboutPage() {
        return "about";
    }

    @GetMapping("/contact")
    public String getContactPage() {
        return "contact";
    }

    // Merchant view related endpoints
    @RequestMapping("/merchant")
    public String getMerchantPage() {
        return "merchant";
    }

    @RequestMapping("/merchant/new-transaction")
    public String getMerchantTransactionPage() {
        return "/merchant/new-transaction";
    }

    // Support view related endpoints
    @RequestMapping("/support")
    public String getSupportPage() {
        return "support";
    }

    @RequestMapping("/support/list-users")
    public String getListPage(Model model) {
        List<UserDataResponse> userDataResponses = userService.getUserDataList();
        model.addAttribute("users", userDataResponses);
        return "/support/list-users";
    }

    @RequestMapping("/support/manage-ip")
    public String getManageIpPage(Model model) {
        List<IPAddress> suspiciousIPs = (List<IPAddress>) validationService.getSuspiciousIPs();
        model.addAttribute("ips", suspiciousIPs);
        return "/support/manage-ip";
    }

    @RequestMapping("/support/manage-stolencard")
    public String getManageStolenCardPage(Model model) {
        List<Card> stolenCards = (List<Card>) validationService.getStolenCards();
        model.addAttribute("cards", stolenCards);
        return "/support/manage-stolencard";
    }

    @RequestMapping("/support/review-transaction")
    public String getReviewTransactionPage(Model model, Principal principal) {
        List<Transaction> transactions = transactionRepository.findAll();

        // Filter out transactions that have already been reviewed (= !feedback.isEmpty())
        transactions.removeIf(transaction -> !transaction.getFeedback().isEmpty());

        // Remove every transaction but the first 10
        transactions.subList(10, transactions.size()).clear();

        model.addAttribute("transactions", transactions);

        // Get the username and role of the currently logged-in user
        String username = principal.getName();
        model.addAttribute("username", username);

        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String userrole = authorities.iterator().next().getAuthority();
        // Remove ROLE_ from the role
        userrole = userrole.substring(5);
        // Make the first letter uppercase
        userrole = userrole.substring(0, 1).toUpperCase() + userrole.substring(1).toLowerCase();

        model.addAttribute("userrole", userrole);

        return "/support/review-transaction";
    }

    @RequestMapping("/support/transaction-history")
    public String getTransactionHistoryPage(Model model,
                                            @RequestParam(value = "cardnumber", required = false) String cardNumber) {
        // If no card number is given, return the page with a full list of transactions
        List<Transaction> transactions;
        if (cardNumber == null || cardNumber.isEmpty()) {
            transactions = transactionRepository.findAll();
        } else {
            // If a card number is given, return the page with a filtered list of transactions
            transactions = transactionService.getTransactionHistory(cardNumber);
        }
        model.addAttribute("transactions", transactions);
        return "/support/transaction-history";
    }

    // Admin view related endpoints
    @RequestMapping("/admin")
    public String getAdminPage() {
        return "admin";
    }

    @RequestMapping("/admin/new-user")
    public String getNewUserPage() {
        return "/admin/new-user";
    }

    @RequestMapping("/admin/delete-user")
    public String getDeleteUserPage() {
        return "/admin/delete-user";
    }

    @RequestMapping("/admin/update-role")
    public String getUpdateRolePage() {
        return "/admin/update-role";
    }

    @RequestMapping("/admin/update-access")
    public String getUpdateAccessPage() {
        return "/admin/update-access";
    }

}
