package com.example.cvgenerator.controller.util;

import com.example.cvgenerator.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserHelper {


    // Метод для оновлення даних користувача (оновлюємо поля лише якщо вони не null і не isEmpty())

    public void updateUserData(User existingUser, User updatedUser) {
        System.out.println("UserHelper.updateUserData викликано!");

        if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().isEmpty()) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }

        if (updatedUser.getLastName() != null && !updatedUser.getLastName().isEmpty()) {
            existingUser.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().isEmpty()) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        if (updatedUser.getBirthDate() != null) {
            existingUser.setBirthDate(updatedUser.getBirthDate());
        }

        if (updatedUser.getCityLife() != null) {
            existingUser.setCityLife(updatedUser.getCityLife());
        }

        // (Для адміністратора) Окремо перевіряємо роль
        if (updatedUser.getRole() != null && !updatedUser.getRole().isEmpty()) {
            existingUser.setRole(updatedUser.getRole());
        }

    }

    // Метод для підготовки об'єкта користувача для форми
    public User prepareUserForm(User user) {
        User formUser = new User();
        formUser.setId(user.getId());
        formUser.setFirstName(user.getFirstName());
        formUser.setLastName(user.getLastName());
        formUser.setEmail(user.getEmail());
        formUser.setPhoneNumber(user.getPhoneNumber());
        formUser.setBirthDate(user.getBirthDate());
        formUser.setCityLife(user.getCityLife());
        formUser.setRole(user.getRole());
        formUser.setPassword("");

        return formUser;
    }
}