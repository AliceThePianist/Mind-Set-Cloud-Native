
package com.mindset.diario.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "registros_diario",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"usuario_id", "fecha"}
        )
)
public class RegistroDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Integer estadoEmocional;

    @Column(length = 500)
    private String comentario;

    public RegistroDiario() {
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getEstadoEmocional() {
        return estadoEmocional;
    }

    public void setEstadoEmocional(Integer estadoEmocional) {
        this.estadoEmocional = estadoEmocional;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
