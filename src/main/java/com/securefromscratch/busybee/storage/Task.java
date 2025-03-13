package com.securefromscratch.busybee.storage;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.securefromscratch.busybee.safety.Description;
import com.securefromscratch.busybee.safety.DueDate;
import com.securefromscratch.busybee.safety.DueTime;
import com.securefromscratch.busybee.safety.Name;
import com.securefromscratch.busybee.safety.Username;
import org.owasp.safetypes.exception.TypeValidationException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

// This class is now truly immutable
public final class Task implements Serializable {
    private final UUID m_taskid;
    private final Name m_name;
    private final Description m_desc;
    private final DueDate m_dueDate;
    private final DueTime m_dueTime;
    private final Username m_createdBy;
    private final Username[] m_responsibilityOf;
    private final DueDate m_creationDate;
    private final DueTime m_creationTime;
    private final boolean m_done;
    private final List<TaskComment> m_comments;

    // Constructor for creating a new Task
    public Task(Name name, Description desc, Username createdBy, Username[] responsibilityOf) throws TypeValidationException {
        this(name, desc, Optional.empty(), Optional.empty(), createdBy, responsibilityOf);
    }

    public Task(Name name, Description desc, DueDate dueDate, Username createdBy, Username[] responsibilityOf) throws TypeValidationException {
        this(name, desc, Optional.of(dueDate), Optional.empty(), createdBy, responsibilityOf);
    }

    public Task(Name name, Description desc, DueDate dueDate, Username createdBy) throws TypeValidationException {
        this(name, desc, Optional.of(dueDate), Optional.empty(), createdBy, new Username[]{createdBy});
    }

    public Task(Name name, Description desc, DueDate dueDate, DueTime dueTime, Username createdBy, Username[] responsibilityOf) throws TypeValidationException {
        this(name, desc, Optional.of(dueDate), Optional.of(dueTime), createdBy, responsibilityOf);
    }

    public Task(Name name, Description desc, DueDate dueDate, DueTime dueTime, Username createdBy) throws TypeValidationException {
        this(name, desc, Optional.of(dueDate), Optional.of(dueTime), createdBy, new Username[]{createdBy});
    }

    private Task(
            Name name,
            Description desc,
            Optional<DueDate> dueDate,
            Optional<DueTime> dueTime,
            Username createdBy,
            Username[] responsibilityOf
    ) throws TypeValidationException {
        this(UUID.randomUUID(), name, desc,
                dueDate.orElse(null),
                dueTime.orElse(null),
                createdBy, responsibilityOf, LocalDateTime.now(), false, Collections.emptyList());
    }

    private Task(
            UUID taskid,
            Name name,
            Description desc,
            DueDate dueDate,
            DueTime dueTime,
            Username createdBy,
            Username[] responsibilityOf,
            LocalDateTime creationDatetime,
            boolean done,
            List<TaskComment> comments
    ) throws TypeValidationException {
        this.m_taskid = taskid;
        this.m_name = name;
        this.m_desc = desc;
        this.m_dueDate = dueDate;
        this.m_dueTime = dueTime;
        this.m_createdBy = createdBy;

        this.m_responsibilityOf = responsibilityOf.clone(); // Defensive copy
        if (dueDate == null) {
            this.m_creationDate = new DueDate(creationDatetime.toLocalDate());
            this.m_creationTime = new DueTime(creationDatetime.toLocalTime());
        } else {
            this.m_creationDate = new DueDate(creationDatetime.toLocalDate(), dueDate.get(), false);

            if (dueTime != null) {
                this.m_creationTime = new DueTime(creationDatetime, LocalDateTime.of(dueDate.get(), dueTime.get()));
            } else {
                this.m_creationTime = new DueTime(creationDatetime.toLocalTime());
            }
        }


        this.m_done = done;
        this.m_comments = Collections.unmodifiableList(new ArrayList<>(comments)); // Immutable list
    }

    public Task(Name name, Description description, DueDate dueDate, Username uname, LocalDateTime created) throws TypeValidationException {

        this(UUID.randomUUID(), name, description, dueDate, new DueTime(LocalTime.MIN), uname, new Username[]{uname}, created, false, Collections.emptyList());
    }

    public Task(Name name, Description desc, DueDate dueDate, Username uname, Username[] unames, LocalDateTime created) throws TypeValidationException {
        this(UUID.randomUUID(), name, desc, dueDate, new DueTime(LocalTime.MIN), uname, unames, created, false, Collections.emptyList());
    }

