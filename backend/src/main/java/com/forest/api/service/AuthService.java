package com.forest.api.service;

import com.forest.api.dto.AuthDtos.AuthResponse;
import com.forest.api.dto.AuthDtos.LoginRequest;
import com.forest.api.dto.AuthDtos.RegisterRequest;
import com.forest.api.model.User;
import com.forest.api.repository.UserRepository;
import com.forest.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) throw new ResponseStatusException(CONFLICT, "Email already in use");
        if (userRepository.existsByUsername(request.username())) throw new ResponseStatusException(CONFLICT, "Username already in use");

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(saved.getId(), saved.getEmail()), saved.getId(), saved.getUsername(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        return new AuthResponse(jwtService.generateToken(user.getId(), user.getEmail()), user.getId(), user.getUsername(), user.getEmail());
    }
}
