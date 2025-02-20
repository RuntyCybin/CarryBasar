package com.carry.basar.service;

import com.carry.basar.model.User;
import com.carry.basar.config.JwtUtil;
import com.carry.basar.model.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<String> authenticate(String username, String password) {
        return userRepository.findByName(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .flatMap(user -> Mono.just(jwtUtil.generateToken(user.getName())))
                .onErrorResume(e -> {

                    System.out.println("Error searching for a user: " + e.getMessage());
                    return Mono.error(new RuntimeException("Error searching for a user: ", e));

                });
    }

    public Mono<User> register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
