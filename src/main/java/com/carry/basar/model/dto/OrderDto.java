package com.carry.basar.model.dto;

import java.time.LocalDateTime;

public record OrderDto(Long orderId, String description, Integer volume, LocalDateTime createdAt,
                        LocalDateTime dueDate, String toLocation, String fromLocation, Double price) {
}
