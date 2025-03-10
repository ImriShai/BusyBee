package com.securefromscratch.busybee.storage;

import com.securefromscratch.busybee.exceptions.TaskNotFoundException;
import com.securefromscratch.busybee.safety.*;
import jakarta.validation.constraints.NotNull;
import org.owasp.safetypes.exception.TypeValidationException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

@Service
public class TasksStorage {
    private static final Logger logger = Logger.getLogger(TasksStorage.class.getName());
    private List<Task> m_tasks;

    public TasksStorage() throws TypeValidationException, IOException, ClassNotFoundException {
        List<Task> tasks;
        try {
            tasks = loadTasks();
            if (tasks.isEmpty()) {
                InitialDataGenerator.fillWithData(tasks);
            }

        } catch (IOException | ClassNotFoundException | TypeValidationException e) {
            tasks = new ArrayList<>();
            InitialDataGenerator.fillWithData(tasks);
            logger.severe("Exception during initialization: " + e.getMessage());
        }
        FileStorage.initFiles(tasks);
        m_tasks = Collections.unmodifiableList(tasks);
    }

    public List<Task> getAll() {
        logger.info("Fetching all tasks");
        return m_tasks;
    }

    public List<Task> getTasks(Username related) {
        logger.info("Fetching tasks related to: " + related);
        return m_tasks.stream().filter((other) -> other.isResponsibleFor(related)).toList();
    }

    public UUID add(String name, String desc,String createdBy, String[] responsibilityOf) throws IOException, TypeValidationException {
        logger.info("Adding new task with name: " + name);
        Task newTask = new Task(new Name(name), new Description(desc), new Username(createdBy), createUsernames(responsibilityOf));
        return add(newTask);
    }

    public UUID add(String name, String desc, LocalDate dueDate,String createdBy, String[] responsibilityOf) throws IOException, TypeValidationException {
        logger.info("Adding new task with name: " + name + " and due date: " + dueDate);
        Task newTask = new Task(new Name(name), new Description(desc), new DueDate(dueDate), new Username(createdBy), createUsernames(responsibilityOf));
        return add(newTask);
    }

    public UUID add(String name, String desc, LocalDate dueDate, LocalTime dueTime,String createdBy, String[] responsibilityOf) throws IOException, TypeValidationException {
        logger.info("Adding new task with name: " + name + ", due date: " + dueDate + " and due time: " + dueTime);
        Task newTask = new Task(new Name(name), new Description(desc), new DueDate(dueDate), new DueTime(dueTime), new Username(createdBy), createUsernames(responsibilityOf));
        return add(newTask);
    }

    public boolean markDone(UUID taskid) throws IOException {
        logger.info("Marking task as done with ID: " + taskid);
        List<Task> modifiableTasks = new ArrayList<>(m_tasks);
        Iterator<Task> tasksItr = modifiableTasks.iterator();
        try {
            while (tasksItr.hasNext()) {
                Task t = tasksItr.next();
                if (t.taskid().equals(taskid)) {
                    if (t.done()) {
                        logger.info("Task already marked as done: " + taskid);
                        return true;
                    }
                    tasksItr.remove();
                    Task doneTask = Task.asDone(t);
                    modifiableTasks.add(doneTask);
                    try {
                        saveTasks(modifiableTasks);
                        m_tasks = Collections.unmodifiableList(modifiableTasks);
                    } catch (IOException e) {
                        logger.severe("Exception while saving tasks: " + e.getMessage());
                        throw e;
                    }
                    return false;
                }
            }
        } catch (TypeValidationException e) {
            logger.severe("Exception while marking task as done: " + e.getMessage());
            throw new RuntimeException("Failed to mark task as done", e);
        }
        logger.warning("Task not found with ID: " + taskid);
        throw new TaskNotFoundException(taskid);
    }

    public UUID add(Task newTask) throws IOException {
        logger.info("Adding new task: " + newTask);
        List<Task> modifiableTasks = new ArrayList<>(m_tasks);
        modifiableTasks.add(newTask);
        try {
            saveTasks(modifiableTasks);
            m_tasks = Collections.unmodifiableList(modifiableTasks);
        } catch (IOException e) {
            logger.severe("Exception while saving tasks: " + e.getMessage());
            throw e;
        }
        return newTask.taskid();
    }

    private List<Task> loadTasks() throws IOException, ClassNotFoundException {
        logger.info("Loading tasks from file");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("tasks.ser"))) {
            return (List<Task>) ois.readObject();
        } catch (ClassNotFoundException | FileNotFoundException e) {
            logger.warning("Exception while loading tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveTasks(List<Task> tasks) throws IOException {
        logger.info("Saving tasks to file");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tasks.ser"))) {
            oos.writeObject(tasks);
        }
    }



    public UUID addComment(Task task, String text, Optional<String> image, Optional<String> attachment, Optional<String> originalFilename,
                           String createdBy, Optional<UUID> after) throws IOException, TypeValidationException {
        logger.info("Adding comment with image/attachment to task: " + task.taskid());

        UUID commentId = UUID.randomUUID();
        TaskComment newComment = new TaskComment(commentId, text, image, attachment, new Username(createdBy), after, originalFilename);

        // Create a modified task with the new comment
        Task updatedTask = task.withComment(newComment);

        // Update the tasks list atomically
        try {
            updateTaskList(updatedTask);
        } catch (IOException e) {
            logger.severe("Exception while saving tasks: " + e.getMessage());
            throw e;
        }

         return commentId;
    }

    /**
     * Updates the task list atomically.
     */
    private synchronized void updateTaskList(Task updatedTask) throws IOException {
        List<Task> modifiableTasks = new ArrayList<>(m_tasks);

        // Replace the old task with the updated task
        modifiableTasks.replaceAll(task -> task.taskid().equals(updatedTask.taskid()) ? updatedTask : task);

        // Persist the updated list
        saveTasks(modifiableTasks);

        // Ensure thread-safety when updating the unmodifiable reference
        m_tasks = Collections.unmodifiableList(modifiableTasks);
    }

    public Optional<Task> find(UUID taskid) {
        logger.info("Finding task with ID: " + taskid);
        return m_tasks.stream().filter((other) -> other.taskid().equals(taskid)).findAny();
    }

    public boolean isTaskNameExists(String s) {
        logger.info("Checking if task name exists: " + s);
        return m_tasks.stream().anyMatch((other) -> other.name().get().equals(s));
    }

    private Username[] createUsernames(String[] responsibilityOf) {
        return Arrays.stream(responsibilityOf).map(name -> {
            try {
                return new Username(name);
            } catch (TypeValidationException e) {
                logger.severe("Invalid username: " + name);
                throw new RuntimeException("Invalid username: " + name, e);
            }
        }).toArray(Username[]::new);
    }

    public boolean isTaskIdExists(UUID taskid) {
        logger.info("Checking if task ID exists: " + taskid);
        return m_tasks.stream().anyMatch((other) -> other.taskid().equals(taskid));
    }


}