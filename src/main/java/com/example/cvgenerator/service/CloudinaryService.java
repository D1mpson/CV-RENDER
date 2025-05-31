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
            System.out.println("⚠️ No file provided for upload");
            return null;
        }

        System.out.println("📤 Uploading file to Cloudinary: " + file.getOriginalFilename());
        System.out.println("📊 File size: " + file.getSize() + " bytes");

        try {
            // Простий upload без трансформацій
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "cv-photos",
                            "resource_type", "image"
                    )
            );

            String photoUrl = uploadResult.get("secure_url").toString();
            System.out.println("✅ Photo uploaded successfully to: " + photoUrl);
            return photoUrl;

        } catch (IOException e) {
            System.err.println("❌ Error uploading file to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteFile(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return;
        }

        try {
            String publicId = extractPublicIdFromUrl(photoUrl);
            if (publicId != null) {
                System.out.println("🗑️ Deleting file from Cloudinary: " + publicId);
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("✅ File deleted successfully");
            }
        } catch (IOException e) {
            System.err.println("❌ Error deleting file from Cloudinary: " + e.getMessage());
        }
    }

    // Покращений метод для отримання public_id з URL
    public String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // URL формат: https://res.cloudinary.com/cloud-name/image/upload/v123456/cv-photos/filename.jpg
            String[] parts = url.split("/");
            if (parts.length >= 3) {
                // Знаходимо індекс "upload"
                int uploadIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if ("upload".equals(parts[i])) {
                        uploadIndex = i;
                        break;
                    }
                }

                if (uploadIndex != -1 && uploadIndex + 2 < parts.length) {
                    // Пропускаємо версію (v123456) і беремо папку та файл
                    StringBuilder publicId = new StringBuilder();
                    for (int i = uploadIndex + 2; i < parts.length; i++) {
                        if (i > uploadIndex + 2) {
                            publicId.append("/");
                        }
                        publicId.append(parts[i]);
                    }

                    // Видаляємо розширення файлу
                    String result = publicId.toString();
                    int lastDotIndex = result.lastIndexOf('.');
                    if (lastDotIndex > 0) {
                        result = result.substring(0, lastDotIndex);
                    }

                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting public_id from URL: " + e.getMessage());
        }

        return null;
    }
}