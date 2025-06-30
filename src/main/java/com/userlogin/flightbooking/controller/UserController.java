package com.userlogin.flightbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import com.userlogin.flightbooking.service.UserService;
import com.userlogin.flightbooking.model.User;
import com.userlogin.flightbooking.responses.UserResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;




@RequestMapping("/users")
@Controller
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }
    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<User> authenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }
    /*@GetMapping("/")
    public ResponseEntity<List<User>> allUsers(){
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }*/
    /*@GetMapping("/")

    public ResponseEntity<List<UserResponse>> allUsers() {
        List<UserResponse> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }*/


}
