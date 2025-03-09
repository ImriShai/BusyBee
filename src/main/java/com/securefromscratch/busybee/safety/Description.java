package com.securefromscratch.busybee.safety;

import io.swagger.v3.oas.annotations.media.Schema;
import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.safetypes.exception.TypeValidationException;

import java.io.Serial;
import java.io.Serializable;


@Schema(type = "String", description = "Description")
public final class Description implements Serializable {
    public static final int MIN_LENGTH = 0;
    public static final int MAX_LENGTH = 1000;

    private final String desc;

    // Private constructor to enforce validation
    public Description(String value) throws TypeValidationException, SecurityException {
       this.desc = isValidDescription(value);

    }
    public Description() throws TypeValidationException {
        this.desc = isValidDescription("");
    }



    public static String isValidDescription(String value) throws SecurityException, TypeValidationException {

        if (value == null) {
            throw new TypeValidationException("Description cannot be null");
        }

        // Validate length
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new TypeValidationException("Description must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long");
        }

        // Validate printable ASCII and Hebrew characters
        if (!value.matches("^[\\p{Print}\\p{IsHebrew}\n]*$")) {
            throw new TypeValidationException("Description must contain only printable ASCII characters or Hebrew characters");
        }

        // Sanitize the description to prevent XSS
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("a", "img", "b", "i", "u", "strong", "em", "p", "br", "ul", "ol", "li")
                .allowUrlProtocols("http", "https")
                .allowAttributes("href").onElements("a")
                .allowAttributes("src").onElements("img")
                .toFactory();
        String sanitizedValue = policy.sanitize(value);

       // Check if the sanitized value is empty
        if (sanitizedValue.isEmpty() ) {
            throw new SecurityException("Description cannot be empty");
        }

        return sanitizedValue;

    }

    public String get() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Description that = (Description) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }

    @Override
    public String toString() {
        return desc;
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException, TypeValidationException {
        in.defaultReadObject();
        isValidDescription(desc);
    }
}