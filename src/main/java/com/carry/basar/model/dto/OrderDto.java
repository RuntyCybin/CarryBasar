package com.carry.basar.model.dto;

import java.time.LocalDateTime;

public class OrderDto {
    private String description;
    private Integer volume;
    private LocalDateTime createdAt;

    public OrderDto() {
    }

    public OrderDto(String description, Integer volume, LocalDateTime createdAt) {
        this.description = description;
        this.volume = volume;
        this.createdAt = createdAt;
    }

    public OrderDto(String description, Integer volume) {
        this.description = description;
        this.volume = volume;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
