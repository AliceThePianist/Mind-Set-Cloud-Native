package com.mindset.profesionales.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "horarios")
public class Horario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;
    @Column(nullable = false)
    private Instant inicio;
    @Column(nullable = false)
    private Instant fin;
    @Column(unique = true)
    private Long solicitudId;
    public Horario() {}
    public Long getId() { return id; }
    public Profesional getProfesional() { return profesional; }
    public void setProfesional(Profesional profesional) { this.profesional = profesional; }
    public Instant getInicio() { return inicio; }
    public void setInicio(Instant inicio) { this.inicio = inicio; }
    public Instant getFin() { return fin; }
    public void setFin(Instant fin) { this.fin = fin; }
    public Long getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Long solicitudId) { this.solicitudId = solicitudId; }
}
