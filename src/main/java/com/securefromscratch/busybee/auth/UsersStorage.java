package com.securefromscratch.busybee.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.*;

@Service
public class UsersStorage {
    private final Map<String, UserAccount> m_users = new HashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(m_users.get(username));
    }

    public UserAccount createUser(String username, String password) {

        String hashedPassword = passwordEncoder.encode(password);
        UserAccount newAccount = new UserAccount(username, hashedPassword);
        
        m_users.putIfAbsent(username, newAccount);


        return newAccount;
    }
}
