package com.re.rikkei_bank_manager.kyc.service;

import com.re.rikkei_bank_manager.common.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {
    @Value("${app.storage.local-upload-dir:uploads/kyc}")
    private String localUploadDir;

    @Override
    public String uploadKycFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(localUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "kyc-file" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String fileName = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/kyc/" + fileName;
        } catch (IOException ex) {
            throw new FileUploadException("Cannot upload file: " + ex.getMessage());
        }
    }
}
