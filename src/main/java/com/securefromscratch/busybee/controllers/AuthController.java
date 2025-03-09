package com.securefromscratch.busybee.controllers;

import com.securefromscratch.busybee.auth.UsernamePasswordDetailsService;
import com.securefromscratch.busybee.exceptions.*;
import com.securefromscratch.busybee.safety.Name;
import com.securefromscratch.busybee.storage.Task;
import com.securefromscratch.busybee.storage.TasksStorage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.owasp.safetypes.exception.TypeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.securefromscratch.busybee.auth.UsersStorage;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080", "https://localhost:8443", "https://127.0.0.1:8443"})
public class AuthController {
        private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
        private final UsernamePasswordDetailsService m_usernamePasswordDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

        public AuthController(UsersStorage usersStorage) throws TypeValidationException, IOException, ClassNotFoundException {
            this.m_usernamePasswordDetailsService = new UsernamePasswordDetailsService(usersStorage);
        }


    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@RequestBody Map<String, String> payload, HttpServletRequest request, HttpSession httpSession) throws TypeValidationException, UserAlreadyExistException {
        String username = payload.get("username");
        String password = payload.get("password");
        LOGGER.info("Register request received for user: {}", username);
        m_usernamePasswordDetailsService.createUser(username, password);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully.");
        response.put("redirectTo", "/main.html");

        // Auto-login after registration
        UserDetails userDetails = m_usernamePasswordDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());


        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}