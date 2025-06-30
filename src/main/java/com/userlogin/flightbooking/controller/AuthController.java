package com.userlogin.flightbooking.controller;

import com.userlogin.flightbooking.service.JwtService;
import com.userlogin.flightbooking.dto.LoginDto;
import com.userlogin.flightbooking.dto.OtpVerifyDto;
import com.userlogin.flightbooking.dto.RegisterDto;
import com.userlogin.flightbooking.service.AuthService;
import com.userlogin.flightbooking.service.UserService;
import com.userlogin.flightbooking.responses.UserResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.userlogin.flightbooking.model.User;
import com.userlogin.flightbooking.responses.LoginResponse;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {
   private final JwtService jwtService;
   private final AuthService authService;

   public AuthController(JwtService jwtService, AuthService authService) {
       this.jwtService = jwtService;
       this.authService = authService;
   }

   @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword){
       
       if (!isValidEmail(email)) {
           return "redirect:/users/register?error=Invalid email domain. Only gmail.com and yahoo.com are allowed.";
       }
       
       if (!isValidPassword(password)) {
           return "redirect:/users/register?error=Password must be at least 8 characters with at least one number and one special character.";
       }
       
       try {
           RegisterDto registerDto = new RegisterDto();
           registerDto.setUsername(username);
           registerDto.setEmail(email);
           registerDto.setPassword(password);
           registerDto.setConfirmPassword(confirmPassword);
           
           UserResponse registeredUser = authService.register(registerDto);
           return "redirect:/users/verify?email=" + email;
       } catch (RuntimeException e) {
           return "redirect:/users/register?error=" + e.getMessage();
       }
   }
   
   private boolean isValidEmail(String email) {
       return email != null && 
              (email.endsWith("@gmail.com") || email.endsWith("@yahoo.com"));
   }
   
   private boolean isValidPassword(String password) {
       if (password == null || password.length() < 8) {
           return false;
       }
       
       boolean hasNumber = password.matches(".*\\d.*");
       boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?].*");
       
       return hasNumber && hasSpecialChar;
   }

   @PostMapping("/login")
   @ResponseBody
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginDto loginDto){
       User authenticatedUser = authService.authenticate(loginDto);

       String jwtToken = jwtService.generateToken(authenticatedUser, authenticatedUser.getRole());

       LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
       return ResponseEntity.ok(loginResponse);
   }
   @PostMapping("/verify")
    public String verifyUser(
            @RequestParam String email,
            @RequestParam String verificationCode){
       try{
           OtpVerifyDto otpVerifyDto = new OtpVerifyDto();
           otpVerifyDto.setEmail(email);
           otpVerifyDto.setVerificationCode(verificationCode);
           
           authService.verifyUser(otpVerifyDto);
           return "success";
       }catch (RuntimeException e){
           return "error";
       }
   }

   @PostMapping("/resend")
   @ResponseBody
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){

       try{
           authService.resendVerificationCode(email);
           return ResponseEntity.ok("Verification code resent successfully");
       }catch (RuntimeException e){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
       }
   }
   
   @PostMapping("/login-form")
   public String loginForm(@RequestParam String email, @RequestParam String password, HttpServletRequest request) {
       try {
           LoginDto loginDto = new LoginDto();
           loginDto.setEmail(email);
           loginDto.setPassword(password);
           
           User authenticatedUser = authService.authenticate(loginDto);
           
           // Set up Spring Security context
           UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
               authenticatedUser, null, authenticatedUser.getAuthorities());
           SecurityContextHolder.getContext().setAuthentication(authToken);
           
           // Save to session
           HttpSession session = request.getSession(true);
           session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
           
           if ("ADMIN".equals(authenticatedUser.getRole())) {
               return "redirect:/admin/dashboard";
           } else {
               return "redirect:/users/dashboard";
           }
       } catch (RuntimeException e) {
           return "redirect:/users/login?error=Invalid email or password";
       }
   }
}


