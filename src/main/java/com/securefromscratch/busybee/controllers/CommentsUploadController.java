package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.safety.CommentText;
import com.securefromscratch.busybee.storage.FileStorage;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TaskNotFoundException;
import com.securefromscratch.busybee.storage.TasksStorage;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class CommentsUploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsUploadController.class);

    @Autowired
    private TasksStorage m_tasks;

    public record AddCommentFields(@NotNull UUID taskid, Optional<UUID> commentid, @NotNull CommentText text) { }
    public record CreatedCommentId(UUID commentid) {}
    @PostMapping(value = "/comment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatedCommentId> addComment(
            @RequestPart("commentFields") AddCommentFields commentFields,
            @RequestPart(value = "file", required = false) Optional<MultipartFile> optFile
    ) throws IOException {
        Optional<Task> t = m_tasks.find(commentFields.taskid());
        if (t.isEmpty()) {
            throw new TaskNotFoundException(commentFields.taskid());
        }

        if (optFile.isEmpty() || optFile.get().isEmpty()) {
            UUID newComment = m_tasks.addComment(t.get(), commentFields.text().get(), "Yariv", commentFields.commentid());
            return ResponseEntity.ok(new CreatedCommentId(newComment));
        }


		String storedFilename = filePartProcessing(optFile.get());

        FileStorage.FileType filetype = FileStorage.identifyType(optFile.get());
        Optional<String> imageFilename = (filetype == FileStorage.FileType.IMAGE) ? Optional.of(storedFilename) : Optional.empty();
        Optional<String> attachFilename = (filetype != FileStorage.FileType.IMAGE) ? Optional.of(storedFilename) : Optional.empty();

        UUID newComment = m_tasks.addComment(
                t.get(),
                commentFields.text().get(),
                imageFilename,
                attachFilename,
                "Yariv",
                commentFields.commentid()
        );
		return ResponseEntity.ok(new CreatedCommentId(newComment));
    }

	private String filePartProcessing(MultipartFile fileData) throws IOException {

        FileStorage storage = new FileStorage(Path.of("Skeleton/uploads"));
        try {
            return storage.store(fileData).getFileName().toString();
        } catch (IOException e) {
            LOGGER.error("Failed to store file", e);
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Failed to store file");
        }
	}
}
