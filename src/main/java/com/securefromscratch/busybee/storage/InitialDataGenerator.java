package com.securefromscratch.busybee.storage;

import com.securefromscratch.busybee.safety.*;
import org.owasp.safetypes.exception.TypeValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InitialDataGenerator {
    public static void fillWithData(List<Task> tasks) throws TypeValidationException {
        List<LocalDateTime> randomPastDates = generateRandomDateTimes(15, 5);
        List<LocalDateTime> randomFutureDates = generateRandomDateTimes(10, -5);

//        tasks.addAll(List.of(
//                new Task(
//                        new Name("Buy ingredients for Caprese Sandwich"),
//                        new Description("""
//                                    <ul>
//                                        <li>1 fresh baguette or ciabatta roll, sliced in half</li>
//                                        <li>1-2 ripe tomatoes, sliced</li>
//                                        <li>Fresh mozzarella cheese, sliced</li>
//                                        <li>Fresh basil leaves</li>
//                                        <li>1 tbsp balsamic glaze (optional)</li>
//                                        <li>Salt and pepper to taste</li>
//                                        <li>Olive oil for drizzling</li>
//                                    </ul>
//                                """),
//                        new DueDate(randomFutureDates.remove(randomFutureDates.size() - 1).toLocalDate()),
//                        new Username("Yariv"),new Username[] {new Username("Rita"), new Username("Rami")} ,randomPastDates.remove(0)
//
//                ),
//                new Task(
//                        new Name("Get sticker for car"),
//                        new Description("Parking inside Ariel requires sticker. You can get one at security office."),
//                        new DueDate(LocalDate.now().plusDays(10)),
//                        new Username("Ariel Security Department"),
//                        new Username[]{new Username("Yariv")}, randomPastDates.remove(0)
//                ),
//                new Task(
//                        new Name("Change closet from summer to winter"),
//                        new Description("Winter is Coming"),
//                        new DueDate(LocalDate.now().plusDays(20)),
//                        new Username("Ariel Security Department"),
//                        new Username[]{new Username("Yariv"), new Username("Or")},  randomPastDates.remove(0)
//                ),
//                new Task(
//                        new Name("Prepare lab report 1"),
//                        new Description("Can be found at <a href='https://moodlearn.ariel.ac.il/mod/resource/view.php?id=2011102'>moodle lab report 1</a>"),
//                        new DueDate(LocalDate.now()),
//                        new DueTime(randomFutureDates.remove(randomFutureDates.size() - 1).toLocalTime()),
//                        new Username("Yariv"),
//                        new Username[]{new Username("Students"), new Username("Nisan"), new Username("Rony"), new Username("Aviv")}, randomPastDates.remove(0)
//                )
//        ));
//        Task updatedTask = tasks.get(0).withComment(new TaskComment(
//                "Out of tomatoes in local supermarket",
//                Optional.of("Wikimedia-Corona_Lockdown_Tirupur,_Tamil_Nadu_(3).jpg"),
//                Optional.empty(),
//                new Username("Rita"),
//                randomPastDates.remove(0)
//        ));
//        UUID c0_1 = updatedTask.comments().get(updatedTask.comments().size() - 1).commentId();
//        tasks.set(0, updatedTask);
//
//        updatedTask = tasks.get(0).withComment(new TaskComment(
//                "Found and bought at our favorite grocer",
//                Optional.of("wikimedia_Fresh_vegetable_stall.jpg"),
//                Optional.empty(),
//                new Username("Rami"),
//                randomPastDates.remove(0),
//                c0_1
//        ));
//        tasks.set(0, updatedTask);
//
//        updatedTask = tasks.get(2).withComment(new TaskComment(
//                "באמת הגיע הזמן לסדר את הבלאגן בארון",
//                Optional.of("wikipedia_Space-saving_closet.JPG"),
//                Optional.empty(),
//                new Username("Or"),
//                randomPastDates.remove(0)
//        ));
//        tasks.set(2, updatedTask);
//
//        updatedTask = tasks.get(tasks.size() - 1).withComment(new TaskComment(
//                "מישהו יודע את התשובה לשאלה 12?",
//                Optional.empty(),
//                Optional.of("דוח מעבדה עקרונות תכנות מאובטח.docx"),
//                new Username("Nisan"),
//                randomPastDates.remove(0)
//        ));
//        UUID c3_1 = updatedTask.comments().get(updatedTask.comments().size() - 1).commentId();
//        tasks.set(tasks.size() - 1, updatedTask);
//
//        updatedTask = tasks.get(tasks.size() - 1).withComment(new TaskComment(
//                "פשוט תעתיק את התוצאה מחלון הפקודה",
//                Optional.of("CommandWindow.png"),
//                Optional.empty(),
//                new Username("Rony"),
//                randomPastDates.remove(0),
//                c3_1
//        ));
//        tasks.set(tasks.size() - 1, updatedTask);
//
//        updatedTask = tasks.get(tasks.size() - 1).withComment(new TaskComment(
//                "אתה מתכוון לשאלה עם ה-POST?",
//                new Username("Aviv"),
//                randomPastDates.remove(0),
//                c3_1
//        ));
//        tasks.set(tasks.size() - 1, updatedTask);
//
//        updatedTask = tasks.get(tasks.size() - 1).withComment(new TaskComment(
//                "המחשב נתקע. מה עושים?",
//                new Username("Rony"),
//                randomPastDates.remove(0),
//                0
//        ));
//        UUID c3_2 = updatedTask.comments().get(updatedTask.comments().size() - 1).commentId();
//        tasks.set(tasks.size() - 1, updatedTask);
    }

    public static List<LocalDateTime> generateRandomDateTimes(int numberOfDates, int daysAgo) {
        List<LocalDateTime> randomDateTimes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastDate = now.minusDays(daysAgo);

        long startEpoch = pastDate.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endEpoch = now.atZone(ZoneId.systemDefault()).toEpochSecond();
        if (startEpoch > endEpoch) {
            long oldStartEpoch = startEpoch;
            startEpoch = endEpoch;
            endEpoch = oldStartEpoch;
        }

        for (int i = 0; i < numberOfDates; i++) {
            long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch + 1);
            LocalDateTime randomDateTime = LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneId.systemDefault().getRules().getOffset(now));
            randomDateTimes.add(randomDateTime);
        }

        // Sort the dates in ascending order
        randomDateTimes.sort(LocalDateTime::compareTo);

        return randomDateTimes;
    }
}