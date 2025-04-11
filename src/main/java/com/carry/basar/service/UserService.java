package com.carry.basar.service;

import com.carry.basar.model.User;
import com.carry.basar.config.JwtUtil;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserResponse;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.model.repository.UserRolRepository;

import java.util.Set;

import com.carry.basar.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserRolRepository userRolRepository;
  private final UserRoleService userRoleService;
  private final Utils utils;


  public UserService(UserRepository userRepository,
                     JwtUtil jwtUtil,
                     PasswordEncoder passwordEncoder,
                     RoleRepository roleRepository,
                     UserRolRepository userRolRepository,
                     Utils utils,
                     UserRoleService userRoleService) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.userRolRepository = userRolRepository;
    this.utils = utils;
    this.userRoleService = userRoleService;
  }

  public Mono<String> authenticate(String username, String password) {
    return userRepository.findByName(username)
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .flatMap(user -> Mono.just(jwtUtil.generateToken(user.getName()))).onErrorResume(e -> {
              System.out.println("Error searching for a user: " + e.getMessage());
              return Mono.error(new RuntimeException("Error searching for a user: ", e));
            });
  }

  public Mono<User> register(CreateUserRequest createUserRequest) {
    if (!this.utils.isVerified(createUserRequest)) {
      return Mono.error(new IllegalArgumentException("Invalid argument found"));
    }

    // mapping CreateUserRequest to User
    User user = mapUser(createUserRequest);
    return userRepository.findByEmail(user.getEmail()).flatMap(existingUser -> {
      System.out.println("User already exists: " + existingUser.getEmail());
      return Mono.error(new RuntimeException("User already exists: " + existingUser.getEmail()));
    }).switchIfEmpty(Mono.defer(() -> {
      System.out.println("User does not exist: " + user.getEmail());
      return assignRolesToUser(user, createUserRequest.getRoles());
    })).cast(User.class);
  }

  private Mono<User> assignRolesToUser(User user, Set<String> roles) {
    return userRepository.save(user).flatMap(savedUser -> {
      if (savedUser.getId() == null) {
        return Mono.error(new RuntimeException("El ID del usuario guardado es null"));
      }
      System.out.println("User saved: " + savedUser.getId());

      // 2. Buscar roles de forma reactiva
      return Flux.fromIterable(roles).flatMap(roleName -> roleRepository.findByName(roleName).flatMap(role -> {
                if (role == null || role.getRolId() == null) {
                  return Mono.error(new ResponseStatusException(
                          HttpStatus.NOT_FOUND, "El rol " + roleName + " no existe"));
                }
                UserRol userRol = new UserRol(null, savedUser.getId(), role.getRolId());
                System.out.println("Creando UserRol: " + userRol.getUserId());
                return Mono.just(userRol);
              })).doOnNext(userRol -> System.out.println(
                      "UserRol listo para ser guardado: " + userRol))
              .collectList()
              .flatMapMany(userRolRepository::saveAll)
              .doOnNext(userRol -> System.out.println(
                      "Insertando en usuarios_roles: " + userRol))
              .collectList()
              .thenReturn(savedUser);
    }).onErrorResume(e -> {
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

  public Mono<UpdateUserResponse> update(UpdateUserRequest updateUserRequest) {
    return this.utils.getAuthenticatedUsername().flatMap(username -> userRepository.findByName(username)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No logged in user found")))
            .doOnNext(user -> {
              System.out.println("Removing all roles for user: " + user.getEmail());
              userRoleService.removeAllRolesForUser(user.getId());
            })
            .flatMap(user -> {
              Flux<UserRol> fluxRole = Flux.fromIterable(updateUserRequest.getRole())
                      .flatMap(strRole -> roleRepository.findByName(strRole)
                              .switchIfEmpty(Mono.error(new ResponseStatusException(
                                      HttpStatus.NOT_FOUND, "Role " + strRole + " not found")))
                              .map(userRole -> new UserRol(null, user.getId(), userRole.getRolId()))
                      );
              user.setName(updateUserRequest.getUsername());
              user.setEmail(updateUserRequest.getEmail());
              user.setPassword(user.getPassword());

              return userRolRepository.saveAll(fluxRole)
                      .then(userRepository.save(user)
                              .flatMap(savedUser -> {
                                UpdateUserResponse response = new UpdateUserResponse();
                                response.setUsername(savedUser.getName());
                                response.setEmail(savedUser.getEmail());
                                return Mono.just(response);
                              }));
            })
    );
  }


  public void removeAllRoles(String username) {
    userRepository.findByName(username)
            .doOnSubscribe(sub -> System.out.println("Subscribed " + username))
            .doOnNext(usr -> System.out.println("usr on next: " + usr.getEmail()))
            .doOnError(error -> System.out.println("Error: " + error.getMessage()))
            .subscribe(usr -> {
              userRoleService.removeAllRolesForUser(usr.getId());
            });
  }

}
