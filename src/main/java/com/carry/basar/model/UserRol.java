package com.carry.basar.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("usuarios_roles")
public class UserRol {

    @Id
    private Long id;

    @Column("usuario")
    private Long userId;

    @Column("rol")
    private Long roleId;

    public UserRol() {
    }

    public UserRol(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRol(Long id, Long userId, Long roleId) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

}
