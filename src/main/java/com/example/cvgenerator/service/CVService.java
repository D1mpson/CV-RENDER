package com.example.cvgenerator.service;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.CVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CVService {

    private final CVRepository cvRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public CVService(CVRepository cvRepository, UserService userService, CloudinaryService cloudinaryService) {
        this.cvRepository = cvRepository;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    public CV createCV(CV cv, MultipartFile photoFile) throws IOException {
        // Встановлюємо поточного користувача
        User currentUser = userService.getCurrentUser();
        cv.setUser(currentUser);

        // Обробка завантаження фото через Cloudinary
        if (photoFile != null && !photoFile.isEmpty()) {
            String photoUrl = cloudinaryService.uploadFile(photoFile);
            cv.setPhotoPath(photoUrl);
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

        // Обробка нового фото через Cloudinary
        if (photoFile != null && !photoFile.isEmpty()) {
            // Видалення старого фото з Cloudinary, якщо воно існує
            if (existingCV.getPhotoPath() != null && !existingCV.getPhotoPath().isEmpty()) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(existingCV.getPhotoPath());
                cloudinaryService.deleteFile(publicId);
            }

            // Завантаження нового фото
            String newPhotoUrl = cloudinaryService.uploadFile(photoFile);
            existingCV.setPhotoPath(newPhotoUrl);
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

        // Видалення фото з Cloudinary, якщо воно існує
        if (cv.getPhotoPath() != null && !cv.getPhotoPath().isEmpty()) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(cv.getPhotoPath());
            cloudinaryService.deleteFile(publicId);
        }

        cvRepository.delete(cv);
    }
}