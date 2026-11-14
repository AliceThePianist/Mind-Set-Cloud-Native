package com.mindset.profesionales.model;
import jakarta.persistence.*;

@Entity
@Table(name = "profesionales")
public class Profesional {
    @Id
    private Long usuarioId;
    @Version
    private Long version;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, length = 120)
    private String especialidad;
    @Column(nullable = false, length = 1000)
    private String descripcion;
    @Column(nullable = false)
    private boolean activo;
    public Profesional() {}
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
