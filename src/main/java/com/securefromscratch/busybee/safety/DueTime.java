package com.securefromscratch.busybee.safety;

import org.owasp.safetypes.exception.TypeValidationException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DueTime  implements Serializable {
    private final LocalTime value;

    public DueTime(LocalTime value) throws TypeValidationException {
        if (value == null) {
            throw new TypeValidationException("Due time cannot be null");
        }
        if (value.isBefore(LocalTime.MIN) || value.isAfter(LocalTime.MAX)) {
            throw new TypeValidationException("Due time must be between 00:00 and 23:59");
        }


        this.value = value;
    }

    public DueTime(LocalDateTime value, LocalDateTime dueTime) throws TypeValidationException {
         if (value == null) {
            throw new TypeValidationException("Due time cannot be null");
        }
        if (!value.isBefore(dueTime)) {
            throw new TypeValidationException("Due time must be before the due date");
        }
        this.value = value.toLocalTime();
    }

    public DueTime(String value) throws TypeValidationException {
        this(LocalTime.parse(value));
    }

    public DueTime()  {
        this.value = LocalTime.now();
    }

    public LocalTime get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DueTime dueTime = (DueTime) o;
        return value.equals(dueTime.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}