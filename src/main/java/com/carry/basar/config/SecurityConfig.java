package com.carry.basar.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private final JwtUtil jwtUtil;

  public SecurityConfig(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .exceptionHandling(exceptionHandlingSpec ->
                    exceptionHandlingSpec.authenticationEntryPoint(
                            new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
                    ))
            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/", "/public/**").permitAll()
                    .anyExchange().authenticated()
            )
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // desactiva login con navegador
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // desactiva formulario de login HTML
            .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
  }

  private AuthenticationWebFilter jwtAuthenticationFilter() {
    AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager());
    jwtFilter.setServerAuthenticationConverter(jwtConverter());
    jwtFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());
    return jwtFilter;
  }

  private ReactiveAuthenticationManager jwtAuthenticationManager() {
    return authentication -> Mono.just(authentication);
  }

  private ServerAuthenticationConverter jwtConverter() {
    return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
            .filter(authHeader -> authHeader.startsWith("Bearer "))
            .map(authHeader -> authHeader.substring(7))
            .map(token -> {
              Claims claims = jwtUtil.parseToken(token);
              return new UsernamePasswordAuthenticationToken(
                      claims.getSubject(),
                      null,
                      Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            });
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
  }
}
