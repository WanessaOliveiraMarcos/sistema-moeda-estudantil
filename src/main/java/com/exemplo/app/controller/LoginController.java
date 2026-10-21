package com.exemplo.app.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // o POST /login é tratado pelo Spring Security (SecurityConfig)
    @GetMapping("/login")
    public String login(Authentication autenticacao) {
        if (autenticacao != null && !(autenticacao instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }
}
