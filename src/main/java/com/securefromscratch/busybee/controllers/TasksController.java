package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.exceptions.ConflictException;
import com.securefromscratch.busybee.safety.*;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.exceptions.TaskNotFoundException;
import com.securefromscratch.busybee.storage.TasksStorage;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.owasp.safetypes.exception.TypeValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080", "https://localhost:8443", "https://127.0.0.1:8443"})
public class TasksController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksController.class);

    @Autowired
    private TasksStorage m_tasks;

    public TasksController() throws IOException {
        // Use an absolute path or ensure the relative path is correct
        Path uploadPath = Path.of("uploads").toAbsolutePath();
    }

    @GetMapping("/tasks")
    public Collection<TaskOut> getTasks(@AuthenticationPrincipal UserDetails userDetails) throws TypeValidationException {
        List<Task> allTasks = m_tasks.getTasks(new Username(userDetails.getUsername()));
        Transformer<Task, TaskOut> transformer = t -> TaskOut.fromTask((Task) t);
        return CollectionUtils.collect(allTasks, transformer);
    }

    public record MarkDoneRequest(@NotNull UUID taskid,@NotNull boolean done) {}
    public record MarkDoneResponse(boolean success) {}

    @PostMapping("/done")
    public ResponseEntity<MarkDoneResponse> markTaskDone(@RequestBody MarkDoneRequest request, @AuthenticationPrincipal UserDetails userDetails) throws IOException, TypeValidationException {
        // Validate that the taskid is a legal UUID
        UUID taskId = UUID.fromString(request.taskid().toString());
        boolean done = request.done;

        // Check if the task exists, return 404 if not
        Optional<Task> taskOptional = m_tasks.find(taskId);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundException(request.taskid());
        }

        // Check that the user is responsible for the task
        Task task = taskOptional.get();
        if (!task.isResponsibleFor(new Username(userDetails.getUsername()))) {
            throw new AccessDeniedException("User is not responsible for the task");
        }

        // Mark the task as done
        boolean oldValue = m_tasks.markDone(request.taskid(), done);
        return ResponseEntity.ok(new MarkDoneResponse(!oldValue));
    }

    public record CreateTaskRequest(
            Name name,
            Description desc,
            DueDate dueDate,
            DueTime dueTime,
            Username[] responsibilityOf
    ) {}

    @PostMapping("/create")
    public ResponseEntity<UUID> create(@RequestBody CreateTaskRequest request, @AuthenticationPrincipal UserDetails userDetails) throws TypeValidationException, IOException, ConflictException {

        Username currentUser = new Username(userDetails.getUsername());

        // Check if task name already exists for this user or one of the responsible users
       List<Task> allTasks = m_tasks.getAll();
       for (Task task : allTasks) {
              if (task.name().equals(request.name()) &&( (task.createdBy().equals(currentUser) || Arrays.stream(request.responsibilityOf()).anyMatch(task::isResponsibleFor)))) {
                throw new ConflictException("Task name already exists");
              }
       }


        UUID taskId;

        if (request.dueDate() == null) { // Due date is optional
            taskId = m_tasks.add(
                    request.name().get(),
                    request.desc().get(),
                    currentUser.get(),
                    Arrays.stream(request.responsibilityOf()).map(Username::get).toArray(String[]::new)
            );
        } else if (request.dueTime() == null) { // Due time is optional
            taskId = m_tasks.add(
                    request.name().get(),
                    request.desc().get(),
                    request.dueDate().get(),
                    currentUser.get(),
                    Arrays.stream(request.responsibilityOf()).map(Username::get).toArray(String[]::new)
            );
        } else {
            taskId = m_tasks.add( // all fields are mandatory
                    request.name().get(),
                    request.desc().get(),
                    request.dueDate().get(),
                    request.dueTime().get(),
                    currentUser.get(),
                    Arrays.stream(request.responsibilityOf()).map(Username::get).toArray(String[]::new)
            );
        }

        return ResponseEntity.ok().body(taskId);
    }
}