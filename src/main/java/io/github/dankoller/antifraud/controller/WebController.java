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

    // TODO: Add merchant, support and admin pages
}
