package com.userlogin.flightbooking.service;


import com.userlogin.flightbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.userlogin.flightbooking.dto.RegisterDto;
import com.userlogin.flightbooking.dto.OtpVerifyDto;
import com.userlogin.flightbooking.dto.LoginDto;
import com.userlogin.flightbooking.model.User;
import com.userlogin.flightbooking.responses.UserResponse;


import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import java.util.Random;
import jakarta.mail.MessagingException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }
    public UserResponse register(RegisterDto input){
        if (!input.getPassword().equals(input.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }
        
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
       User savedUser= userRepository.save(user);
        return new UserResponse(savedUser.getUsername(), savedUser.getEmail(), savedUser.isEnabled());




    }
    public User authenticate(LoginDto input){
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Email"));

        if(!user.isEnabled()){
            throw new RuntimeException("Account is not verified. Please verify your account.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );
        return user;
    }

    public void verifyUser(OtpVerifyDto input){
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code has expired.");

            }
            if(user.getVerificationCode().equals(input.getVerificationCode())){
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            }else{
                throw new RuntimeException("Invalid verification code.");
            }
        }else{
            throw new RuntimeException("User not found.");
        }
    }
    public void resendVerificationCode(String email){
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(user.isEnabled()){
                throw new RuntimeException("Account is already verified.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        }else{
            throw new RuntimeException("User not found.");
        }
    }
    public void sendVerificationEmail(User user) {
        String subject = "Verify your account";
        String verificationCode = user.getVerificationCode();
        String recipient = user.getEmail();

        System.out.println("Sending verification code to: " + recipient);
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color:  #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color:#333;\">Welcome to SkyFly!</h2>"
                + "<p style=\"font-size:16px;\">Dear " + user.getUsername()+ "</p>"
                + "<p style=\"font-size:16px;\">Thank you for registering with SkyFly. To complete your registration, please use the following verification code:</p>"
                + "<div style=\"background-color: #fff; padding:20px;border-radius:50px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color:#333;\">Verification Code:</h3>"
                + "<p style=\"font-size:18px;font-weight:bold;color:#007bff;\">" + verificationCode + "</p>"
                +"<p style=\"font-size:16px;\">This OTP will expire in 15 minutes.</p>"
                +"<p style=\"font-size:16px;\">Regards,</p>"
                +"<p style=\"font-size:16px;\">SkyFly Team</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(recipient, subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();

        }
    }
    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    }




