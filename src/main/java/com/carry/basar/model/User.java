package com.carry.basar.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;

    public User() {
    }

    public User(Long id, String username, String email, String password) {
        this.id = id;
        this.name = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}