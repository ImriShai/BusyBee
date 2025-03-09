package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.exceptions.*;
import com.securefromscratch.busybee.storage.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080", "https://localhost:8443", "https://127.0.0.1:8443"})
public class StorageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageController.class);

    @Autowired
    private FileStorage m_files;
    private final ConcurrentMap<String, List<Instant>> userFilesDownloadsTimestamps = new ConcurrentHashMap<>();
    private static final int MAX_FILE_DOWNLOADS_PER_HOUR = 0;

    public StorageController() throws IOException {
        // Use an absolute path or ensure the relative path is correct
        Path uploadPath = Path.of("uploads").toAbsolutePath();
        m_files = new FileStorage(uploadPath);
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> getImage(@RequestParam String img) throws IOException {
        return serveFile(img, FileStorage.FileType.IMAGE, false);
    }

    @GetMapping("/attachment")
    public ResponseEntity<byte[]> getAttachment(@RequestParam String file) throws IOException {
        // Check if already exceeded the limit
        List<Instant> timestamps = userFilesDownloadsTimestamps.getOrDefault("Yariv", List.of());
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        List<Instant> recentTimestamps = new ArrayList<>(timestamps.stream()
                .filter(timestamp -> timestamp.isAfter(oneHourAgo))
                .toList());
        if (recentTimestamps.size() >= MAX_FILE_DOWNLOADS_PER_HOUR) {
            LOGGER.warn("User has exceeded the download limit for file: {}", file);
            throw new TooManyRequestsException("Download limit exceeded.");
        }

        ResponseEntity<byte[]> response = serveFile(file, FileStorage.FileType.OTHER, true);
        if (response.getStatusCode() == HttpStatus.OK) {
            // Update the user's import timestamps
            recentTimestamps.add(Instant.now());
            userFilesDownloadsTimestamps.put("Yariv", recentTimestamps);
        }
        return response;
    }

    private ResponseEntity<byte[]> serveFile(String filename, FileStorage.FileType expectedType, boolean forceDownload) throws IOException {
        if (filename == null || filename.isEmpty()) {
            LOGGER.warn("Invalid filename requested");
            throw new BadRequestException("Invalid filename requested.");
        }
        if (filename.startsWith("uploads/")) { // Remove the uploads/ prefix if it exists
            filename = filename.substring(8);
        }
        Path filePath = m_files.retrieve(filename);
        String originalFilename = FileStorage.retrieveOriginalFilename(filePath);

        // Validate file type using FileStorage's identifyType method
        FileStorage.FileType fileType = FileStorage.identifyType(filePath);
        if (((fileType != expectedType) && (fileType == FileStorage.FileType.IMAGE)) || ((expectedType == FileStorage.FileType.OTHER) && (fileType == FileStorage.FileType.OTHER))) {
            LOGGER.warn("Invalid file type requested: {}", filename);
            throw new SecurityException("Invalid file type requested.");
        }

        byte[] fileBytes = Files.readAllBytes(filePath);
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null || mimeType.isEmpty()) {
            LOGGER.warn("Invalid MIME type for file: {}", filename);
            throw new SecurityException("Invalid MIME type for file.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        String encodedFilename = UriUtils.encode(originalFilename, StandardCharsets.UTF_8);

        if (forceDownload) {
            headers.setContentDisposition(ContentDisposition.parse("attachment; filename=\"" + encodedFilename + "\""));
        } else {
            headers.setContentDisposition(ContentDisposition.parse("inline; filename=\"" + encodedFilename + "\""));
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }
}