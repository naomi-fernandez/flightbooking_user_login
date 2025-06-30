package com.userlogin.flightbooking.controller;

import com.userlogin.flightbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
@RequestMapping("/test")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/db-connection")
    public String testDatabaseConnection() {
        try {
            Connection connection = dataSource.getConnection();
            String dbName = connection.getMetaData().getDatabaseProductName();
            String dbUrl = connection.getMetaData().getURL();
            connection.close();
            
            long userCount = userRepository.count();
            
            return "Database Connection: SUCCESS\n" +
                   "Database: " + dbName + "\n" +
                   "URL: " + dbUrl + "\n" +
                   "Users in database: " + userCount;
        } catch (Exception e) {
            return "Database Connection: FAILED\nError: " + e.getMessage();
        }
    }

    @GetMapping("/auth-info")
    public String getAuthInfo() {
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null) {
                return "No authentication found";
            }
            
            return "User: " + auth.getName() + "\n" +
                   "Authorities: " + auth.getAuthorities() + "\n" +
                   "Authenticated: " + auth.isAuthenticated();
        } catch (Exception e) {
            return "Auth check failed: " + e.getMessage();
        }
    }
}