package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.exceptions.BadRequestException;
import com.securefromscratch.busybee.exceptions.NotEnoughSpaceException;
import com.securefromscratch.busybee.safety.CommentText;
import com.securefromscratch.busybee.safety.Username;
import com.securefromscratch.busybee.storage.FileStorage;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.exceptions.TaskNotFoundException;
import com.securefromscratch.busybee.storage.TasksStorage;
import jakarta.validation.constraints.NotNull;
import org.owasp.safetypes.exception.TypeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080", "https://localhost:8443", "https://127.0.0.1:8443"})
public class CommentsUploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsUploadController.class);

    @Autowired
    private TasksStorage m_tasks;

    @Autowired
    private FileStorage fileStorage;


    public record AddCommentFields(@NotNull UUID taskid, Optional<UUID> commentid, @NotNull CommentText text) { }
    public record CreatedCommentId(UUID commentid) {}

    @PostMapping(value = "/comment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatedCommentId> addComment(
            @RequestPart("commentFields") AddCommentFields commentFields,
            @RequestPart(value = "file", required = false) Optional<MultipartFile> optFile, @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException, TypeValidationException, SecurityException, TaskNotFoundException, BadRequestException, NotEnoughSpaceException, AccessDeniedException {

        Optional<Task> t = m_tasks.find(commentFields.taskid());
        if (t.isEmpty()) {
            throw new TaskNotFoundException(commentFields.taskid());
        }
        String username = userDetails.getUsername();


        if (!t.get().isResponsibleFor(new Username(username))) {
            throw new AccessDeniedException("User is not responsible for the task, therefore cannot add a comment");
        }

        List<Username> owners = new ArrayList<>(List.of(t.get().responsibilityOf()));
        owners.add(t.get().createdBy());

        Optional<String> imageFilename = Optional.empty();
        Optional<String> attachFilename = Optional.empty();
        Optional<String> originalFilename = Optional.empty();
        if (optFile.isPresent() && !optFile.get().isEmpty()) {
            FileStorage.StoredFile storedFile = fileStorage.store(optFile.get(), username, owners);
            FileStorage.FileType filetype = FileStorage.identifyType(optFile.get());
            imageFilename = (filetype == FileStorage.FileType.IMAGE) ? Optional.of(storedFile.storedFilename()) : Optional.empty();
            attachFilename = (filetype != FileStorage.FileType.IMAGE) ? Optional.of(storedFile.storedFilename()) : Optional.empty();
            originalFilename = Optional.of(storedFile.originalFilename());
        }

        UUID newComment = m_tasks.addComment(
                t.get(),
                commentFields.text().get(),
                imageFilename,
                attachFilename,
                originalFilename,
                username,
                commentFields.commentid()
        );

        LOGGER.info("Task after adding comment: {}", t.get());
        return ResponseEntity.ok(new CreatedCommentId(newComment));
    }
}