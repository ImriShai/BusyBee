package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.safety.Description;
import com.securefromscratch.busybee.safety.Name;
import com.securefromscratch.busybee.storage.FileStorage;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TasksStorage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "null")
public class TasksController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksController.class);

    @Autowired
    private TasksStorage m_tasks;
    private FileStorage m_files = new FileStorage(Path.of("Skeleton/uploads"));

    public TasksController() throws IOException {
    }

    // Request: No arguments
    // Expected Response: [
    //    {
    //       "taskid": "<UUID>",
    //       "name": "<name>",
    //       "desc": "<desc>",
    //       "dueDate": "<date>",  // this is optional
    //       "dueTime": "<time>",  // this is optional
    //       "createdBy": "<name of user>",
    //       "responsibilityOf": [ "<user1">, "<user2>", ...],
    //       "creationDatetime": "<date+time>",
    //       "done": false/true,
    //       "comments": [ { comment1 }, { comment2 }, ... ] (see TaskCommentOut for fields)
    //    }, ...
    // ]
    @GetMapping("/tasks")
    public Collection<TaskOut> getTasks() {
        List<Task> allTasks = m_tasks.getAll();
        Transformer<Task, TaskOut> transformer = t-> TaskOut.fromTask((Task)t);
        return CollectionUtils.collect(allTasks, transformer);
    }

    // Request: { "taskid": "<uuid>" }
    // Expected Response: { "success": true/false }
    record MarkDoneRequest(UUID taskid) {}
    record MarkDoneResponse(boolean success) {}
    @PostMapping("/done")
    public ResponseEntity markTaskDone(@RequestBody MarkDoneRequest request) {
        try {
            boolean oldValue = m_tasks.markDone(request.taskid());
            return ResponseEntity.ok(new MarkDoneResponse(!oldValue));
        } catch (Exception e) {
            LOGGER.error("Failed to mark task as done", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }

    }

    // Request: {
    //     "name": "<task name>",
    //     "desc": "<description>",
    //     "dueDate": "<date>", // or null
    //     "dueTime": "<time>", // or null
    //     "responsibilityOf": [ "<name1>", "<name2>", ... ]
    // }
    // Expected Response: { "taskid": "<uuid>" }

    record CreateTaskRequest(Name name, Description desc, LocalDate dueDate, LocalTime dueTime, Name[] responsibilityOf) {}
    @PostMapping("/create")
    public ResponseEntity create(@RequestBody CreateTaskRequest request) {
        System.out.println("Create task request: " + request);
        try {
            UUID taskid = m_tasks.add(request.name().get(), request.desc().get(), request.dueDate(), request.dueTime(), Arrays.stream(request.responsibilityOf()).map(Name::get).toArray(String[]::new));
            return ResponseEntity.ok().body(taskid);
        } catch (Exception e) {
            LOGGER.error("Failed to create task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public record FileRecord(String img) {}
    @GetMapping("/image")
    public ResponseEntity<byte[]> getImages(FileRecord fileRecord){
        Path p = Path.of("../uploads").toAbsolutePath();
        try {
            return ResponseEntity.ok().body(m_files.getBytes(fileRecord.img()));
        }catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new byte[0]);
        }
    }

}
