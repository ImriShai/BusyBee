package com.securefromscratch.busybee.safety;

import org.owasp.safetypes.exception.TypeValidationException;

import java.io.Serializable;
import java.time.LocalDate;

public final class DueDate implements Serializable {
    private final LocalDate value;

    public DueDate(){
        this.value = LocalDate.now();
    }

    public DueDate(LocalDate value) throws TypeValidationException  {
        if (value == null) {
            throw new TypeValidationException("Due date cannot be null");
        }
        this.value = value;
    }

    public DueDate(LocalDate value, LocalDate creationDate, boolean isDueDate) throws TypeValidationException {
        if (value == null) {
            throw new TypeValidationException("Due date cannot be null");
        }
        if (isDueDate) {
            if (value.isBefore(creationDate) || creationDate.isAfter(value) || value.isBefore(LocalDate.now()) || creationDate.isAfter(LocalDate.now())) {
                throw new TypeValidationException("Due date cannot be before creation date");
            }
        }
        else { //were creating a CreationDate, so need to make sure Due is after creation
            if (value.isAfter(creationDate) || value.isAfter(LocalDate.now()) ) {
                throw new TypeValidationException("Creation date cannot be after due date");
            }

        }
        this.value = value;
    }

    public DueDate(String value) throws TypeValidationException {
        this(LocalDate.parse(value));
    }

    public LocalDate get() {
        return value;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DueDate dueDate = (DueDate) o;
        return value.equals(dueDate.value);
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