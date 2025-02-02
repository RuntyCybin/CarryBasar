package com.carry.basar.config;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth.getCredentials() != null)
                .flatMap(auth -> {
                    String token = auth.getCredentials().toString();
                    // Validate the JWT token (e.g., using a JWT library)
                    String username = JwtUtil.extractUsername(token);
                    if (username != null && JwtUtil.validateToken(token)) {
                        return Mono.just(new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
                    }
                    return Mono.empty();
                });
    }
}
