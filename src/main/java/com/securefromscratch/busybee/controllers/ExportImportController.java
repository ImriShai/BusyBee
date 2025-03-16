package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.auth.UsersStorage;
import com.securefromscratch.busybee.exceptions.*;
import com.securefromscratch.busybee.safety.Username;
import com.securefromscratch.busybee.storage.FileStorage;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TaskComment;
import com.securefromscratch.busybee.storage.TasksStorage;
import org.checkerframework.checker.units.qual.A;
import org.owasp.safetypes.exception.TypeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Type;
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
    @Autowired
    private UsersStorage m_users;

    @GetMapping("/extra/export")
    public ResponseEntity<byte[]> exportTasks(@AuthenticationPrincipal UserDetails userDetails) throws IOException, TypeValidationException {
        Username username = new Username(userDetails.getUsername());

        List<Task> allTasks = m_tasks.getTasks(username);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        // Serialize the tasks
        oos.writeObject(allTasks);
        byte[] serializedTasks = bos.toByteArray();

        // Create headers for the downloadable response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tasks.ser");

        LOGGER.info("Exported {} tasks successfully for user {}", allTasks.size(), username.get());

        return ResponseEntity.ok()
                .headers(headers)
                .body(serializedTasks);
    }

    @PostMapping("/extra/import")
    public ResponseEntity<String> importTasks(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) throws TypeValidationException, BadRequestException, UserDoesNotExistException {
        Username username = new Username(userDetails.getUsername());
        LOGGER.info("Import request received from user: {}", username.get());



        // DoS Protection: Check if the user has exceeded the import limit
        List<Instant> timestamps = userImportTimestamps.getOrDefault(username.get(), List.of());
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        List<Instant> recentTimestamps = new ArrayList<>(timestamps.stream()
                .filter(timestamp -> timestamp.isAfter(oneHourAgo))
                .toList());

        if (recentTimestamps.size() >= MAX_IMPORTS_PER_HOUR) {
            throw new TooManyRequestsException("Import limit exceeded.");
        }

        // DoS Protection: Limit file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException("File too large. Max allowed size: 10KB.");
        }

        // Invalid File Handling
        if (file.isEmpty() || !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".ser")) {
            throw new BadRequestException("Invalid file format. Only .ser files are allowed.");
        }

        try (ObjectInputStream ois = new ObjectInputStream(file.getInputStream())) {
            // Deserialize tasks
            Object obj = ois.readObject();
            if (!(obj instanceof List<?>)) {
                throw new BadRequestException("Invalid file content.");
            }

            List<?> taskList = (List<?>) obj;
            Set<String> taskNames = new HashSet<>();
            Set<UUID> taskIds = new HashSet<>();

            for (Object taskObj : taskList) {
                if (!(taskObj instanceof Task task)) {
                    throw new BadRequestException("Invalid task data format.");
                }

                // Check for duplicate tasks in the imported list
                if (!taskNames.add(task.name().get()) || !taskIds.add(task.taskid())) {
                    throw new ConflictException("Duplicate task detected.");
                }

                // Check for duplicate tasks for the user in the storage
                if (m_tasks.isTaskNameExists(task.name().get())&&(task.isResponsibleFor(username)) || m_tasks.isTaskIdExists(task.taskid())) {
                    throw new ConflictException("Duplicate task detected.");
                }

                // Check that all the tasks are assigned to the user
                if (!task.isResponsibleFor(username)) {
                    throw new AccessDeniedException("Task not assigned to user.");
                }

                // check that all the users actually exist
                if((m_users.findByUsername(task.createdBy().get()).isEmpty()) && !m_users.isUserExists(task.responsibilityOf()) ) {
                    throw new UserDoesNotExistException("User not found: " + task.createdBy().get());
                }
                // Check that all the comments are created by existing users
                if (!commentsUsersExist(task.comments())) {
                    throw new UserDoesNotExistException("User not found in comments");
                }

            }

            // If all tasks are valid, the user is related to each,  and no duplicates are found, add them to the storage
            for (Object taskObj : taskList) {
                Task task = (Task) taskObj;
                m_tasks.add(task);
            }

            // Initialize files for the tasks, if any, so related users could fetch them later
            List<Task> tasks = m_tasks.getTasks(username);
            FileStorage.initFiles(tasks);

            // Update the user's import timestamps
            recentTimestamps.add(Instant.now());
            userImportTimestamps.put(username.get(), recentTimestamps);

            LOGGER.info("Tasks imported successfully - {} tasks has been added to {}", taskList.size(), username.get());
            return ResponseEntity.ok("Tasks imported successfully.");
        }
        catch (Exception e) {
            if(e instanceof InvalidClassException ) {
                if(e.getMessage().contains("REJECTED")) {
                    throw new SecurityException("Your file Seems to be malicious. Please contact support.");
                }
                throw new BadRequestException("Invalid file content. Your file may be corrupted or too old.");
            }
            else if(e.getCause() instanceof TypeValidationException) {
                throw new BadRequestException("Error importing tasks. Your file may be corrupted: " + e.getCause().getMessage());
            }
            else if(e instanceof PayloadTooLargeException) {
                throw new PayloadTooLargeException("File too large. Max allowed size: 10KB.");
            }
            else if(e instanceof TooManyRequestsException) {
                throw new TooManyRequestsException("Import limit exceeded.");
            }
            else if(e instanceof AccessDeniedException) {
                throw new AccessDeniedException("Task not assigned to user.");
            }
            else if(e instanceof ConflictException) {
                throw new ConflictException("Duplicate task detected.");
            }
            else if(e instanceof BadRequestException) {
                throw new BadRequestException("Invalid file format. Only .ser files are allowed.");
            }
            else if(e instanceof IOException) {
                throw new BadRequestException("Error importing tasks. Your file may be corrupted: " + e.getMessage());
            }
            throw new BadRequestException("Error importing tasks. Your file may be corrupted: " + e.getMessage());
        }
    }



    private boolean commentsUsersExist(List<TaskComment> comments) {
        for (TaskComment comment : comments) {
            if(m_users.findByUsername(comment.createdBy().get()).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}