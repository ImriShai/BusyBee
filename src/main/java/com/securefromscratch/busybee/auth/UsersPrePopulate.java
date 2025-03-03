package com.securefromscratch.busybee.auth;

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
    CommandLineRunner createUser(UsersStorage usersStorage, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "Imri";

            String plainPassword = "1234";

//            String encodedPassword = passwordEncoder.encode(plainPassword); // The password is encoded in the UsersStorage class


            UserAccount newAccount = usersStorage.createUser(username, plainPassword);

            System.out.print("User created: ");
            System.out.println(newAccount.getUsername());
            System.out.println("******** Password: ");
            System.out.println(plainPassword);
            System.out.println("Hashed Password: ");
            System.out.println(newAccount.getHashedPassword());
        };
    }
}
