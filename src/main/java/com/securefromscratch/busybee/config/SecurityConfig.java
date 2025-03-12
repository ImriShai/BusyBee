package com.securefromscratch.busybee.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.function.Supplier;




@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "index.html", "/register", "/favicon.ico", "/gencsrftoken").permitAll() // Allow static resources
                        .anyRequest().authenticated()

                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/main.html")
                        .permitAll()
                )


                .csrf(csrf -> csrf
                                .ignoringRequestMatchers("/h2-console/**") // Disable CSRF for H2 console
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())

                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline' https://maxcdn.bootstrapcdn.com https://getbootstrap.com; " +
                                                "img-src 'self' data:; " +
                                                "font-src 'self'; " +
                                                "connect-src 'self';" +
                                                " object-src 'none';" +
                                                " base-uri 'self'; " +
                                                "form-action 'self';"
                                                +
                                        "frame-ancestors 'none';"+
                                        "block-all-mixed-content;"+
                                "upgrade-insecure-requests;")                                )

                );
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public HandlerMappingIntrospector customHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }


}
