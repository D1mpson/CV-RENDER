package com.example.cvgenerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Назва шаблону є обовʼязковою")
    private String name;

    private String description;

    @NotBlank(message = "Шлях до HTML файлу шаблону є обовʼязковим")
    private String htmlPath;

    private String previewImagePath;

    @OneToMany(mappedBy = "template")
    private List<CV> cvList = new ArrayList<>();
}