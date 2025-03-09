package com.securefromscratch.busybee.config;

import com.securefromscratch.busybee.storage.FileStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Bean
    public FileStorage fileStorage() throws IOException {
        Path storageDirectory = Paths.get("uploads");
        return new FileStorage(storageDirectory);
    }
}