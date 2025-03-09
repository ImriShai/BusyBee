package com.securefromscratch.busybee.exceptions;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(UUID taskId, UUID commentId) {
        super("Comment with ID " + taskId + "::" + commentId + " not found.");
    }
}
