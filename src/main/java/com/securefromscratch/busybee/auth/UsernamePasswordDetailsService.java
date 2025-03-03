package com.securefromscratch.busybee.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsernamePasswordDetailsService implements UserDetailsService {
    private final UsersStorage m_usersStorage;

    public UsernamePasswordDetailsService(UsersStorage usersStorage) {
        this.m_usersStorage = usersStorage;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserAccount> userAccount = m_usersStorage.findByUsername(username);
        if (userAccount.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserAccount account = userAccount.get();
        return User
                .withUsername(account.getUsername())
                .password(account.getHashedPassword())
                .roles(account.getRole())
                .build();
    }

    public void createUser(String username, String password) {
        m_usersStorage.createUser(username, password);
    }
}