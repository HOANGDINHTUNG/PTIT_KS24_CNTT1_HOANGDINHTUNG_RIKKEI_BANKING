package com.re.rikkei_bank_manager.kyc.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.re.rikkei_bank_manager.common.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryStorageService implements StorageService {
    private final Cloudinary cloudinary;

    public CloudinaryStorageService(@Value("${app.storage.cloudinary.cloud-name}") String cloudName,
                                    @Value("${app.storage.cloudinary.api-key}") String apiKey,
                                    @Value("${app.storage.cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
    }

    @Override
    public String uploadKycFile(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "rikkei-bank/kyc", "resource_type", "auto"));
            Object url = result.get("secure_url");
            if (url == null) throw new FileUploadException("Cloudinary did not return secure_url");
            return url.toString();
        } catch (IOException ex) {
            throw new FileUploadException("Cannot upload to Cloudinary: " + ex.getMessage());
        }
    }
}
