package com.example.cvgenerator.service;

import com.example.cvgenerator.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendVerificationEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Підтвердження реєстрації - CV Generator");

            String verificationUrl = baseUrl + "/verify-email?token=" + user.getVerificationToken();

            String emailBody = String.format(
                    "Вітаємо, %s!\n\n" +
                            "Дякуємо за реєстрацію в CV Generator.\n\n" +
                            "Для завершення реєстрації, будь ласка, перейдіть за посиланням:\n" +
                            "%s\n\n" +
                            "Посилання дійсне протягом 24 годин.\n\n" +
                            "Якщо ви не реєструвалися на нашому сайті, проігноруйте цей лист.\n\n" +
                            "З повагою,\n" +
                            "Команда CV Generator",
                    user.getFirstName(),
                    verificationUrl
            );

            message.setText(emailBody);
            mailSender.send(message);

            System.out.println("✅ Verification email sent to: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + user.getEmail());
            System.err.println("Error: " + e.getMessage());
        }
    }
}