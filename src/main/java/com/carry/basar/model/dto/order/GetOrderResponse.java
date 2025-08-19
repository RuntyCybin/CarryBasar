package com.carry.basar.model.dto.order;

public class GetOrderResponse {
  private String descripcion;
  private Integer volumen;
  private String fechaCreacion;
  private String nombreUsuario;

  public GetOrderResponse() {
  }

  public GetOrderResponse(String descripcion, Integer volumen, String fechaCreacion, String nombreUsuario) {
    this.descripcion = descripcion;
    this.volumen = volumen;
    this.fechaCreacion = fechaCreacion;
    this.nombreUsuario = nombreUsuario;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Integer getVolumen() {
    return volumen;
  }

  public void setVolumen(Integer volumen) {
    this.volumen = volumen;
  }

  public String getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(String fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public String getNombreUsuario() {
    return nombreUsuario;
  }

  public void setNombreUsuario(String nombreUsuario) {
    this.nombreUsuario = nombreUsuario;
  }

  @Override
  public String toString() {
    return "GetOrderResponse{" +
            "descripcion='" + descripcion + '\'' +
            ", volumen=" + volumen +
            ", fechaCreacion='" + fechaCreacion + '\'' +
            ", nombreUsuario='" + nombreUsuario + '\'' +
            '}';
  }
}
