package com.example.cvgenerator.service;

import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JdbcTemplate jdbcTemplate, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
    }

    public Optional<User> findByEmailWithJdbc(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setBirthDate(rs.getObject("birth_date", LocalDate.class));
                u.setCityLife(rs.getString("city_life"));
                u.setRole(rs.getString("role"));

                // Безпечне отримання boolean значення
                Boolean verified = rs.getObject("email_verified", Boolean.class);
                u.setEmailVerified(verified != null ? verified : false);

                return u;
            });
            return Optional.ofNullable(user);
        } catch (Exception e) {
            System.out.println("Користувача з email " + email + " не знайдено: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void saveUser(User user) {
        // Якщо це новий користувач (без ID), перевіряємо чи існує такий email
        if (user.getId() == null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Користувач з таким email вже існує");
        }

        // Для нових користувачів створюємо verification token
        if (user.getId() == null) {
            user.setVerificationToken(UUID.randomUUID().toString());
            user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24));
            user.setEmailVerified(false);
        }

        // Шифрування пароля, якщо він ще не зашифрований і не пустий
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User savedUser = userRepository.save(user);

        // Відправляємо email тільки для нових користувачів
        if (savedUser.getVerificationToken() != null && !savedUser.isEmailVerified()) {
            emailService.sendVerificationEmail(savedUser);
        }
    }

    @Transactional
    public boolean verifyEmail(String token) {
        System.out.println("🔍 Attempting to verify email with token: " + token);

        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("✅ User found: " + user.getEmail());
            System.out.println("📅 Token expires at: " + user.getVerificationTokenExpires());
            System.out.println("📅 Current time: " + LocalDateTime.now());

            // Перевіряємо чи не протерміновано токен
            if (user.getVerificationTokenExpires() != null &&
                    user.getVerificationTokenExpires().isAfter(LocalDateTime.now())) {

                // ВАЖЛИВО: Отримуємо користувача знову, щоб він був в managed стані
                User managedUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Оновлюємо поля
                managedUser.setEmailVerified(true);
                managedUser.setVerificationToken(null);
                managedUser.setVerificationTokenExpires(null);

                // Явно викликаємо flush для збереження змін
                User savedUser = userRepository.saveAndFlush(managedUser);

                System.out.println("✅ Email verified successfully for: " + savedUser.getEmail());
                System.out.println("✅ Email verified status after save: " + savedUser.isEmailVerified());
                System.out.println("✅ Verification token after save: " + savedUser.getVerificationToken());

                return true;
            } else {
                System.out.println("❌ Token expired!");
            }
        } else {
            System.out.println("❌ No user found with token: " + token);
        }

        return false;
    }

    public void updatePassword(User user, String newPassword) {
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Метод для отримання поточного авторизованого користувача
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача з email " + email + " не знайдено"));

        // Перевіряємо чи верифіковано email
        if (!user.isEmailVerified()) {
            throw new UsernameNotFoundException("Email не підтверджено. Перевірте свою пошту.");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }

    // Отримання всіх користувачів (для адміністратора)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Видалення користувача (для адміністратора)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> findByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }
}