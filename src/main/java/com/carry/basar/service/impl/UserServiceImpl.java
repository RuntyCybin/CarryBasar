package com.carry.basar.service.impl;

import com.carry.basar.config.JwtUtil;
import com.carry.basar.model.User;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserResponse;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.model.repository.UserRolRepository;
import com.carry.basar.service.UserRoleService;
import com.carry.basar.service.UserService;
import com.carry.basar.utils.Utils;
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


  public UserServiceImpl(UserRepository userRepository,
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

  @Override
  public Mono<String> authenticate(String username, String password) {
    return userRepository.findByName(username)
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .flatMap(user -> Mono.just(jwtUtil.generateToken(user.getName()))).onErrorResume(e -> {
              System.out.println("Error searching for a user: " + e.getMessage());
              return Mono.error(new RuntimeException("Error searching for a user: ", e));
            });
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
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .doOnSubscribe(subscription -> System.out.println("Subscribed to find a user: " + username))
            .doOnNext(user -> System.out.println("User found: " + user.getEmail()))
            .doOnError(error -> System.out.println("Error finding a user: " + error.getMessage()))
            .flatMap(user -> userRoleService.removeAllRolesForUser(user.getId())
                    .then(userRepository.delete(user)))
            .thenReturn("User and its roles were deleted successfully");
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
}
