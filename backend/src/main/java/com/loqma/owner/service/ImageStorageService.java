package com.loqma.owner.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ImageStorageService {
    private final Cloudinary cloudinary;

    public ImageStorageService(
            @Value("${cloudinary.cloud-name}") String name,
            @Value("${cloudinary.api-key}") String key,
            @Value("${cloudinary.api-secret}") String secret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", name, "api_key", key, "api_secret", secret));
    }

    public String upload(MultipartFile file, String folder) throws IOException {
        String type = file.getContentType();
        if (type == null || !List.of("image/jpeg","image/png","image/webp").contains(type)) {
            throw new IllegalArgumentException("Only JPG, PNG or WebP allowed");
        }
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(), ObjectUtils.asMap("folder", folder));
        return (String) result.get("secure_url");
    }
}