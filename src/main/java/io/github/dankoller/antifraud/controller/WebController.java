package io.github.dankoller.antifraud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class WebController {

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/success")
    public String getSignupPage() {
        return "success";
    }

    @PostMapping("/success")
    public String postSignupPage() {
        return "success";
    }

    @GetMapping("/merchant")
    public String getMerchantPage() {
        return "merchant";
    }

    @GetMapping("/support")
    public String getSupportPage() {
        return "support";
    }

    @GetMapping("/admin")
    public String getAdminPage() {
        return "admin";
    }
}
