package com.userlogin.flightbooking.service;

import com.userlogin.flightbooking.dto.OtpVerifyDto;
import com.userlogin.flightbooking.dto.RegisterDto;

import com.userlogin.flightbooking.model.User;

import com.userlogin.flightbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.userlogin.flightbooking.responses.UserResponse;
import java.util.List;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    /*public List<User> allUsers(){
    List<User> users = new ArrayList<>();
    userRepository.findAll().forEach(users::add);
    System.out.println("Fetched users count: "+users.size());
    return users;
    }*/
    public List<UserResponse> allUsers() {
       /* List<UserResponse> responseList = new ArrayList<>();

        userRepository.findAll().forEach(user -> {
            responseList.add(new UserResponse(user.getUsername(), user.getEmail(), user.isEnabled()));
        });
        return responseList;

        */
        List<User> userList = userRepository.findAll();
        List<UserResponse> responses = userList.stream()
                .map(user -> new UserResponse(user.getUsername(), user.getEmail(), user.isEnabled()))
                .collect(Collectors.toList());
        return responses;

    }


}




