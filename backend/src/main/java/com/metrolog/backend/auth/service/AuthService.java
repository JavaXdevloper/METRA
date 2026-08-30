package com.metrolog.backend.auth.service;

import com.metrolog.backend.auth.model.AuthRequest;
import com.metrolog.backend.auth.model.AuthResponse;
import com.metrolog.backend.auth.model.User;
import com.metrolog.backend.auth.repository.UserRepository;
import com.metrolog.backend.config.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        String userRole = (request.getRole() != null && !request.getRole().isEmpty())
                ? request.getRole()
                : "OFFICER";
        user.setRole(userRole);

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            String token = jwtUtils.generateToken(request.getUsername());
            return new AuthResponse(token);
        }

        throw new RuntimeException("Invalid username or password");
    }
}