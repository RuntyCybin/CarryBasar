package com.carry.basar.model.dto.accepted_order;

import java.time.LocalDateTime;

public record AcceptOrderRequest(Long orderId, Long userId, LocalDateTime shipAt, LocalDateTime shipTo,
                                  String description, Integer volumen) {
}