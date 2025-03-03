package com.securefromscratch.busybee.safety;

public class CommentText {
    public static final int MIN_LENGTH = 0;
    public static final int MAX_LENGTH = 100;

    private final String text;

    public CommentText(String value) {
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Comment must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long");
        }
        for (char c : value.toCharArray()) {
            if (c < 32 || c > 126) {
                throw new IllegalArgumentException("Comment must contain only printable ASCII characters");
            }
        }
        this.text = value;
    }

    public String get() {
        return text;
    }
}
