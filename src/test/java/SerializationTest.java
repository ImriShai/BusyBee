import com.securefromscratch.busybee.safety.*;
import com.securefromscratch.busybee.storage.Task;
import org.junit.jupiter.api.Test;
import org.owasp.safetypes.exception.TypeValidationException;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SerializationTest {

    @Test
    public void testUserNameSerialization() {
        try {
            // Create a Username object
            Username username = new Username("User123");

            // Serialize the object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(username);
            objectOutputStream.close();

            // Deserialize the object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Username deserializedUsername = (Username) objectInputStream.readObject();
            objectInputStream.close();

            // Verify the deserialized object
            System.out.println("Original: " + username);
            System.out.println("Deserialized: " + deserializedUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNameSerialization() {
        try {
            // Create a Name object
            Name name = new Name("John Doe");

            // Serialize the object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(name);
            objectOutputStream.close();

            // Deserialize the object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Name deserializedName = (Name) objectInputStream.readObject();
            objectInputStream.close();

            // Verify the deserialized object
            System.out.println("Original: " + name);
            System.out.println("Deserialized: " + deserializedName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommentTextSerialization() {
        try {
            // Create a CommentText object
            CommentText commentText = new CommentText("This is a comment");

            // Serialize the object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(commentText);
            objectOutputStream.close();

            // Deserialize the object
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            CommentText deserializedCommentText = (CommentText) objectInputStream.readObject();
            objectInputStream.close();

            // Verify the deserialized object
            System.out.println("Original: " + commentText.get());
            System.out.println("Deserialized: " + deserializedCommentText.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createInvalidFile() throws TypeValidationException {
        List<Task> tasks = new ArrayList<>();
        List<Task> tasks1 = new ArrayList<>();
        List<Task> tasks2 = new ArrayList<>();

        // Create a valid task
        Task validTask = new Task(new Name("Valid Task"), new Description("This is a valid task"),
                new DueDate(LocalDate.now()), new DueTime(LocalTime.MAX), new Username("User123"), new Username[]{new Username("User123")});
        tasks.add(validTask);

        // Create an invalid task with invalid DueDate and DueTime
        Task invalidTask = new Task(new Name("Yariv"), new Description("<script>alert('XSS')</script>"),
                new DueDate(LocalDate.MIN), new DueTime(LocalTime.MIN), new Username("User123"), new Username[]{new Username("User123")});
        tasks.add(invalidTask);

        // Create a valid task
        Task validTask1 = new Task(new Name("Valid Task"), new Description("This is a valid task"),
                new DueDate(LocalDate.now()), new DueTime(LocalTime.MAX), new Username("User123"), new Username[]{new Username("User123")});
        tasks1.add(validTask1);

        // Create an invalid task with invalid DueDate and DueTime
        Task invalidTask1= new Task(new Name("<script>alert('XSS')</script>"), new Description("Valid description"),
                new DueDate(LocalDate.MIN), new DueTime(LocalTime.MIN), new Username("User123"), new Username[]{new Username("User123")});
        tasks1.add(invalidTask1);
        
        // Create a valid task
        Task validTask2 = new Task(new Name("Valid Task"), new Description("This is a valid task"),
                new DueDate(LocalDate.now()), new DueTime(LocalTime.MAX), new Username("User123"), new Username[]{new Username("User123")});
        tasks2.add(validTask2);
        Task invalidTask2 = new Task(new Name("inValid Task"), new Description("This is an invalid task"),
                new DueDate(LocalDate.now()), new DueTime(LocalTime.MAX), new Username("<script>alert('XSS')</script>"), new Username[]{new Username("User123")});
        tasks2.add(invalidTask2);
        
        
        
        

        // Serialize the tasks to a .ser file
        try (FileOutputStream fos = new FileOutputStream("tasks_invalid.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(tasks);
            System.out.println("Serialized tasks to tasks_invalid.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try (FileOutputStream fos = new FileOutputStream("tasks_invalid1.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(tasks1);
            System.out.println("Serialized tasks to tasks_invalid1.ser");
        } catch (IOException e) {
            e.printStackTrace();
    }
        
        try (FileOutputStream fos = new FileOutputStream("tasks_invalid2.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(tasks2);
            System.out.println("Serialized tasks to tasks_invalid2.ser");
        } catch (IOException e) {
            e.printStackTrace();

        }
        }
}