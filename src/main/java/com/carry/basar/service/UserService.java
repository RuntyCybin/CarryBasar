package com.carry.basar.service;

import com.carry.basar.model.User;
import com.carry.basar.config.JwtUtil;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.model.repository.UserRolRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRolRepository userRolRepository;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRolRepository userRolRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRolRepository = userRolRepository;
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

    public Mono<User> register(CreateUserRequest createUserRequest) {
        // mapping CreateUserRequest to User
        User user = mapUser(createUserRequest);
        return userRepository.save(user)
                .flatMap(savedUser -> {
                    if (savedUser.getId() == null) {
                        return Mono.error(new RuntimeException("El ID del usuario guardado es null"));
                    }
                    System.out.println("User saved: " + savedUser.getId());

                    // 2. Buscar roles de forma reactiva
                    return Flux.fromIterable(createUserRequest.getRoles())
                            .flatMap(roleName ->
                                    roleRepository.findByName(roleName)
                                            .flatMap(role -> {
                                                if (role == null || role.getRolId() == null) {
                                                    return Mono.error(new RuntimeException("Rol no encontrado: " + roleName));
                                                }
                                                UserRol userRol = new UserRol(null, savedUser.getId(), role.getRolId());
                                                System.out.println("Creando UserRol: " + userRol.getUserId());
                                                return Mono.just(userRol);
                                            })
                            )
                            .doOnNext(userRol -> System.out.println("UserRol listo para ser guardado: " + userRol))
                            .collectList()
                            .flatMapMany(userRolRepository::saveAll)
                            .doOnNext(userRol -> System.out.println("Insertando en usuarios_roles: " + userRol))
                            .collectList()
                            .thenReturn(savedUser);
                })
                .onErrorResume(e -> {
                    System.out.println("Error saving a user: " + e.getMessage());
                    return Mono.error(new RuntimeException("Error saving a user: ", e));
                });
    }

    private User mapUser(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setName(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        return user;
    }
}
