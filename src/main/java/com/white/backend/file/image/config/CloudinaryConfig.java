package com.white.backend.file.image.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloud.cloudinary.cloud-name}")
    private String cloudName;
    @Value("${cloud.cloudinary.api-key}")
    private String apiKey;
    @Value("${cloud.cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    Cloudinary cloudinary() {

        Map<String, String> config = Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        );

        return new Cloudinary(config);

    }

}
