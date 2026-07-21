package com.beyzanur.chingu_ai.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";  // Sadece mor login.html ekranını açar
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            return "redirect:/chingu/ui";  // Giriş yapılmışsa direkt chat'e uçur
        }
        return "redirect:/login";  // Giriş yoksa giriş sayfasına yönlendir
    }
}