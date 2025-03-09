package com.securefromscratch.busybee.auth;

import com.securefromscratch.busybee.exceptions.UserAlreadyExistException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
public class UsersPrePopulate {
    @Bean
    CommandLineRunner createUser(UsernamePasswordDetailsService usersDetails, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "Imri";

            String plainPassword = "1234";



            UserAccount newAccount;
            try {
                newAccount = usersDetails.createUser(username, plainPassword);
            } catch (UserAlreadyExistException e) {
                throw new RuntimeException(e);
            }
            System.out.print("User created: ");
            System.out.println(newAccount.getUsername());
            System.out.println("******** Password: ");
            System.out.println(plainPassword);
            System.out.println("Hashed Password: ");
            System.out.println(newAccount.getHashedPassword());
        };
    }
}
