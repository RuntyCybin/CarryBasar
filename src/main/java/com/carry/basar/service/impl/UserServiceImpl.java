package com.carry.basar.service.impl;

import com.carry.basar.config.JwtUtil;
import com.carry.basar.constants.RoleConstants;
import com.carry.basar.model.Role;
import com.carry.basar.model.User;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.role.RolesListResponse;
import com.carry.basar.model.dto.user.*;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.model.repository.UserRolRepository;
import com.carry.basar.service.EmailService;
import com.carry.basar.service.UserRoleService;
import com.carry.basar.service.UserService;
import com.carry.basar.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserRolRepository userRolRepository;
  private final UserRoleService userRoleService;
  private final Utils utils;
  private final EmailService emailService;

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  public UserServiceImpl(UserRepository userRepository,
                         JwtUtil jwtUtil,
                         PasswordEncoder passwordEncoder,
                         RoleRepository roleRepository,
                         UserRolRepository userRolRepository,
                         Utils utils,
                         UserRoleService userRoleService,
                         EmailService emailService) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.userRolRepository = userRolRepository;
    this.utils = utils;
    this.userRoleService = userRoleService;
    this.emailService = emailService;
  }

  @Override
  public Mono<AuthResponse> authenticate(String username, String password) {
    return userRepository.findByName(username)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Error, user not found")))
            .flatMap(user -> {
              if (!passwordEncoder.matches(password, user.getPassword())) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Error, invalid credentials"));
              }
              return userRolRepository.findByUserId(user.getId())
                      .flatMap(userRol -> roleRepository.findById(userRol.getRoleId()))
                      .map(Role::getName)
                      .collectList()
                      .flatMap(roleNames -> {
                        String tkn = jwtUtil.generateToken(user.getName());
                        AuthResponse response = new AuthResponse(tkn, user.getName(), user.getEmail(), roleNames, user.getId());
                        return Mono.just(response);
                      });
            })
            .doOnError(error -> System.out.println("Error searching for a user: " + error.getMessage()));
  }

  @Override
  public Mono<User> register(CreateUserRequest createUserRequest) {
    if (!this.utils.isVerified(createUserRequest)) {
      return Mono.error(new IllegalArgumentException("Invalid argument found"));
    }

    // mapping CreateUserRequest to User
    User user = this.utils.mapUser(createUserRequest);
    return userRepository.findByEmail(user.getEmail()).flatMap(existingUser -> {
      log.info("User already exists: {}", existingUser.getEmail());
      return Mono.error(new RuntimeException("User already exists: " + existingUser.getEmail()));
    }).switchIfEmpty(Mono.defer(() -> {
      log.info("User does not exist: {}", user.getEmail());
      return assignRolesToUser(user, createUserRequest.roles());
    })).cast(User.class);
  }

  @Override
  public Mono<UpdateUserResponse> update(UpdateUserRequest updateUserRequest) {
    return this.utils.getAuthenticatedUsername()
            .flatMap(username -> userRepository.findByName(username)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No logged in user found")))
                    .flatMap(user -> {
                      log.info("Removing all roles for user: {}", user.getEmail());
                      log.info("new password: {}", updateUserRequest.newPassword());

                      user.setName(updateUserRequest.username());
                      user.setEmail(updateUserRequest.email());
                      user.setPassword(passwordEncoder.encode(updateUserRequest.newPassword()));

                      // Encadenar la eliminación de roles correctamente
                      return userRoleService.removeAllRolesForUser(user.getId())
                              .thenMany(Flux.fromIterable(updateUserRequest.role())
                                      .flatMap(strRole -> roleRepository.findByName(strRole)
                                              .switchIfEmpty(Mono.error(new ResponseStatusException(
                                                      HttpStatus.NOT_FOUND, "Role " + strRole + " not found")))
                                              .map(role -> new UserRol(null, user.getId(), role.getRolId()))
                                      )
                              )
                              .collectList() // recoge todos los roles a asignar
                              .flatMapMany(userRolRepository::saveAll)
                              .then(userRepository.save(user)
                                      .map(savedUser -> new UpdateUserResponse(
                                              savedUser.getName(), savedUser.getEmail(), null)));
                    })
            );
  }

  @Override
  public Mono<Void> removeAllRoles(String username) {
    return userRepository.findByName(username)
            .doOnSubscribe(sub -> System.out.println("Subscribed " + username))
            .doOnNext(usr -> System.out.println("usr on next: " + usr.getEmail()))
            .doOnError(error -> System.out.println("Error: " + error.getMessage()))
            .flatMap(usr ->
                    userRoleService.removeAllRolesForUser(usr.getId()))
            .then();
  }

  @Override
  public Mono<String> removeUser(String username) {
    return userRepository.findByName(username)
            .doOnNext(user -> System.out.println("User found: " + user.getEmail()))
            .doOnError(error -> System.out.println("Error finding a user: " + error.getMessage()))
            .flatMap(user ->
                    userRoleService.removeAllRolesForUser(user.getId())
                            .then(userRepository.delete(user)))
            .thenReturn("User and its roles were deleted successfully");
  }

  @Override
  public Flux<ListUsersResponse> listAllUsers() {
    return userRepository.findAll()
            .map(user -> new ListUsersResponse(user.getEmail(), user.getName()))
            .collectList() // convierte Flux → Mono<List<ListUsersResponse>>
            .flatMapMany(list -> {
              if (list.isEmpty()) {
                return Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found"));
              }
              return Flux.fromIterable(list);
            });
  }

  @Override
  public Flux<RolesListResponse> listAllRolesByUserName(String username) {
    return userRepository.findByName(username)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for username: " + username)))
            .doOnNext(user -> log.info("User id: {}", user.getId()))
            .doOnError(error -> log.error("❌ Error: {}", error.getMessage()))
            .flatMapMany(user -> {
              return userRolRepository.findByUserId(user.getId())
                      .flatMap(userRol -> {
                        return roleRepository.findById(userRol.getRoleId())
                                .map(role -> new RolesListResponse(role.getName()));
                      });
            });
  }

  private Mono<User> assignRolesToUser(User user, Set<String> roles) {
    return userRepository.save(user).flatMap(savedUser -> {
      if (savedUser.getId() == null) {
        return Mono.error(new RuntimeException("El ID del usuario guardado es null"));
      }
      log.info("User saved: {}", savedUser.getId());

      // 2. Buscar roles de forma reactiva
      return Flux.fromIterable(roles)
              .flatMap(roleName -> roleRepository.findByName(roleName).flatMap(role -> {
                if (role == null || role.getRolId() == null) {
                  return Mono.error(new ResponseStatusException(
                          HttpStatus.NOT_FOUND, "El rol " + roleName + " no existe"));
                }
                UserRol userRol = new UserRol(null, savedUser.getId(), role.getRolId());
                log.info("Creando UserRol: {}", userRol.getUserId());
                return Mono.just(userRol);
              })).doOnNext(userRol -> log.info("UserRol listo para ser guardado: {}", userRol))
              .collectList()
              .flatMapMany(userRolRepository::saveAll)
              .doOnNext(userRol -> log.info("Insertando en usuarios_roles: {}", userRol))
              .collectList()
              .thenReturn(savedUser);
    }).onErrorResume(e -> {
      log.error("❌ Error saving a user: {}", e.getMessage());
      return Mono.error(new RuntimeException("Error saving a user: ", e));
    });
  }

  @Override
  public Mono<ChangePasswordResponse> changeUserPassword(ChangePasswordRequest request) {
    // find a user by his email
    return userRepository.findByEmail(request.userEmail())
            .doOnSubscribe(sub -> log.info(
                    "\uD83D\uDD0D Buscando usuario con email: {}", request.userEmail()))
            .doOnNext(user -> log.info("✅ Usuario encontrado: {}", user.getEmail()))
            .doOnError(error -> log.error("❌ Error al buscar usuario: {}", error.getMessage()))
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No user found")))
            .flatMap(foundUser -> {
              foundUser.setPassword(passwordEncoder.encode(request.newPassword()));
              return userRepository.save(foundUser)
                      .flatMap(savedUser -> {
                        log.info("Password for user '{}' (ID: {}) was changed successfully.",
                                savedUser.getName(), savedUser.getId());
                        return emailService.sendAsync(
                                        request.userEmail(),
                                        "Password change",
                                        "Your new password has been modified")
                                .onErrorResume(e -> {
                                  log.error("❌ Error sending password change email: {}", e.getMessage());
                                  return Mono.empty();
                                })
                                .thenReturn(savedUser);
                      })
                      .map(savedUser -> new ChangePasswordResponse(
                              savedUser.getName(),
                              savedUser.getPassword()));
            });
  }
}
