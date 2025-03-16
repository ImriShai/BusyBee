package com.securefromscratch.busybee.safety;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.safetypes.exception.TypeValidationException;

import java.io.*;

public class CommentText implements Serializable {
    public static final int MIN_LENGTH = 0;
    public static final int MAX_LENGTH = 100;

    private final String text;

    public CommentText(String value) throws TypeValidationException {
        if (value.length() > MAX_LENGTH || value.isEmpty()) {
            throw new IllegalArgumentException("Comment must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long");
        }
        validateComment(value);
        this.text = value;
    }
    private void validateComment(String value) throws TypeValidationException {
        if (value == null || value.isEmpty()) {
            throw new TypeValidationException("Name cannot be null or empty");
        }


        if (!value.matches("^[\\p{Print}\\p{IsHebrew}]*$")) {
            throw new TypeValidationException("Name must contain only letters, numbers, spaces, and Hebrew characters");
        }

        // Sanitize the description to prevent XSS
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("a", "img", "b", "i", "u", "strong", "em", "p", "br")
                .allowUrlProtocols("http", "https")
                .allowAttributes("href").onElements("a")
                .allowAttributes("src").onElements("img")
                .toFactory();
        String sanitizedValue = policy.sanitize(value);

        if (!sanitizedValue.equalsIgnoreCase(value)) {
            throw new TypeValidationException("Name contains invalid characters after sanitization");
        }
    }

    public String get() {
        return text;
    }

    // Custom serialization logic
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(get()); // Serialize the underlying string value
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, TypeValidationException {
        in.defaultReadObject();
        String value = in.readUTF(); // Deserialize the underlying string value
        validateComment(value); // Revalidate the deserialized value
    }

}
