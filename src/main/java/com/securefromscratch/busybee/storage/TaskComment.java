package com.securefromscratch.busybee.storage;

import com.securefromscratch.busybee.safety.DueDate;
import com.securefromscratch.busybee.safety.DueTime;
import com.securefromscratch.busybee.safety.Username;
import org.owasp.safetypes.exception.TypeValidationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class TaskComment implements java.io.Serializable {

    private enum AttachedFileType { NONE, IMAGE, ATTACHMENT }
    private final UUID commentid;
    private final String text;
    private final AttachedFileType attachedFileType;
    private final String imageOrAttachment;
    private final Username createdBy;
    private final DueDate dateCreatedOn;
    private final DueTime timeCreatedOn;
    private final int indent;
    private final String originalFilename;

    public TaskComment(String text, Optional<String> image, Optional<String> empty, Username uname, LocalDateTime createdOn) throws TypeValidationException {
        this(UUID.randomUUID(), text, image, empty, 0, uname, createdOn, Optional.empty());
    }

    public TaskComment(String text, Optional<String> image, Optional<String> empty, Username uname, LocalDateTime createdOn, UUID uuid) throws TypeValidationException {
        this(uuid, text, image, empty, 0, uname, createdOn, Optional.empty());
    }

    public TaskComment(String text, Username uname, LocalDateTime createdOn, UUID uuid) throws TypeValidationException {
        this(uuid, text, Optional.empty(), Optional.empty(), 0, uname, createdOn, Optional.empty());
    }

    public TaskComment(UUID uuid, String text, Username username, Optional<UUID> after) throws TypeValidationException {
        this(uuid, text, Optional.empty(), Optional.empty(), 0, username, LocalDateTime.now(), Optional.empty());
    }

    public TaskComment(UUID uuid, String text, Optional<String> image, Optional<String> attachment, Username username, Optional<UUID> after) throws TypeValidationException {
        this(uuid, text, image, attachment, 0, username, LocalDateTime.now(), Optional.empty());
    }

    public TaskComment(UUID uuid, String text, Optional<String> image, Optional<String> attachment, Username username, Optional<UUID> after, Optional<String> originalFilename) throws TypeValidationException {
        this(uuid, text, image, attachment, 0, username, LocalDateTime.now(), originalFilename);
    }

    public TaskComment(String text, Username createdBy, int indent) throws TypeValidationException {
        this(UUID.randomUUID(), text, Optional.empty(), Optional.empty(), indent, createdBy, LocalDateTime.now(), Optional.empty());
    }

    public TaskComment(String text, Username createdBy, LocalDateTime createdOn, int indent) throws TypeValidationException {
        this(UUID.randomUUID(), text, Optional.empty(), Optional.empty(), indent, createdBy, createdOn, Optional.empty());
    }

    public TaskComment(String text, Optional<String> image, Optional<String> attachment, Username createdBy, int indent) throws TypeValidationException {
        this(UUID.randomUUID(), text, image, attachment, indent, createdBy, LocalDateTime.now(), Optional.empty());
    }

    public TaskComment(String text, Optional<String> image, Optional<String> attachment, Username createdBy, LocalDateTime createdOn, int indent) throws TypeValidationException {
        this(UUID.randomUUID(), text, image, attachment, indent, createdBy, createdOn, Optional.empty());
    }

    private TaskComment(UUID commentid, String text, Optional<String> image, Optional<String> attachment, int indent, Username createdBy, LocalDateTime createdOn, Optional<String> originalFilename) throws TypeValidationException {
        this.commentid = commentid;
        this.text = text;

        AttachedFileType attachedFileType = AttachedFileType.NONE;
        String attachedFile;
        String originalAttachedFilename = null;
        if (image.isPresent() ) {
            attachedFile = image.get();
            attachedFileType = AttachedFileType.IMAGE;
            originalAttachedFilename = originalFilename.orElse(attachedFile);

        } else if (attachment.isPresent() ) {
            attachedFile = attachment.get();
            attachedFileType = AttachedFileType.ATTACHMENT;
            originalAttachedFilename = originalFilename.orElse(attachedFile);

        } else {
            attachedFile = null;
        }
        this.attachedFileType = attachedFileType;
        this.imageOrAttachment = attachedFile;
        this.indent = indent;
        this.createdBy = createdBy;
        this.dateCreatedOn = new DueDate(createdOn.toLocalDate());
        this.timeCreatedOn = new DueTime(createdOn.toLocalTime());
        this.originalFilename = originalAttachedFilename;
    }

    public UUID commentId() { return commentid; }
    public String text() { return text; }
    public Username createdBy() { return createdBy; }
    public LocalDateTime createdOn() { return LocalDateTime.of(dateCreatedOn.get(), timeCreatedOn.get()); }
    public int indent() { return indent; }

    public Optional<String> image() {
        return (attachedFileType == AttachedFileType.IMAGE)
                ? Optional.of(imageOrAttachment)
                : Optional.empty();
    }

    public Optional<String> attachment() {
        return (attachedFileType == AttachedFileType.ATTACHMENT)
                ? Optional.of(imageOrAttachment)
                : Optional.empty();
    }
    public Optional<String> originalFilename() {
        return Optional.ofNullable(originalFilename);
    }
    public boolean hasFile() {
        return attachedFileType != AttachedFileType.NONE;
    }
    public Optional<String> imageOrAttachment() {
        return Optional.ofNullable(imageOrAttachment);
    }

}