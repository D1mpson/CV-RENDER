package com.example.cvgenerator.config;

import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.TemplateService;
import com.example.cvgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TemplateService templateService;
    private final UserService userService;

    @Autowired
    public DataInitializer(TemplateService templateService, UserService userService) {
        this.templateService = templateService;
        this.userService = userService;
        System.out.println("DataInitializer: Конструктор викликано, залежності впроваджено");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer: CommandLineRunner викликано");

        try {
            // Створюю 3 стандартні шаблони, якщо шаблони відсутні в БД
            if (templateService.getAllTemplates().isEmpty()) {
                System.out.println("DataInitializer: Створення стандартних шаблонів");
                createDefaultTemplates();
            } else {
                System.out.println("DataInitializer: Шаблони вже існують (" + templateService.getAllTemplates().size() + " шт.)");
            }

            // Якщо користувачів немає, створюю Адміністратора
            if (userService.getAllUsers().isEmpty()) {
                System.out.println("DataInitializer: Створення адміністратора");
                createAdmin();
            } else {
                System.out.println("DataInitializer: Користувачі вже існують (" + userService.getAllUsers().size() + " шт.)");
            }

            System.out.println("DataInitializer: Ініціалізація даних завершена успішно");

        } catch (Exception e) {
            System.err.println("DataInitializer: Помилка під час ініціалізації: " + e.getMessage());
            e.printStackTrace();
            // Не кидаємо винятку, щоб не зупиняти запуск додатка
        }
    }

    private void createDefaultTemplates() {
        try {
            Template template1 = new Template();
            template1.setName("Professional");
            template1.setDescription("Класичний професійний шаблон, підходить для більшості галузей");
            template1.setHtmlPath("cv-1");
            template1.setPreviewImagePath("/images/generator/Professional.png");
            templateService.saveTemplate(template1);
            System.out.println("DataInitializer: ✅ Створено шаблон Professional");

            Template template2 = new Template();
            template2.setName("Creative");
            template2.setDescription("Сучасний креативний шаблон для дизайнерів та креативних професій");
            template2.setHtmlPath("cv-2");
            template2.setPreviewImagePath("/images/generator/Creative.png");
            templateService.saveTemplate(template2);
            System.out.println("DataInitializer: ✅ Створено шаблон Creative");

            Template template3 = new Template();
            template3.setName("Academic");
            template3.setDescription("Формальний шаблон для академічних та наукових позицій");
            template3.setHtmlPath("cv-3");
            template3.setPreviewImagePath("/images/generator/Academic.png");
            templateService.saveTemplate(template3);
            System.out.println("DataInitializer: ✅ Створено шаблон Academic");

        } catch (Exception e) {
            System.err.println("DataInitializer: ❌ Помилка при створенні шаблонів: " + e.getMessage());
        }
    }

    private void createAdmin() {
        try {
            if (userService.findByRole("ROLE_ADMIN").isEmpty()) {
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setEmail("admin@system.com");
                admin.setPassword("admin123");
                admin.setPhoneNumber("+380000000000");
                admin.setBirthDate(LocalDate.now().minusYears(18));
                admin.setCityLife("City");
                admin.setRole("ROLE_ADMIN");
                // Адмін автоматично верифікований
                admin.setEmailVerified(true);

                userService.saveUser(admin);
                System.out.println("DataInitializer: ✅ Створено адміністратора (email: admin@system.com, пароль: admin123)");
            } else {
                System.out.println("DataInitializer: ℹ️ Адміністратор вже існує");
            }
        } catch (Exception e) {
            System.err.println("DataInitializer: ❌ Помилка при створенні адміністратора: " + e.getMessage());
        }
    }
}