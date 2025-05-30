package com.example.cvgenerator.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "cv-photos",
                        "resource_type", "image",
                        "transformation", ObjectUtils.asMap(
                                "width", 400,
                                "height", 400,
                                "crop", "limit",
                                "quality", "auto"
                        )
                )
        );

        return uploadResult.get("secure_url").toString();
    }

    public void deleteFile(String publicId) {
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                System.err.println("Помилка видалення файлу з Cloudinary: " + e.getMessage());
            }
        }
    }

    // Допоміжний метод для отримання public_id з URL
    public String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // URL має вигляд: https://res.cloudinary.com/cloud-name/image/upload/v123456/cv-photos/filename.jpg
        String[] parts = url.split("/");
        if (parts.length >= 2) {
            String lastPart = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            // Видаляємо розширення файлу
            String filename = lastPart.substring(0, lastPart.lastIndexOf('.'));
            return folder + "/" + filename;
        }
        return null;
    }
}