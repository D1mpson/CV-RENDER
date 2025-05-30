package com.example.cvgenerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cvs")
public class CV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Назва CV є обовʼязковою")
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    private String photoPath;

    @Size(max = 1000, message = "Опис 'про себе' не може бути більшим за 1000 символів")
    @Column(length = 1000)
    private String aboutMe;

    @Size(max = 500, message = "Опис хобі не може бути більшим за 500 символів")
    @Column(length = 500)
    private String hobbies;

    // Змінено з тексту на список посилань
    @ElementCollection
    @CollectionTable(name = "cv_portfolio_links", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "link", length = 500)
    private List<String> portfolioLinks = new ArrayList<>();

    // Нові поля
    @Size(max = 500, message = "Опис софт скілів не може бути більшим за 500 символів")
    @Column(length = 500)
    private String softSkills;

    @Size(max = 500, message = "Опис хард скілів не може бути більшим за 500 символів")
    @Column(length = 500)
    private String hardSkills;

    // Знання мов
    @ElementCollection
    @CollectionTable(name = "cv_languages", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "language_name", length = 100)
    private List<String> knownLanguages = new ArrayList<>();

    @Size(max = 300, message = "Поле для інших мов не може бути більшим за 300 символів")
    @Column(length = 300)
    private String otherLanguages;

    // Освіта
    @Size(max = 1000, message = "Опис освіти не може бути більшим за 1000 символів")
    @Column(length = 1000)
    private String education;

    // Курси
    @Size(max = 1000, message = "Опис курсів не може бути більшим за 1000 символів")
    @Column(length = 1000)
    private String courses;

    // Досвід роботи
    @Size(max = 1500, message = "Опис досвіду роботи не може бути більшим за 1500 символів")
    @Column(length = 1500)
    private String workExperience;

    private String font;

    private String language = "uk"; // uk - українська, en - англійська

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}