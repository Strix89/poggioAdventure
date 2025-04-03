package com.mycompany.gamestatapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Strix89
 */

@Configuration
@ConfigurationProperties(prefix = "file") // Legge le properties che iniziano con "file."
public class FileStorageConfig {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}