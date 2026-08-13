package com.carry.basar.model.dto.accepted_order;

import java.time.LocalDateTime;

public record AcceptedOrderResponse(Long orderId, String orderDesc, Long userId, LocalDateTime createdAt,
                                     LocalDateTime shippedAt, Integer vol) {
}