package io.github.dankoller.antifraud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class WebController {

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

    // Support view related endpoints
    @RequestMapping("/support")
    public String getSupportPage() {
        return "support";
    }

    @RequestMapping("/list")
    public String getListPage() {
        // TODO: Add user list to model
        return "list";
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
