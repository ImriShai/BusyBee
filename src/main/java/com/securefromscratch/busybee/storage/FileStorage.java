package com.securefromscratch.busybee.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.apache.tika.Tika;

public class FileStorage {
    public enum FileType {
        IMAGE,
        PDF,
        OTHER
    }

    private static final int UUID_LENGTH = UUID.randomUUID().toString().length();

    private final Path m_storagebox;

    public FileStorage(Path storageDirectory) throws IOException {
        m_storagebox = storageDirectory;
        Path rootPath = m_storagebox;
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
    }

    public Path store(MultipartFile file) throws IOException {
        FileType type = identifyType(file);
        if (type == FileType.OTHER) {
            throw new IllegalArgumentException("Unsupported file type");
        }
        if(file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        if(file.getSize() > 10_000_000) {
            throw new IllegalArgumentException("File too large");
        }
        // write code to store a file and returns its path
        String filename = UUID.randomUUID().toString() + extractExtension(file.getOriginalFilename());
        Path filepath = m_storagebox.resolve(filename);
        file.transferTo(filepath);
        return filepath;
    }

    public byte[] getBytes(String filename) throws IOException {
        Path filepath = m_storagebox.resolve(filename);
        byte[] serialized = Files.readAllBytes(filepath);
        return serialized;
    }

    public static FileType identifyType(MultipartFile file) throws IOException {

        Tika tika = new Tika();
        String magicByteType;
        try {
            magicByteType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new IOException("Failed to read file", e);

        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return FileType.OTHER;
        }
        contentType = contentType.toLowerCase();
        if (contentType.startsWith("image/") && magicByteType.startsWith("image")) {
            return FileType.IMAGE;
        }
        if (contentType.contains("pdf") && magicByteType.contains("pdf")) {
            return FileType.PDF;
        }
        return FileType.OTHER;
    }

    private static String extractExtension(String filename) {
        String[] parts = filename.split("\\.");
        return parts.length == 1 ? "" : ("." + parts[parts.length - 1]);
    }
}
