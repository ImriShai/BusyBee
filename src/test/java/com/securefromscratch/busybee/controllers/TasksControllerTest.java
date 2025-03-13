package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.safety.Description;
import org.junit.jupiter.api.Test;
import org.owasp.safetypes.exception.TypeValidationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TasksControllerTest {

    @Test
    void testSanitizeDescription_PreventXSS()  {
        String input = "<script>alert('XSS');</script> <a href=\"https://example.com\">Link</a>";
        String expected = " <a href=\"https://example.com\">Link</a>";
        Description description = assertDoesNotThrow(() -> new Description(input));
        assertEquals(expected, description.get());
    }
    @Test
    void testSanitizeDescription_PreventXSS2()  {
        String input = "<script>alert('XSS');</script>";
        assertThrows(SecurityException .class, () -> new Description(input));
    }

    @Test
    void testSanitizeDescription_AllowSafeHtml() throws TypeValidationException {
        String input = "<strong>and</strong> <a href=\"https://example.com\">a link</a>";
        assertDoesNotThrow(() -> {new Description(input);});
        assertEquals(input, new Description(input).get());
    }

    @Test
    void testSanitizeDescription_InvalidInput() {
        String input = "Invalid description with \u0000 null character.";
        assertThrows(TypeValidationException.class, () -> new Description(input));
    }
}