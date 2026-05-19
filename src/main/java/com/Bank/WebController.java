package com.Bank;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for web page routing (Thymeleaf).
 * <p>
 * Responsible for displaying HTML pages, unlike REST controllers,
 * which return JSON. All methods return the name of the template
 * that will be processed by Thymeleaf. * </p>
 *
 * <p><b>Available routes:</b></p>
 * <ul>
 * <li>{@code GET /} → home page (index.html)</li>
 * <li>{@code GET /login} → login page (login.html)</li>
 * <li>{@code GET /register} → registration page (register.html)</li>
 * <li>{@code GET /dashboard} → personal account (dashboard.html)</li>
 * <li>{@code GET /accounts} → account list (accounts/list.html)</li>
 * <li>{@code GET /transfer} → transfer form (transfer/transfer.html)</li>
 * <li>{@code GET /transactions} → transaction history (transactions.html)</li>
 * </ul>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see com.Bank.AuthController
 * @since 04-2026
 */
@Controller
public class WebController {

    /**
     * Landing page.
     *
     * @param model model for passing data to the template
     * @return the name of the Thymeleaf template "index"
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "index";
    }

    /**
     * Login page. *
     * @return the Thymeleaf template name "auth/login"
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "auth/login";
    }

    /**
     * New user registration page.
     *
     * @return the Thymeleaf template name "auth/register"
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Registration");
        return "auth/register";
    }

    /**
     * User account (dashboard).
     * Access only for authenticated users. *
     * @return the name of the Thymeleaf template "dashboard"
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Personal Account");
        return "dashboard";
    }

    /**
     * Page with a list of bank accounts.
     *
     * @return the name of the Thymeleaf template "accounts/list"
     */
    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("pageTitle", "My Accounts");
        return "accounts/list";
    }

    /**
     * Page for transferring funds between accounts. *
     * @return the name of the Thymeleaf template "transfer/transfer"
     */
    @GetMapping("/transfer")
    public String transfer(Model model) {
        model.addAttribute("pageTitle", "Transfer");
        return "transfer/transfer";
    }

    /**
     * The transaction history page.
     *
     * @return the name of the Thymeleaf template "transactions"
     */
    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("pageTitle", "Transaction History");
        return "transactions";
    }
}
