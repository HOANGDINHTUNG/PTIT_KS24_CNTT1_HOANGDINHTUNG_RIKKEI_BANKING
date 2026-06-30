package com.re.rikkei_bank_manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.storage.local-upload-dir:uploads/kyc}")
    private String localUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(localUploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/kyc/**")
                .addResourceLocations(uploadPath.toUri().toString() + "/");
    }
}
