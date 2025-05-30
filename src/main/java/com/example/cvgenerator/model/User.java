package com.example.cvgenerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Імʼя є обовʼязковим")
    private String firstName;

    @NotBlank(message = "Прізвище є обовʼязковим")
    private String lastName;

    @NotBlank(message = "Email є обовʼязковим")
    @Email(message = "Некоректний формат email")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Пароль є обовʼязковим")
    @Size(min = 6, message = "Пароль повинен містити не менше 6 символів")
    private String password;

    @NotBlank(message = "Номер телефону є обовʼязковим")
    @Pattern(regexp = "(\\+380[0-9]{9}|0[0-9]{9})", message = "Некоректний формат номеру телефону")
    private String phoneNumber;

    @NotNull(message = "Дата народження є обовʼязковою")
    @Past(message = "Дата народження повинна бути в минулому")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthDate;

    @NotNull(message = "Місто є обовʼязковим")
    private String cityLife;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CV> cvList = new ArrayList<>();

    private String role = "ROLE_USER";

    // Email верифікація - використовуємо Boolean замість boolean для підтримки null
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expires")
    private LocalDateTime verificationTokenExpires;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Встановлюємо дефолтне значення якщо null
        if (emailVerified == null) {
            emailVerified = false;
        }
    }

    // Додаємо helper метод для boolean операцій
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }
}