package com.example.cvgenerator.controller.util;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.service.TemplateService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Component
public class CVHelper {

    private final TemplateService templateService;

    public CVHelper(TemplateService templateService) {
        this.templateService = templateService;
    }

    //Підготовка атрибутів для форми CV
    public void prepareFormAttributes(Model model) {
        model.addAttribute("templates", templateService.getAllTemplates());
        model.addAttribute("fonts", new String[]{"Arial", "Times New Roman", "Calibri", "Verdana", "Roboto"});
    }

    //Отримання та підготовка шаблону
    public Template getAndPrepareTemplate(Long templateId) {
        Template template = templateService.getTemplateById(templateId)
                .orElseThrow(() -> new RuntimeException("Шаблон не знайдено"));

        // Перевірка, що htmlPath не null
        if (template.getHtmlPath() == null) {
            template.setHtmlPath("cv-1");
            templateService.saveTemplate(template);
        }

        return template;
    }

    //Обробка списку для формування текстового поля (через кому)
    public void processListToStringField(List<String> items, Consumer<String> setter) {
        if (items == null || items.isEmpty()) {
            setter.accept("");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append(", ");
                }
                builder.append(item.trim());
            }
        }
        setter.accept(builder.toString());
    }

    //Обробка списку для формування текстового поля з можливістю використання існуючого значення
    public void processListToStringFieldWithFallback(List<String> items, Consumer<String> setter, String fallback) {
        if (items == null || items.isEmpty()) {
            setter.accept(fallback);
            return;
        }

        processListToStringField(items, setter);
    }

    //Обробка списку для формування List<String>
    public void processListItems(List<String> items, Consumer<List<String>> setter) {
        if (items == null || items.isEmpty()) {
            setter.accept(new ArrayList<>());
            return;
        }

        List<String> processedList = new ArrayList<>();
        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) {
                processedList.add(item.trim());
            }
        }

        setter.accept(processedList);
    }

    //Визначення представлення на основі шаблону
    public String determineTemplateView(Template template) {

        if (template == null || template.getHtmlPath() == null || template.getHtmlPath().isEmpty()) {
            return "cv-1";
        }

        String htmlPath = template.getHtmlPath();

        if (htmlPath.contains("cv-2")) {
            return "cv-2";
        } else if (htmlPath.contains("cv-3")) {
            return "cv-3";
        } else if (htmlPath.contains("cv-4")) {
            return "cv-4";
        } else if (htmlPath.contains("cv-5")) {
            return "cv-5";
        } else if (htmlPath.contains("cv-6")) {
            return "cv-6";
        }

        return "cv-1";
    }

    //Обробка всіх текстових полів CV з одного методу
    public void processAllTextFields(CV cv,
                                     List<String> educationItems,
                                     List<String> coursesItems,
                                     List<String> workExperienceItems,
                                     List<String> softSkillsItems,
                                     List<String> hardSkillsItems) {
        processListToStringField(educationItems, cv::setEducation);
        processListToStringField(coursesItems, cv::setCourses);
        processListToStringField(workExperienceItems, cv::setWorkExperience);
        processListToStringField(softSkillsItems, cv::setSoftSkills);
        processListToStringField(hardSkillsItems, cv::setHardSkills);
    }

    //Обробка всіх текстових полів CV з використанням існуючого CV для значень за замовчуванням
    public void processAllTextFieldsWithFallback(CV cv,
                                                 CV existingCV,
                                                 List<String> educationItems,
                                                 List<String> coursesItems,
                                                 List<String> workExperienceItems,
                                                 List<String> softSkillsItems,
                                                 List<String> hardSkillsItems) {
        processListToStringFieldWithFallback(educationItems, cv::setEducation, existingCV.getEducation());
        processListToStringFieldWithFallback(coursesItems, cv::setCourses, existingCV.getCourses());
        processListToStringFieldWithFallback(workExperienceItems, cv::setWorkExperience, existingCV.getWorkExperience());
        processListToStringFieldWithFallback(softSkillsItems, cv::setSoftSkills, existingCV.getSoftSkills());
        processListToStringFieldWithFallback(hardSkillsItems, cv::setHardSkills, existingCV.getHardSkills());
    }

    //Функціональний інтерфейс для обробки текстових полів
    public interface Consumer<T> {
        void accept(T value);
    }
}