package com.securefromscratch.busybee.safety;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "String", description = "Description")
public class Description {
    public static final int MIN_LENGTH = 0;
    public static final int MAX_LENGTH = 1000;

    private final String desc;

    // Allow only printable ASCII characters
    public Description(String value) {
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Description must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long");
        }
        for (char c : value.toCharArray()) {
            if (c < 32 || c > 126) {
                throw new IllegalArgumentException("Description must contain only printable ASCII characters");
            }
        }
        this.desc = value;
    }

    public String get() {
        return desc;
    }
}
