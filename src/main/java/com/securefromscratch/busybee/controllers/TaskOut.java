package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.safety.*;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TaskComment;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public record TaskOut(
        UUID taskid,
        String name,
        String desc,
        Optional<LocalDate> dueDate,
        Optional<LocalTime> dueTime,
        String createdBy,
        String[] responsibilityOf,
        LocalDateTime creationDatetime,
        boolean done,
        TaskCommentOut[] comments
) {
    public TaskOut(
            UUID taskid,
            Name name,
            Description desc,
            Optional<DueDate> dueDate,
            Optional<DueTime> dueTime,
            Username createdBy,
            Username[] responsibilityOf,
            DueDate creationDate,
            DueTime creationTime,
            boolean done,
            TaskCommentOut[] comments
    ) {
        this(
                taskid,
                name.get(),
                desc.get(),
                dueDate.map(DueDate::get),
                dueTime.map(DueTime::get),
                createdBy.get(),
                Arrays.stream(responsibilityOf).map(Username::get).toArray(String[]::new),
                LocalDateTime.of(creationDate.get(), creationTime.get()),
                done,
                comments
        );
    }

    static TaskOut fromTask(Task t) {
        Transformer<TaskComment, TaskCommentOut> transformer = c -> TaskCommentOut.fromComment((TaskComment) c);
        return new TaskOut(
                t.taskid(),
                t.name().get(),
                t.desc().get(),
                t.dueDate().map(DueDate::get),
                t.dueTime().map(DueTime::get),
                t.createdBy().get(),
                Arrays.stream(t.responsibilityOf()).map(Username::get).toArray(String[]::new),
                t.creationDatetime(),
                t.done(),
                CollectionUtils.collect(t.comments(), transformer).toArray(new TaskCommentOut[0])
        );
    }
}