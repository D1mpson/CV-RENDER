package com.example.cvgenerator.aspect;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.*;

@Aspect
@Component
public class SmartTipsAspect {

    private final Logger logger = LoggerFactory.getLogger(SmartTipsAspect.class);
    private final UserService userService;
    private final CVService cvService;

    @Autowired
    public SmartTipsAspect(UserService userService, CVService cvService) {
        this.userService = userService;
        this.cvService = cvService;
    }

    @Around("execution(* com.example.cvgenerator.controller.UserController.showUserProfile(..))")
    public Object addSmartTipsToProfile(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs(); //<-- Взяли агрументи методу
        Model model = null;

        for (Object arg : args) {
            if (arg instanceof Model) {    //<-- шукаэмо Model серед arg
                model = (Model) arg;
                break;
            }
        }

        if (model != null) {
            try {
                User currentUser = userService.getCurrentUser();
                if (currentUser != null) {

                    List<CV> userCVs = cvService.getAllCVsByUser(currentUser);

                    Map<String, String> smartTips = generateSmartTips(currentUser, userCVs);  //<-- Генеруємо персоналізовані підказки

                    model.addAttribute("smartTips", smartTips);

                    logger.info("Згенеровано {} підказок для користувача {}",
                            smartTips.size(), currentUser.getEmail());
                }
            } catch (Exception e) {
                logger.error("Помилка під час генерації підказок: {}", e.getMessage());
            }
        }

        return joinPoint.proceed(args);
    }

    private Map<String, String> generateSmartTips(User user, List<CV> cvList) {
        Map<String, String> tips = new LinkedHashMap<>();

        // 1. Перевірка чи є CV
        if (cvList.isEmpty()) {
            tips.put("create_first_cv", "Створіть своє перше CV! Роботодавці частіше розглядають кандидатів з добре оформленим резюме.");
            return tips; // Повертаємо лише цю пораду, якщо CV ще немає
        }

        // 2. Перевірка "Про мене"
        boolean hasDetailedAboutMe = cvList.stream()
                .anyMatch(cv -> cv.getAboutMe() != null && cv.getAboutMe().length() > 200);
        if (!hasDetailedAboutMe) {
            tips.put("improve_about_me", "Детальна секція 'Про мене' збільшує шанси отримати запрошення на інтерв'ю.");
        }

        // 3. Перевірка чи є фото
        boolean hasPhoto = cvList.stream()
                .anyMatch(cv -> cv.getPhotoPath() != null && !cv.getPhotoPath().isEmpty());
        if (!hasPhoto) {
            tips.put("add_photo", "CV з фото отримують більше переглядів. Додайте фото для кращого враження.");
        }

        // 4. Перевірка хард-скілів
        boolean hasDetailedHardSkills = cvList.stream()
                .anyMatch(cv -> cv.getHardSkills() != null &&
                        (cv.getHardSkills().split(",").length > 5 || cv.getHardSkills().length() > 100));
        if (!hasDetailedHardSkills) {
            tips.put("enhance_hard_skills", "Вкажіть декілька конкретних технічних навичок. Це допомагає пройти фільтрацію резюме.");
        }

        // 5. Перевірка мов
        boolean hasLanguages = cvList.stream()
                .anyMatch(cv -> cv.getKnownLanguages() != null && !cv.getKnownLanguages().isEmpty());
        if (!hasLanguages) {
            tips.put("add_languages", "Кандидати, які знають іноземних мови, мають більше шансів отримати вищу зарплату.");
        }

        // 6. Перевірка досвіду роботи
        boolean hasDetailedWorkExperience = cvList.stream()
                .anyMatch(cv -> cv.getWorkExperience() != null && cv.getWorkExperience().length() > 300);
        if (!hasDetailedWorkExperience) {
            tips.put("detail_work_experience", "Описуйте досвід роботи з конкретними досягненнями. Це привертає увагу.");
        }

        // 7. Перевірка англомовного CV
        boolean hasEnglishCV = cvList.stream()
                .anyMatch(cv -> "en".equals(cv.getLanguage()));
        if (!hasEnglishCV) {
            tips.put("create_english_cv", "CV англійською мовою відкриває доступ до міжнародних вакансій.");
        }

        // 8. Перевірка портфоліо
        boolean hasPortfolio = cvList.stream()
                .anyMatch(cv -> cv.getPortfolioLinks() != null && !cv.getPortfolioLinks().isEmpty());
        if (!hasPortfolio) {
            tips.put("add_portfolio", "Додайте посилання на проекти або портфоліо. Це збільшує ймовірність запрошення на інтервʼю.");
        }

        return tips;
    }
}