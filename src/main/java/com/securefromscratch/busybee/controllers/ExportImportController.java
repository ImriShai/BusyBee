package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.exceptions.*;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TasksStorage;
import org.owasp.safetypes.exception.TypeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080", "https://localhost:8443", "https://127.0.0.1:8443"})
public class ExportImportController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportController.class);
    private static final int MAX_IMPORTS_PER_HOUR = 5;
    private static final int MAX_FILE_SIZE = 1024 * 10; // 10KB limit
    private final ConcurrentMap<String, List<Instant>> userImportTimestamps = new ConcurrentHashMap<>();

    @Autowired
    private TasksStorage m_tasks;

    @GetMapping("/extra/export")
    public ResponseEntity<byte[]> exportTasks() throws IOException {
        List<Task> allTasks = m_tasks.getAll();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        // Serialize the tasks
        oos.writeObject(allTasks);
        byte[] serializedTasks = bos.toByteArray();

        // Create headers for the downloadable response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tasks.ser");

        LOGGER.info("Exported tasks successfully");

        return ResponseEntity.ok()
                .headers(headers)
                .body(serializedTasks);
    }

    @PostMapping("/extra/import")
    public ResponseEntity<String> importTasks(@RequestParam("file") MultipartFile file) throws IOException, ClassNotFoundException {
        LOGGER.info("Import request received");

        // DoS Protection: Check if the user has exceeded the import limit
        List<Instant> timestamps = userImportTimestamps.getOrDefault("Yariv", List.of());
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        List<Instant> recentTimestamps = new ArrayList<>(timestamps.stream()
                .filter(timestamp -> timestamp.isAfter(oneHourAgo))
                .toList());

        if (recentTimestamps.size() >= MAX_IMPORTS_PER_HOUR) {
            LOGGER.warn("Import limit exceeded for user Yariv");
            throw new TooManyRequestsException("Import limit exceeded.");
        }

        // DoS Protection: Limit file size
        if (file.getSize() > MAX_FILE_SIZE) {
            LOGGER.warn("File too large: {} bytes", file.getSize());
            throw new PayloadTooLargeException("File too large. Max allowed size: 10KB.");
        }

        // Invalid File Handling
        if (file.isEmpty() || !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".ser")) {
            LOGGER.warn("Invalid file format: {}", file.getOriginalFilename());
            throw new BadRequestException("Invalid file format. Only .ser files are allowed.");
        }

        try (ObjectInputStream ois = new ObjectInputStream(file.getInputStream())) {
            // Deserialize tasks
            Object obj = ois.readObject();
            if (!(obj instanceof List<?>)) {
                LOGGER.warn("Invalid file content");
                throw new BadRequestException("Invalid file content.");
            }

            List<?> taskList = (List<?>) obj;
            Set<String> taskNames = new HashSet<>();
            Set<UUID> taskIds = new HashSet<>();

            for (Object taskObj : taskList) {
                if (!(taskObj instanceof Task task)) {
                    LOGGER.warn("Invalid task data format");
                    throw new BadRequestException("Invalid task data format.");
                }

                // Validate DueDate + DueTime
                if (!task.isDueDateTimeValid()) {
                    LOGGER.info("Invalid due date or due time for task: {}", task.name().get());
                    throw new BadRequestException("Invalid due date or due time.");
                }

                // Check for duplicate tasks in the imported list
                if (!taskNames.add(task.name().get()) || !taskIds.add(task.taskid())) {
                    LOGGER.info("Duplicate task detected in the imported list: {}", task.name().get());
                    throw new ConflictException("Duplicate task detected.");
                }

                // Check for duplicate tasks
                if (m_tasks.isTaskNameExists(task.name().get()) || m_tasks.isTaskIdExists(task.taskid())) {
                    LOGGER.info("Duplicate task detected in storage: {}", task.name().get());
                    throw new ConflictException("Duplicate task detected.");
                }
            }

            // If all tasks are valid and no duplicates are found, add them to the storage
            for (Object taskObj : taskList) {
                Task task = (Task) taskObj;
                m_tasks.add(task);
            }

            // Update the user's import timestamps
            recentTimestamps.add(Instant.now());
            userImportTimestamps.put("Yariv", recentTimestamps);

            LOGGER.info("Tasks imported successfully");
            return ResponseEntity.ok("Tasks imported successfully.");
        }
    }
}