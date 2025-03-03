package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TasksStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Map;

@RestController
public class ExportImportController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportController.class);

    @Autowired
    private TasksStorage m_tasks;

    @GetMapping("/extra/export")
    public ResponseEntity<byte[]> exportTasks() {
        List<Task> allTasks = m_tasks.getAll();

        try {
            // Create headers for the downloadable response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "tasks.ser");

            byte[] serializedTasks ;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(allTasks);
            serializedTasks = bos.toByteArray();
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(serializedTasks);
        } catch (IOException e) {
            LOGGER.error("Failed to serialize tasks for export", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/extra/import")
    public ResponseEntity<String> importTasks(@RequestParam("file") MultipartFile file) {
        // - deserialize the file
        // - add the tasks to the storage m_tasks
        try {
            ObjectInputStream ois = new ObjectInputStream(file.getInputStream());
            String contentType = file.getContentType();
            List<Task> tasks = (List<Task>) ois.readObject();
            for (Task task : tasks) {
                m_tasks.add(task);
            }
            return ResponseEntity.ok("Tasks imported successfully");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to import tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to import tasks");
        }
    }
}