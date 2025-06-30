package com.userlogin.flightbooking.responses;




public class UserResponse {
    private String username;
    private String email;
    private boolean enabled;

    public UserResponse(String username, String email, boolean enabled) {
        this.username = username;
        this.email = email;
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
