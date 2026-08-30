package com.metrolog.backend.auth.model;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private String role; // Optional for login, defaults to OFFICER on registration
}