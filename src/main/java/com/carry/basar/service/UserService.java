package com.carry.basar.service;

import com.carry.basar.model.User;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.role.RolesListResponse;
import com.carry.basar.model.dto.user.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface UserService {

  Mono<AuthResponse> authenticate(String username, String password);

  Mono<User> register(CreateUserRequest createUserRequest);

  Mono<UpdateUserResponse> update(UpdateUserRequest updateUserRequest);

  Mono<Void> removeAllRoles(String username);

  Mono<String> removeUser(String username);

  Flux<ListUsersResponse> listAllUsers();

  Mono<String> changePwd(ChangePwdNotLoggedUserRequest request);

  Flux<RolesListResponse> listAllRolesByUserName(String username);

  Mono<RecoverPwdResponse> changeUserPassword(RecoverPwdRequest request);

  Flux<RolesListResponse> listPublicRoles();

}
