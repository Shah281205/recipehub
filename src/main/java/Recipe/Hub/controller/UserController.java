package Recipe.Hub.controller;

import Recipe.Hub.model.User;
import Recipe.Hub.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // =========================
    // REGISTER
    // =========================

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role) {

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        userRepository.save(user);

        return "redirect:/register";
    }


    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {

        User user =
                userRepository.findByEmail(email);

        if (user != null &&
                user.getPassword().equals(password)) {

            session.setAttribute("user", user);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/dashboard";
        }

        return "redirect:/login";
    }


    // =========================
    // DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        return "dashboard";
    }


    // =========================
    // PROFILE
    // =========================

    @GetMapping("/profile")
    public String profile(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        return "profile";
    }


    // =========================
    // UPDATE NAME + EMAIL
    // =========================

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        user.setName(name);
        user.setEmail(email);

        userRepository.save(user);

        session.setAttribute("user", user);

        return "redirect:/profile";
    }


    // =========================
    // CHANGE PASSWORD
    // =========================

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }


        // Check current password

        if (!user.getPassword().equals(currentPassword)) {

            model.addAttribute(
                    "error",
                    "Current password is incorrect."
            );

            model.addAttribute("user", user);

            return "profile";
        }


        // Check new password

        if (newPassword.length() < 6) {

            model.addAttribute(
                    "error",
                    "New password must contain at least 6 characters."
            );

            model.addAttribute("user", user);

            return "profile";
        }


        // Confirm password

        if (!newPassword.equals(confirmPassword)) {

            model.addAttribute(
                    "error",
                    "New password and confirm password do not match."
            );

            model.addAttribute("user", user);

            return "profile";
        }


        // Save new password

        user.setPassword(newPassword);

        userRepository.save(user);

        session.setAttribute("user", user);


        model.addAttribute(
                "success",
                "Password changed successfully."
        );

        model.addAttribute("user", user);

        return "profile";
    }


    // =========================
    // LOGOUT
    // =========================

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }

}