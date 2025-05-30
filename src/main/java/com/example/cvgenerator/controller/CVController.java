package com.example.cvgenerator.controller;

import com.example.cvgenerator.controller.util.CVHelper;
import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CVController {

    private final CVService cvService;
    private final UserService userService;
    private final CVHelper cvHelper;

    @Autowired
    public CVController(CVService cvService,
                        UserService userService, CVHelper cvHelper) {
        this.cvService = cvService;
        this.userService = userService;
        this.cvHelper = cvHelper;
    }

    @GetMapping("/all-cv")
    public String showAllCV(Model model) {
        model.addAttribute("cvList", cvService.getAllCVsForCurrentUser());
        return "all-cv";
    }

    @GetMapping("/generator")
    public String showGeneratorForm(Model model) {
        model.addAttribute("cv", new CV());
        cvHelper.prepareFormAttributes(model);
        return "generator";
    }

    @PostMapping("/generator")
    public String createCV(
            @Valid @ModelAttribute("cv") CV cv,
            BindingResult result,
            @RequestParam("templateId") Long templateId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "portfolioLinks", required = false) List<String> portfolioLinks,
            @RequestParam(value = "knownLanguages", required = false) List<String> knownLanguages,
            @RequestParam(value = "educationItems", required = false) List<String> educationItems,
            @RequestParam(value = "coursesItems", required = false) List<String> coursesItems,
            @RequestParam(value = "workExperienceItems", required = false) List<String> workExperienceItems,
            @RequestParam(value = "softSkillsItems", required = false) List<String> softSkillsItems,
            @RequestParam(value = "hardSkillsItems", required = false) List<String> hardSkillsItems,
            @RequestParam(value = "hobbiesItems", required = false) List<String> hobbiesItems,
            RedirectAttributes redirectAttrs,
            Model model) {

        if (result.hasErrors()) {
            cvHelper.prepareFormAttributes(model);
            return "generator";
        }

        try {
            Template template = cvHelper.getAndPrepareTemplate(templateId);
            cv.setTemplate(template);

            cvHelper.processAllTextFields(cv, educationItems, coursesItems,
                    workExperienceItems, softSkillsItems, hardSkillsItems);

            // Додаємо обробку хобі
            cvHelper.processListToStringField(hobbiesItems, cv::setHobbies);

            cvHelper.processListItems(portfolioLinks, cv::setPortfolioLinks);

            if (knownLanguages != null) {
                cv.setKnownLanguages(knownLanguages);
            }

            CV savedCV = cvService.createCV(cv, photo);
            redirectAttrs.addFlashAttribute("success", "CV успішно створено!");
            return "redirect:/cv/" + savedCV.getId();
        } catch (Exception e) {
            result.rejectValue("name", "error.cv", e.getMessage());
            cvHelper.prepareFormAttributes(model);
            return "generator";
        }
    }


    @GetMapping("/cv/{id}")
    public String viewCV(@PathVariable Long id, Model model) {
        CV cv = cvService.getCVById(id)
                .orElseThrow(() -> new RuntimeException("CV не знайдено"));

        if (!cv.getUser().getId().equals(userService.getCurrentUser().getId())) {
            return "redirect:/profile?error=unauthorized";
        }

        model.addAttribute("cv", cv);

        return cvHelper.determineTemplateView(cv.getTemplate());
    }

    @GetMapping("/cv/{id}/edit")
    public String showEditCVForm(@PathVariable Long id, Model model) {
        CV cv = cvService.getCVById(id)
                .orElseThrow(() -> new RuntimeException("CV не знайдено"));

        if (!cv.getUser().getId().equals(userService.getCurrentUser().getId())) {
            return "redirect:/profile?error=unauthorized";
        }

        model.addAttribute("cv", cv);
        cvHelper.prepareFormAttributes(model);

        if (cv.getTemplate() != null) {
            model.addAttribute("selectedTemplateId", cv.getTemplate().getId());
        }

        return "generator";
    }

    @PostMapping("/cv/{id}/edit")
    public String updateCV(
            @PathVariable Long id,
            @Valid @ModelAttribute("cv") CV cv,
            BindingResult result,
            @RequestParam("templateId") Long templateId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "portfolioLinks", required = false) List<String> portfolioLinks,
            @RequestParam(value = "knownLanguages", required = false) List<String> knownLanguages,
            @RequestParam(value = "educationItems", required = false) List<String> educationItems,
            @RequestParam(value = "coursesItems", required = false) List<String> coursesItems,
            @RequestParam(value = "workExperienceItems", required = false) List<String> workExperienceItems,
            @RequestParam(value = "softSkillsItems", required = false) List<String> softSkillsItems,
            @RequestParam(value = "hardSkillsItems", required = false) List<String> hardSkillsItems,
            @RequestParam(value = "hobbiesItems", required = false) List<String> hobbiesItems,
            RedirectAttributes redirectAttrs,
            Model model) {

        if (result.hasErrors()) {
            cvHelper.prepareFormAttributes(model);
            return "generator";
        }

        try {
            CV existingCV = cvService.getCVById(id)
                    .orElseThrow(() -> new RuntimeException("CV не знайдено"));

            cv.setId(id);
            cv.setUser(existingCV.getUser());

            Template template = cvHelper.getAndPrepareTemplate(templateId);
            cv.setTemplate(template);

            cvHelper.processAllTextFieldsWithFallback(cv, existingCV, educationItems, coursesItems,
                    workExperienceItems, softSkillsItems, hardSkillsItems);

            // Додаємо обробку хобі
            cvHelper.processListToStringFieldWithFallback(hobbiesItems, cv::setHobbies, existingCV.getHobbies());

            if (portfolioLinks != null) {
                cvHelper.processListItems(portfolioLinks, cv::setPortfolioLinks);
            } else {
                cv.setPortfolioLinks(existingCV.getPortfolioLinks());
            }

            if (knownLanguages != null) {
                cv.setKnownLanguages(knownLanguages);
            } else {
                cv.setKnownLanguages(existingCV.getKnownLanguages());
            }

            cvService.updateCV(cv, photo);

            redirectAttrs.addFlashAttribute("success", "CV успішно оновлено!");
            return "redirect:/cv/" + id;
        } catch (Exception e) {
            System.out.println("Помилка при оновленні CV: " + e.getMessage());
            e.printStackTrace();
            cvHelper.prepareFormAttributes(model);
            return "generator";
        }
    }

    @PostMapping("/cv/{id}/delete")
    public String deleteCV(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            cvService.deleteCV(id);
            redirectAttrs.addFlashAttribute("success", "CV успішно видалено!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/all-cv";
    }
}