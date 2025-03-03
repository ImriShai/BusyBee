package com.securefromscratch.busybee.auth;

public class UserAccount {
    private String username;
    private String hashedPassword;
    private boolean enabled = true;

    public UserAccount(String username, String hashedPassword) {

        this.username = username;
        this.hashedPassword = hashedPassword;


    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
