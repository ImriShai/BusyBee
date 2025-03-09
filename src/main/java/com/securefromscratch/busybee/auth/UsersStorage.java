package com.securefromscratch.busybee.auth;

import com.securefromscratch.busybee.exceptions.UserAlreadyExistException;
import com.securefromscratch.busybee.safety.Username;
import org.owasp.safetypes.exception.TypeValidationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.*;

@Service
public class UsersStorage {
    private final Map<String, UserAccount> m_users = new HashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String USERS_FILE = "users.json";

    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(m_users.get(username));
    }

    public UserAccount createUser(String username, String password) throws TypeValidationException, UserAlreadyExistException {

        String hashedPassword = passwordEncoder.encode(password);
        UserAccount newAccount = new UserAccount(username, hashedPassword);


        
        if(m_users.putIfAbsent(newAccount.getUsername(), newAccount) != null) {
            throw new UserAlreadyExistException(newAccount.getUsername());
        }
        return newAccount;
    }



}
