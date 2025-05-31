package com.example.cvgenerator.controller;

import com.example.cvgenerator.controller.util.CVHelper;
import com.example.cvgenerator.controller.util.UserHelper;
import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CVService cvService;
    private final UserHelper userHelper;
    private final CVHelper cvHelper;


    @Autowired
    public AdminController(UserService userService, CVService cvService, UserHelper userHelper, CVHelper cvHelper) {
        this.userService = userService;
        this.cvService = cvService;
        this.userHelper = userHelper;
        this.cvHelper = cvHelper;
    }

    @GetMapping
    public String showAdminPanel(Model model, Authentication authentication) {
        try {
            System.out.println("🔍 AdminController: Начало метода showAdminPanel");
            System.out.println("   Текущий пользователь: " + authentication.getName());
            System.out.println("   Авторитеты: " + authentication.getAuthorities());

            // Используем новый метод с инициализацией CV
            List<User> users = userService.getAllUsersWithCVCount();
            System.out.println("   Найдено пользователей: " + (users != null ? users.size() : "null"));

            model.addAttribute("users", users);
            System.out.println("✅ AdminController: Успешно подготовлена модель");
            return "admin/admin-panel";

        } catch (Exception e) {
            System.err.println("❌ AdminController error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
            return "admin/admin-panel";
        }
    }

    @GetMapping("/users/{id}")
    public String showUserDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
        List<CV> userCVs = cvService.getAllCVsByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("cvList", userCVs);
        return "admin/user-details";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        User formUser = userHelper.prepareUserForm(user);

        model.addAttribute("user", formUser);
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") User formUser,
                             @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                             Model model) {
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            userHelper.updateUserData(existingUser, formUser);

            //Перевіряємо чи паролі співпадають
            if (formUser.getPassword() != null && !formUser.getPassword().isEmpty()) {
                if (!formUser.getPassword().equals(passwordConfirm)) {
                    model.addAttribute("user", formUser);
                    model.addAttribute("passwordError", "Паролі не співпадають");
                    return "admin/edit-user";
                }

                // Паролі співпадають, оновлюємо пароль
                userService.updatePassword(existingUser, formUser.getPassword());
            }

            userService.saveUser(existingUser);

        } catch (Exception e) {
            return "redirect:/admin";
        }

        return "redirect:/admin";
    }

    // Видалення користувача
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            System.out.println("Помилка видалення користувача з ID " + userService.findById(id).get().getId());
            return "redirect:/admin";
        }
        return "redirect:/admin";
    }

    // Відображення CV користувача
    @GetMapping("/cv/{id}")
    public String viewCV(@PathVariable Long id, Model model) {
        CV cv = cvService.getCVById(id)
                .orElseThrow(() -> new RuntimeException("CV не знайдено"));

        model.addAttribute("cv", cv);
        model.addAttribute("adminView", true);

        return cvHelper.determineTemplateView(cv.getTemplate());
    }

    // Видалення CV
    @PostMapping("/cv/{id}/delete")
    public String deleteCV(@PathVariable Long id) {
        try {
            CV cv = cvService.getCVById(id)
                    .orElseThrow(() -> new RuntimeException("CV не знайдено"));

            Long userId = cv.getUser().getId();
            cvService.deleteCV(id);

            return "redirect:/admin/users/" + userId;
        } catch (Exception e) {
            System.out.println("Помилка видалення " + cvService.getCVById(id));
            return "redirect:/admin";
        }
    }

    @GetMapping("/search")
    public String searchUserByEmail(@RequestParam("email") String email) {
        try {
            Optional<User> userOpt = userService.findByEmailWithJdbc(email);
            if (userOpt.isPresent()) {
                return "redirect:/admin/users/" + userOpt.get().getId();
            } else {
                return "redirect:/admin";
            }
        } catch (Exception e) {
            System.out.println("Помилка пошуку: " + e.getMessage());
            return "redirect:/admin";
        }
    }
}