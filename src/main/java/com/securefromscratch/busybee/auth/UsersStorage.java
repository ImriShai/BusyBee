package com.securefromscratch.busybee.auth;

import com.securefromscratch.busybee.exceptions.UserAlreadyExistException;
import org.owasp.safetypes.exception.TypeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UsersStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersStorage.class);
    private final Map<String, UserAccount> m_users = new HashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Path USERS_FILE = Paths.get("src/main/java/com/securefromscratch/busybee/auth/users.txt");

    public UsersStorage() {
        loadUsers();
    }

    public Optional<UserAccount> findByUsername(String username) {
        LOGGER.info("Finding user by username: {}", username);
        return Optional.ofNullable(m_users.get(username));
    }

    public UserAccount createUser(String username, String password) throws TypeValidationException, UserAlreadyExistException, IOException {
        LOGGER.info("Creating user with username: {}", username);
        String hashedPassword = passwordEncoder.encode(password);
        UserAccount newAccount = new UserAccount(username, hashedPassword);

        if (m_users.putIfAbsent(newAccount.getUsername(), newAccount) != null) {
            LOGGER.error("User already exists: {}", newAccount.getUsername());
            throw new UserAlreadyExistException(newAccount.getUsername());
        }
        saveUser(newAccount);
        LOGGER.info("User created successfully: {}", username);
        return newAccount;
    }

    private void saveUser(UserAccount user) throws IOException {
        LOGGER.info("Saving user to file: {}", USERS_FILE);
        // We'll append this user to our file in a simple format
        if (!Files.exists(USERS_FILE)) {
            Files.createFile(USERS_FILE);
        }

        // Save in format: username:hashedPassword:enabled
        String userString = String.format("%s:%s:%b",
                user.getUsername(),
                user.getHashedPassword(),
                user.isEnabled());

        Files.writeString(USERS_FILE,
                userString + System.lineSeparator(),
                StandardCharsets.UTF_8,
                Files.exists(USERS_FILE) ?
                        java.nio.file.StandardOpenOption.APPEND :
                        java.nio.file.StandardOpenOption.CREATE);

        LOGGER.info("User saved successfully");
    }

    private void loadUsers() {
        LOGGER.info("Loading users from file: {}", USERS_FILE);
        if (!Files.exists(USERS_FILE)) {
            LOGGER.warn("Users file not found: {}", USERS_FILE);
            return;
        }

        try {
            m_users.clear();
            Files.lines(USERS_FILE, StandardCharsets.UTF_8).forEach(line -> {
                try {
                    String[] parts = line.split(":", 3);
                    if (parts.length >= 2) {
                        String username = parts[0];
                        String hashedPassword = parts[1];
                        boolean enabled = parts.length > 2 ? Boolean.parseBoolean(parts[2]) : true;

                        UserAccount account = new UserAccount(username, hashedPassword);


                        m_users.put(username, account);
                    }
                } catch (TypeValidationException e) {
                    LOGGER.error("Failed to load user from line: {}", line, e);
                }
            });
            LOGGER.info("Loaded {} users successfully", m_users.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load users: {}", e.getMessage(), e);
        }
    }

    public boolean hasUsers() {
        return !m_users.isEmpty();
    }
}