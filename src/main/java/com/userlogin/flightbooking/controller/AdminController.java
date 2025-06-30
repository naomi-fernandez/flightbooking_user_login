package com.userlogin.flightbooking.controller;

import com.userlogin.flightbooking.model.User;
import com.userlogin.flightbooking.responses.UserResponse;
import com.userlogin.flightbooking.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;


import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String allUsers(org.springframework.ui.Model model) {
        List<UserResponse> users = userService.allUsers();
        model.addAttribute("users", users);
        return "users-list";
    }
}


