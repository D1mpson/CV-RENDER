package com.example.cvgenerator.service;

import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    // Додавання нового шаблону (для адміністратора)
    public Template saveTemplate(Template template) {
        return templateRepository.save(template);
    }

    // Видалення шаблону (для адміністратора)
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    // Редагування шаблону (для адміністратора)
    public Template updateTemplate(Template template) {
        Template existingTemplate = templateRepository.findById(template.getId())
                .orElseThrow(() -> new RuntimeException("Шаблон з ID " + template.getId() + " не знайдено"));

        existingTemplate.setName(template.getName());
        existingTemplate.setDescription(template.getDescription());
        existingTemplate.setHtmlPath(template.getHtmlPath());
        existingTemplate.setPreviewImagePath(template.getPreviewImagePath());

        return templateRepository.save(existingTemplate);
    }
}