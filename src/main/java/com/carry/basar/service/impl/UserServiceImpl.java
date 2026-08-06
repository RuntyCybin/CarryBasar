package com.carry.basar.service.impl;

import com.carry.basar.config.JwtUtil;
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

  // Añade un logger a tu clase
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
      System.out.println("User already exists: " + existingUser.getEmail());
      return Mono.error(new RuntimeException("User already exists: " + existingUser.getEmail()));
    }).switchIfEmpty(Mono.defer(() -> {
      System.out.println("User does not exist: " + user.getEmail());
      return assignRolesToUser(user, createUserRequest.getRoles());
    })).cast(User.class);
  }

  @Override
  public Mono<UpdateUserResponse> update(UpdateUserRequest updateUserRequest) {
    return this.utils.getAuthenticatedUsername()
            .flatMap(username -> userRepository.findByName(username)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No logged in user found")))
                    .flatMap(user -> {
                      System.out.println("Removing all roles for user: " + user.getEmail());

                      user.setName(updateUserRequest.getUsername());
                      user.setEmail(updateUserRequest.getEmail());
                      user.setPassword(user.getPassword());

                      // Encadenar la eliminación de roles correctamente
                      return userRoleService.removeAllRolesForUser(user.getId())
                              .thenMany(Flux.fromIterable(updateUserRequest.getRole())
                                      .flatMap(strRole -> roleRepository.findByName(strRole)
                                              .switchIfEmpty(Mono.error(new ResponseStatusException(
                                                      HttpStatus.NOT_FOUND, "Role " + strRole + " not found")))
                                              .map(role -> new UserRol(null, user.getId(), role.getRolId()))
                                      )
                              )
                              .collectList() // recoge todos los roles a asignar
                              .flatMapMany(userRolRepository::saveAll)
                              .then(userRepository.save(user)
                                      .map(savedUser -> {
                                        UpdateUserResponse response = new UpdateUserResponse();
                                        response.setUsername(savedUser.getName());
                                        response.setEmail(savedUser.getEmail());
                                        return response;
                                      }));
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
  public Mono<String> changePwd(ChangePwdNotLoggedUserRequest request) {
    // find a user by his email
    return userRepository.findByEmail(request.getTo())
            .doOnSubscribe(sub -> System.out.println("🔍 Buscando usuario con email: " + request.getTo()))
            .doOnNext(user -> System.out.println("✅ Usuario encontrado: " + user.getEmail()))
            .doOnError(error -> System.out.println("❌ Error al buscar usuario: " + error.getMessage()))
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found")))
            .flatMap(foundUser -> {
              foundUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
              return userRepository.save(foundUser)
                      .doOnSuccess(savedUser -> {
                        System.out.println("🔒 Contraseña actualizada para: " + savedUser.getEmail());
                        emailService.sendAsync(request.getTo(), request.getSubject(), request.getNewPassword());
                      })
                      .then(Mono.just("Password changed successfully"));
            });
  }

  @Override
  public Flux<RolesListResponse> listAllRolesByUserName(String username) {
    return userRepository.findByName(username)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for username: " + username)))
            .doOnNext(user -> System.out.println("User id: " + user.getId()))
            .doOnError(error -> System.out.println("Error: " + error.getMessage()))
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
      System.out.println("User saved: " + savedUser.getId());

      // 2. Buscar roles de forma reactiva
      return Flux.fromIterable(roles)
              .flatMap(roleName -> roleRepository.findByName(roleName).flatMap(role -> {
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

  @Override
  public Mono<RecoverPwdResponse> changeUserPassword(RecoverPwdRequest request) {
    return userRepository.findByName(request.getUsername())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Error, user not found")))
            .flatMap(user -> {
              // Es más eficiente modificar el objeto existente que crear uno nuevo
              user.setPassword(passwordEncoder.encode(request.getPassword()));
              return userRepository.save(user)
                      .doOnSuccess(savedUser -> {
                        log.info("Password for user '{}' (ID: {}) was updated successfully.", savedUser.getName(), savedUser.getId());
                      })
                      .map(savedUser -> new RecoverPwdResponse(savedUser.getName(), savedUser.getPassword()));
            });
  }

  @Override
  public Flux<RolesListResponse> listPublicRoles() {
    return this.roleRepository.findAll()
            .filter(role -> !role.getName().equals("ADMIN"))
            .map(role -> new RolesListResponse(role.getName()));
  }
}
