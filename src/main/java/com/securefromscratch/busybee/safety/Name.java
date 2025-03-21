package com.securefromscratch.busybee.safety;

import io.swagger.v3.oas.annotations.media.Schema;
import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.safetypes.exception.TypeValidationException;
import org.owasp.safetypes.types.string.BoundedString;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import java.lang.SecurityException;

import java.io.*;

@Schema(type = "String", description = "Name")
public final class Name extends BoundedString implements Serializable {
    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 100;

    private Name() throws TypeValidationException {
        super("");
    }

    @ConstructorBinding
    public Name(String value) throws TypeValidationException {
        super(value);
        validateName(value);
    }

    @Override
    public Integer min() {
        return MIN_LENGTH;
    }

    @Override
    public Integer max() {
        return MAX_LENGTH;
    }

    private void validateName(String value) throws TypeValidationException, SecurityException {
        if (value == null || value.isEmpty()) {
            throw new TypeValidationException("Name cannot be null or empty");
        }

        if (value.matches(".*[<>].*")) {
            throw new SecurityException("Name must not contain HTML tags");
        }

        if (!value.matches("^[a-zA-Z0-9 \u0590-\u05FF]*$")) {
            throw new SecurityException("Name must contain only letters, numbers, spaces, and Hebrew characters");
        }

        PolicyFactory policy = new HtmlPolicyBuilder()
                .disallowElements("<", ">")
                .toFactory();
        String sanitizedValue = policy.sanitize(value);

        if (!sanitizedValue.equals(value)) {
            throw new SecurityException("Name contains invalid characters after sanitization");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return get().equals(name.get());
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public String toString() {
        return get();
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
        validateName(value); // Revalidate the deserialized value

    }

    // Custom serialization proxy
    @Serial
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private static class SerializationProxy implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private final String value;

        SerializationProxy(Name username) {
            this.value = username.get();
        }

        @Serial
        private Object readResolve() throws TypeValidationException {
            return new Name(value);
        }
    }
}