    public Task(Name name, Description description, DueDate dueDate, DueTime dueTime, Username uname, Username[] unames, LocalDateTime created) throws TypeValidationException {
        this(UUID.randomUUID(), name, description, dueDate, dueTime, uname, unames, created, false, Collections.emptyList());
    }


    // Factory method to create a new Task with additional comments
    public Task withComment(TaskComment comment) throws TypeValidationException {
        List<TaskComment> newComments = new ArrayList<>(this.m_comments);
        newComments.add(comment);
        return new Task(
                this.m_taskid,
                this.m_name,
                this.m_desc,
                this.m_dueDate,
                this.m_dueTime,
                this.m_createdBy,
                this.m_responsibilityOf,
                creationDatetime(),
                this.m_done,
                newComments
        );
    }

    // Factory method to create a new Task with comments removed
    public Task withoutComment(UUID commentId) throws TypeValidationException {
        List<TaskComment> newComments = this.m_comments.stream()
                .filter(comment -> !comment.commentId().equals(commentId))
                .collect(Collectors.toList());
        return new Task(
                this.m_taskid,
                this.m_name,
                this.m_desc,
                this.m_dueDate,
                this.m_dueTime,
                this.m_createdBy,
                this.m_responsibilityOf,
                creationDatetime(),
                this.m_done,
                newComments
        );
    }

    // Factory method to mark the task as done
    public static Task asDone(Task task, boolean done) throws TypeValidationException {
        return new Task(
                task.m_taskid,
                task.m_name,
                task.m_desc,
                task.m_dueDate,
                task.m_dueTime,
                task.m_createdBy,
                task.m_responsibilityOf,
                task.creationDatetime(),
                done,
                task.m_comments
        );
    }

    // Getters
    public UUID taskid() {
        return m_taskid;
    }

    public Name name() {
        return m_name;
    }

    public Description desc() {
        return m_desc;
    }

    public Username createdBy() {
        return m_createdBy;
    }

    public Username[] responsibilityOf() {
        return m_responsibilityOf.clone();
    } // Defensive copy

    public LocalDateTime creationDatetime() {
        return LocalDateTime.of(m_creationDate.get(), m_creationTime.get());
    }

    public boolean done() {
        return m_done;
    }

    public List<TaskComment> comments() {
        return m_comments;
    }

    public Optional<DueDate> dueDate() {
        return Optional.ofNullable(m_dueDate);
    }

    public Optional<DueTime> dueTime() {
        return Optional.ofNullable(m_dueTime);
    }

    // Helper method to validate DueDate and DueTime
    public boolean isDueDateTimeValid() {
        if (this.dueDate().isEmpty() && this.dueTime().isEmpty()) {
            return true; // DueDate is optional
        }
        if (this.dueDate().isEmpty()) {
            return false; // If DueTime is present, DueDate must be present
        }

        if (this.dueTime().isEmpty()) {
            LocalDate dueDate = this.dueDate().orElse(null).get();
            LocalDate creationDate = creationDatetime().toLocalDate();
            return !dueDate.isBefore(creationDate); // DueDate is after the creation date

        }

        LocalDate dueDate = this.dueDate().orElse(null).get();
        LocalTime dueTime = this.dueTime().orElse(null).get();
        LocalDateTime creationDateTime = creationDatetime();

        if (dueDate != null && dueDate.isBefore(creationDateTime.toLocalDate())) {
            return false; // DueDate is before the creation date
        }

        if (dueDate != null && dueTime != null && dueDate.isEqual(creationDateTime.toLocalDate())) {
            return !dueTime.isBefore(creationDateTime.toLocalTime()); // DueTime is before the creation time on the same day
        }

        return true;
    }

    public boolean isResponsibleFor(Username username) {
        return Arrays.asList(m_responsibilityOf).contains(username) || m_createdBy.equals(username);
    }


    // ReadObject method to validate the deserialized object
    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException, TypeValidationException {
        in.defaultReadObject();
        if (!isDueDateTimeValid()) {
            throw new TypeValidationException("Invalid due date or due time");
        }
    }




    @Override
    public String toString() {
        return "Task{" +
                "m_taskid=" + m_taskid +
                ", m_name=" + m_name +
                ", m_desc=" + m_desc +
                ", m_dueDate=" + m_dueDate +
                ", m_dueTime=" + m_dueTime +
                ", m_createdBy=" + m_createdBy +
                ", m_responsibilityOf=" + Arrays.toString(m_responsibilityOf) +
                ", m_creationDatetime=" + creationDatetime() +
                ", m_done=" + m_done +
                ", m_comments=" + m_comments +
                '}';
    }
}