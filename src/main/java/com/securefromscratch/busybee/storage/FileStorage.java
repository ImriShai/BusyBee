package com.securefromscratch.busybee.storage;

import com.securefromscratch.busybee.exceptions.BadRequestException;
import com.securefromscratch.busybee.exceptions.NotEnoughSpaceException;
import com.securefromscratch.busybee.exceptions.TooManyRequestsException;
import com.securefromscratch.busybee.safety.Username;
import org.apache.catalina.User;
import org.apache.tika.Tika;
import org.owasp.safetypes.exception.TypeValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileStorage {

    public enum FileType {
        IMAGE,
        PDF,
        WORD,
        RTF,
        OTHER
    }

    private static final String imgType = "image/";
    private static final String pdfType = "application/pdf";
    private static final String wordType = "application/msword";
    private static final String wordType2 = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final int UUID_LENGTH = UUID.randomUUID().toString().length();
    private final Path m_storagebox;
    private final Map<String, List<LocalDateTime>> userUploads = new ConcurrentHashMap<>();
    private static final int MAX_UPLOADS_PER_HOUR = 5;
    private static final long maxFileSize = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILE_NAME_LENGTH = 255;
    private static final Map<Path, String> files = new ConcurrentHashMap<>(); // A map of stored files, Path to original filename
    private static final Map<Path, List<Username>> file_owners = new ConcurrentHashMap<>(); // A map of stored files, Path to owner

    public FileStorage(Path storageDirectory) throws IOException {
        m_storagebox = storageDirectory;
        if (!Files.exists(m_storagebox)) {
            Files.createDirectories(m_storagebox);
        }
    }


    // This method goes through the tasks and initializes the files
    public static void initFiles(List<Task> tasks) {
        for (Task task : tasks) {
          for (TaskComment comment : task.comments()) {
            if (comment.hasFile()) {
              String originalFilename = comment.originalFilename().orElse("");
              String filename = comment.imageOrAttachment().orElse("");
              Path path = Path.of("uploads", filename);
              if (Files.exists(path)) {
                files.putIfAbsent(path, originalFilename);
                  List<Username> owners = new ArrayList<>(List.of(task.responsibilityOf()));
                  owners.add(task.createdBy());
                file_owners.putIfAbsent(path, owners);
              }
            }
          }
        }
    }



    public StoredFile store(MultipartFile file, String userId, List<Username> owners) throws IOException, TypeValidationException, SecurityException, TooManyRequestsException {
        validateFile(file);
        if (exceedsUploadLimit(userId)) {
            throw new TooManyRequestsException("Upload limit exceeded");
        }
        if (!hasEnoughDiskSpace()) {
            throw new NotEnoughSpaceException("Not enough disk space");
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + extractExtension(originalFilename);
        Path filepath = m_storagebox.resolve(storedFilename);
        file.transferTo(filepath);

        trackUpload(userId);
        files.putIfAbsent(filepath, originalFilename);
        file_owners.putIfAbsent(filepath,owners);
        return new StoredFile(filepath, originalFilename, storedFilename);
    }


    private void validateFile(MultipartFile file) throws TypeValidationException, SecurityException, BadRequestException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.length() > MAX_FILE_NAME_LENGTH) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // Prevent path traversal attacks
        if (!filename.matches("^[a-zA-Z0-9_()\\-.+!#@$%^\\[\\]& \\p{IsHebrew}]+$") || filename.contains("..")) {
            throw new SecurityException("Invalid file name - path traversal attempt detected");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds the limit (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new TypeValidationException("Invalid file type");
        }
    }

    private boolean isAllowedFileType(String contentType) {
        return contentType.startsWith(imgType) || contentType.equals(pdfType) || contentType.equals(wordType) || contentType.equals(wordType2);
    }

    private boolean hasEnoughDiskSpace() {
        try {
            FileStore store = Files.getFileStore(m_storagebox);
            long freeSpace = store.getUsableSpace();
            long minFreeSpace = 100 * 1024 * 1024; // 100MB
            return freeSpace >= minFreeSpace;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to check disk space", e);
        }
    }

    private boolean exceedsUploadLimit(String userId) {
        List<LocalDateTime> uploads = userUploads.computeIfAbsent(userId, k -> new ArrayList<>());
        LocalDateTime now = LocalDateTime.now();
        uploads.removeIf(uploadTime -> uploadTime.isBefore(now.minusHours(1)));
        if (uploads.size() >= MAX_UPLOADS_PER_HOUR) {
            return true;
        }
        uploads.add(now);
        return false;
    }

    private void trackUpload(String userId) {
        List<LocalDateTime> uploads = userUploads.computeIfAbsent(userId, k -> new ArrayList<>());
        uploads.add(LocalDateTime.now());
    }

    private static String extractExtension(String filename) {
        String[] parts = filename.split("\\.");
        return parts.length == 1 ? "" : ("." + parts[parts.length - 1]);
    }

    public static FileType identifyType(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String magicByteType = tika.detect(file.getInputStream());
        String contentType = file.getContentType();
        return getFileType(magicByteType, contentType);
    }

    public static FileType identifyType(Path path) throws IOException {
        try {
            Tika tika = new Tika();
            String magicByteType = tika.detect(path);
            String contentType = Files.probeContentType(path);
            return getFileType(magicByteType, contentType);
        } catch (IOException e) {
            throw new IOException("Failed to identify file type", e);
        }
    }

    @NotNull
    private static FileType getFileType(String magicByteType, String contentType) {
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
        if (contentType.contains("word") && magicByteType.contains("word")) {
            return FileType.WORD;
        }
        if ((contentType.contains("rtf")||contentType.contains("word")) && magicByteType.contains("rtf")) { //
            return FileType.RTF;
        }
        return FileType.OTHER;
    }

    public record StoredFile(Path path, String originalFilename, String storedFilename) {
    }

    public Path retrieve(String filename, Username username) throws NoSuchFileException, SecurityException, AccessDeniedException {
        Path filepath = m_storagebox.resolve(filename);

        // Check if the user is the owner of the file
        if (!isOwner(filepath, username)) {
            throw new org.springframework.security.access.AccessDeniedException("User is not the owner of the file");
        }

        // Prevent path traversal attacks
        if (!filepath.startsWith(m_storagebox) || filename.contains("..")) {
            throw new SecurityException("Path traversal attempt detected: " + filename);
        }
        if (!Files.exists(filepath)) {
            throw new NoSuchFileException("File not found");
        }
        return filepath;
    }

    public static String retrieveOriginalFilename(Path path) throws NoSuchFileException {

        if (!files.containsKey(path)) {
            throw new NoSuchFileException("File not found");
        }
        return files.get(path);
    }

    public static boolean isOwner(Path path, Username username){
        if (path == null || username == null) {
            return false;
        }
        List<Username> owners = file_owners.get(path);
            if (owners == null) {
                return false;
            }
            return owners.contains(username);
    }
}