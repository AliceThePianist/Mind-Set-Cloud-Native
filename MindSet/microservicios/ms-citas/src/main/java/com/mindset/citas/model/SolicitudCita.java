package com.mindset.citas.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "solicitudes_cita")
public class SolicitudCita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long estudianteId;
    @Column(unique = true)
    private Long estudianteActivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoSolicitud estado;
    @Column(nullable = false)
    private Instant desde;
    @Column(nullable = false)
    private Instant hasta;
    @Column(nullable = false)
    private Instant creadaEn;
    
    private Long profesionalId;
    @Column(unique = true)
    private Long horarioId;
    
    private Instant inicio;
    
    private Instant fin;
    public SolicitudCita() {}
    public Long getId() { return id; }
    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }
    public Long getEstudianteActivo() { return estudianteActivo; }
    public void setEstudianteActivo(Long estudianteActivo) { this.estudianteActivo = estudianteActivo; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public Instant getDesde() { return desde; }
    public void setDesde(Instant desde) { this.desde = desde; }
    public Instant getHasta() { return hasta; }
    public void setHasta(Instant hasta) { this.hasta = hasta; }
    public Instant getCreadaEn() { return creadaEn; }
    public void setCreadaEn(Instant creadaEn) { this.creadaEn = creadaEn; }
    public Long getProfesionalId() { return profesionalId; }
    public void setProfesionalId(Long profesionalId) { this.profesionalId = profesionalId; }
    public Long getHorarioId() { return horarioId; }
    public void setHorarioId(Long horarioId) { this.horarioId = horarioId; }
    public Instant getInicio() { return inicio; }
    public void setInicio(Instant inicio) { this.inicio = inicio; }
    public Instant getFin() { return fin; }
    public void setFin(Instant fin) { this.fin = fin; }
}
