package com.Bank;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Главная");
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Вход");
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Регистрация");
        return "auth/register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Личный кабинет");
        return "dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("pageTitle", "Мои счета");
        return "accounts/list";
    }

    @GetMapping("/transfer")
    public String transfer(Model model) {
        model.addAttribute("pageTitle", "Перевод средств");
        return "transfer/transfer";
    }
    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("pageTitle", "История операций");
        return "transactions";
    }
}