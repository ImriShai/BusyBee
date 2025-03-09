package com.securefromscratch.busybee.auth;

import com.securefromscratch.busybee.safety.Username;
import org.owasp.safetypes.exception.TypeValidationException;

public class UserAccount {
    private Username username;
    private String hashedPassword;
    private boolean enabled = true;

    public UserAccount(String username, String hashedPassword) throws TypeValidationException {

        this.username = new Username(username);
        this.hashedPassword = hashedPassword;


    }

    public String getUsername() {
        return username.get();
    }


    public void setUsername(String username) throws TypeValidationException {
            this.username = new Username(username);
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getRole() {
        return "USER";
    }

    public boolean isEnabled() {
        return enabled;
    }
}
