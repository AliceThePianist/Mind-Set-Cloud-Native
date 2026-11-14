package com.mindset.profesionales.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    private Long solicitudId;
    @Version
    private Long version;
    @Column(nullable = false)
    private Instant desde;
    @Column(nullable = false)
    private Instant hasta;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", unique = true)
    private Horario horario;
    public Reserva() {}
    public Long getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Long solicitudId) { this.solicitudId = solicitudId; }
    public Instant getDesde() { return desde; }
    public void setDesde(Instant desde) { this.desde = desde; }
    public Instant getHasta() { return hasta; }
    public void setHasta(Instant hasta) { this.hasta = hasta; }
    public Horario getHorario() { return horario; }
    public void setHorario(Horario horario) { this.horario = horario; }
}
