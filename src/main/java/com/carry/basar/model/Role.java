package com.carry.basar.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "roles")
public class Role {
    @Id
    @Column("rol_id")
    private Long rolId;

    @Column("descripcion")
    private String name;

    public Role() {
    }

    public Role(Long rolId, String name) {
        this.rolId = rolId;
        this.name = name;
    }

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
