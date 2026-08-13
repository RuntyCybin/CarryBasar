package com.carry.basar.model.dto.order;

public record GetOrderResponse(String descripcion, Integer volumen, String fechaCreacion, String nombreUsuario) {
}
