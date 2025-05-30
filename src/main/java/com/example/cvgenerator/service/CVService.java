package com.example.cvgenerator.service;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.CVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CVService {

    private final CVRepository cvRepository;
    private final UserService userService;
    private final String UPLOAD_DIR = "uploads/photos/";

    @Autowired
    public CVService(CVRepository cvRepository, UserService userService) {
        this.cvRepository = cvRepository;
        this.userService = userService;

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Не вдалося створити директорію для завантажень: " + e.getMessage());
        }
    }

    public CV createCV(CV cv, MultipartFile photoFile) throws IOException {
        // Встановлюємо поточного користувача
        User currentUser = userService.getCurrentUser();
        cv.setUser(currentUser);

        // Обробка завантаження фото
        if (photoFile != null && !photoFile.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photoFile.getBytes());
            cv.setPhotoPath(fileName);
        }

        // Ініціалізуємо колекції, якщо вони null
        if (cv.getPortfolioLinks() == null) {
            cv.setPortfolioLinks(List.of());
        }

        if (cv.getKnownLanguages() == null) {
            cv.setKnownLanguages(List.of());
        }

        return cvRepository.save(cv);
    }

    public List<CV> getAllCVsByUser(User user) {
        return cvRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<CV> getAllCVsForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return getAllCVsByUser(currentUser);
    }

    public Optional<CV> getCVById(Long id) {
        return cvRepository.findById(id);
    }

    public void updateCV(CV cv, MultipartFile photoFile) throws IOException {
        CV existingCV = cvRepository.findById(cv.getId())
                .orElseThrow(() -> new RuntimeException("CV з ID " + cv.getId() + " не знайдено"));

        // Перевірка чи CV належить поточному користувачу
        User currentUser = userService.getCurrentUser();
        if (!existingCV.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("У вас немає прав для редагування цього CV");
        }

        existingCV.setName(cv.getName());
        existingCV.setAboutMe(cv.getAboutMe());
        existingCV.setHobbies(cv.getHobbies());

        existingCV.setSoftSkills(cv.getSoftSkills());
        existingCV.setHardSkills(cv.getHardSkills());
        existingCV.setOtherLanguages(cv.getOtherLanguages());
        existingCV.setEducation(cv.getEducation());
        existingCV.setCourses(cv.getCourses());
        existingCV.setWorkExperience(cv.getWorkExperience());

        if (cv.getPortfolioLinks() != null) {
            existingCV.setPortfolioLinks(cv.getPortfolioLinks());
        }

        if (cv.getKnownLanguages() != null) {
            existingCV.setKnownLanguages(cv.getKnownLanguages());
        }

        existingCV.setFont(cv.getFont());
        existingCV.setLanguage(cv.getLanguage());

        // Якщо шаблон змінився, оновлюємо його
        if (cv.getTemplate() != null) {
            existingCV.setTemplate(cv.getTemplate());
        }

        // Обробка нового фото
        if (photoFile != null && !photoFile.isEmpty()) {
            // Видалення старого фото, якщо воно існує
            if (existingCV.getPhotoPath() != null && !existingCV.getPhotoPath().isEmpty()) {
                Path oldPath = Paths.get(UPLOAD_DIR + existingCV.getPhotoPath());
                Files.deleteIfExists(oldPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photoFile.getBytes());
            existingCV.setPhotoPath(fileName);
        }

        cvRepository.save(existingCV);
    }

    public void deleteCV(Long id) {
        CV cv = cvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV з ID " + id + " не знайдено"));

        User currentUser = userService.getCurrentUser();
        if (!cv.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("У вас немає прав для видалення цього CV");
        }

        if (cv.getPhotoPath() != null && !cv.getPhotoPath().isEmpty()) {
            try {
                Path photoPath = Paths.get(UPLOAD_DIR + cv.getPhotoPath());
                Files.deleteIfExists(photoPath);
            } catch (IOException e) {
                System.err.println("Не вдалося видалити фото: " + e.getMessage());
            }
        }

        cvRepository.delete(cv);
    }
}