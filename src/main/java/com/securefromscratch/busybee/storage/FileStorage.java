package com.securefromscratch.busybee.storage;

import com.securefromscratch.busybee.exceptions.BadRequestException;
import com.securefromscratch.busybee.exceptions.NotEnoughSpaceException;
import com.securefromscratch.busybee.exceptions.TooManyRequestsException;
import com.securefromscratch.busybee.safety.Username;
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
import java.util.logging.Logger;

public class FileStorage {

    private static final Logger LOGGER = Logger.getLogger(FileStorage.class.getName());

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
    private static final long MIN_FREE_SPACE = 1024 * 1024 * 1024; // 1GB

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
        LOGGER.info("FileStorage initialized with directory: " + m_storagebox);
    }

    public static void initFiles(List<Task> tasks) throws SecurityException {
        for (Task task : tasks) {
            for (TaskComment comment : task.comments()) {
                if (comment.hasFile()) {
                    String originalFilename = comment.originalFilename().orElse("");
                    String filename = comment.imageOrAttachment().orElse("");
                    if (!filename.matches("^[a-zA-Z0-9_()\\-.,+!#@$%^\\[\\]& \\p{IsHebrew}]+$") || filename.contains("..")) {
                        throw new SecurityException("Invalid file name - path traversal attempt detected");
                    }
                    Path path = Path.of("uploads", filename);
                    if (Files.exists(path)) {
                        files.putIfAbsent(path, originalFilename);
                        List<Username> owners = new ArrayList<>(List.of(task.responsibilityOf()));
                        owners.add(task.createdBy());
                        file_owners.putIfAbsent(path, owners);
                        LOGGER.info("Initialized file: " + path + " with original filename: " + originalFilename);
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
        file_owners.putIfAbsent(filepath, owners);
        LOGGER.info("Stored file: " + filepath + " with original filename: " + originalFilename);
        return new StoredFile(filepath, originalFilename, storedFilename);
    }

    private void validateFile(MultipartFile file) throws TypeValidationException, SecurityException, BadRequestException, IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.length() > MAX_FILE_NAME_LENGTH) {
            throw new IllegalArgumentException("Invalid file name");
        }

        if (!filename.matches("^[a-zA-Z0-9_()\\-.+!#@$%^\\[\\]& \\p{IsHebrew}]+$") || filename.contains("..")) {
            throw new SecurityException("Invalid file name - path traversal attempt detected");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds the limit (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType) || identifyType(file) == FileType.OTHER) {
            throw new TypeValidationException("Invalid file type");
        }
        LOGGER.info("Validated file: " + filename + " with content type: " + contentType);
    }

    private boolean isAllowedFileType(String contentType) {
        return contentType.startsWith(imgType) || contentType.equals(pdfType) || contentType.equals(wordType) || contentType.equals(wordType2);
    }

    public static FileType identifyType(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String magicByteType = tika.detect(file.getInputStream());
        String contentType = file.getContentType();
        FileType fileType = getFileType(magicByteType, contentType);
        LOGGER.info("Identified file type: " + fileType + " for content type: " + contentType + " and magic byte type: " + magicByteType);
        return fileType;
    }

    public static FileType identifyType(Path path) throws IOException {
        try {
            Tika tika = new Tika();
            String magicByteType = tika.detect(path);
            String contentType = Files.probeContentType(path);
            FileType fileType = getFileType(magicByteType, contentType);
            LOGGER.info("Identified file type: " + fileType + " for path: " + path);
            return fileType;
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
            if (contentType.contains("svg") || magicByteType.contains("svg")) {
                return FileType.OTHER;
            }
            return FileType.IMAGE;
        }
        if (contentType.contains("pdf") && magicByteType.contains("pdf")) {
            return FileType.PDF;
        }
        if (contentType.contains("word") && (magicByteType.contains("word") || magicByteType.equals("application/x-tika-ooxml"))) {
            return FileType.WORD;
        }
        if ((contentType.contains("rtf") || contentType.contains("word")) && magicByteType.contains("rtf")) {
            return FileType.RTF;
        }
        return FileType.OTHER;
    }

    private boolean hasEnoughDiskSpace() {
        try {
            FileStore store = Files.getFileStore(m_storagebox);
            long freeSpace = store.getUsableSpace();
            boolean hasSpace = freeSpace >= MIN_FREE_SPACE;
            LOGGER.info("Disk space check: " + freeSpace + " bytes available, has enough space: " + hasSpace);
            return hasSpace;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to check disk space", e);
        }
    }

    private boolean exceedsUploadLimit(String userId) {
        List<LocalDateTime> uploads = userUploads.computeIfAbsent(userId, k -> new ArrayList<>());
        LocalDateTime now = LocalDateTime.now();
        uploads.removeIf(uploadTime -> uploadTime.isBefore(now.minusHours(1)));
        boolean exceedsLimit = uploads.size() > MAX_UPLOADS_PER_HOUR;
        LOGGER.info("Upload limit check for user: " + userId + ", exceeds limit: " + exceedsLimit);
        return exceedsLimit;
    }

    private void trackUpload(String userId) {
        List<LocalDateTime> uploads = userUploads.computeIfAbsent(userId, k -> new ArrayList<>());
        uploads.add(LocalDateTime.now());
        LOGGER.info("Tracked upload for user: " + userId + ", total uploads in the last hour: " + uploads.size());
    }

    private static String extractExtension(String filename) {
        String[] parts = filename.split("\\.");
        return parts.length == 1 ? "" : ("." + parts[parts.length - 1]);
    }

    public record StoredFile(Path path, String originalFilename, String storedFilename) {
    }

    public Path retrieve(String filename, Username username) throws NoSuchFileException, SecurityException, AccessDeniedException {
        if (!filename.matches("^[a-zA-Z0-9_()\\-.,+!#@$%^\\[\\]& \\p{IsHebrew}]+$")) {
            throw new SecurityException("Invalid file name characters detected: " + filename);
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Path traversal attempt detected: " + filename);
        }

        Path filepath = m_storagebox.resolve(filename).normalize();

        if (!isOwner(filepath, username)) {
            throw new AccessDeniedException("User is not the owner of the file");
        }

        if (!filepath.startsWith(m_storagebox) || filename.contains("..")) {
            throw new SecurityException("Path traversal attempt detected: " + filename);
        }
        if (!Files.exists(filepath)) {
            throw new NoSuchFileException("File not found");
        }
        LOGGER.info("Retrieved file: " + filepath + " for user: " + username.get());
        return filepath;
    }

    public static String retrieveOriginalFilename(Path path) throws NoSuchFileException {
        if (!files.containsKey(path)) {
            throw new NoSuchFileException("File not found");
        }
        String originalFilename = files.get(path);
        LOGGER.info("Retrieved original filename: " + originalFilename + " for path: " + path);
        return originalFilename;
    }

    public static boolean isOwner(Path path, Username username) {
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