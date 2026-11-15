package com.mindset.profesionales.service;
import com.mindset.profesionales.model.*;
import com.mindset.profesionales.repository.*;
import com.mindset.profesionales.controller.ReservaDatos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class ReservaService {
    private final ReservaRepository reservas;
    private final HorarioRepository horarios;
    private final ProfesionalRepository profesionales;
    private final ProfesionalService perfiles;
    public ReservaService(ReservaRepository reservas, HorarioRepository horarios,
                          ProfesionalRepository profesionales, ProfesionalService perfiles) {
        this.reservas = reservas; this.horarios = horarios; this.profesionales = profesionales; this.perfiles = perfiles;
    }
    @Transactional
    public ReservaDatos reservar(Long solicitudId, ReservaDatos.Preferencia datos) {
        if (solicitudId == null || solicitudId <= 0 || datos == null || datos.desde() == null || datos.hasta() == null
                || !datos.hasta().isAfter(datos.desde())) throw new IllegalArgumentException("Preferencias inválidas");
        Reserva anterior = reservas.findById(solicitudId).orElse(null);
        if (anterior != null) {
            if (!anterior.getDesde().equals(datos.desde()) || !anterior.getHasta().equals(datos.hasta()))
                throw new IllegalStateException("La solicitud ya se procesó con otras preferencias");
            return respuesta(anterior);
        }
        Reserva reserva = new Reserva();
        reserva.setSolicitudId(solicitudId); reserva.setDesde(datos.desde()); reserva.setHasta(datos.hasta());
        // La PK impide dos procesamientos concurrentes de la misma solicitud.
        reservas.saveAndFlush(reserva);
        Instant desde = datos.desde().isAfter(Instant.now()) ? datos.desde() : Instant.now();
        for (Long profesionalId : horarios.candidatos(desde, datos.hasta())) {
            Profesional perfil = profesionales.bloquearPorId(profesionalId).orElse(null);
            if (perfil == null || !perfiles.publicable(perfil)) continue;
            // Se consulta después del bloqueo para observar las reservas ya confirmadas.
            var disponibles = horarios.findByProfesionalUsuarioIdAndSolicitudIdIsNullAndInicioGreaterThanEqualAndFinLessThanEqualOrderByInicioAsc(
                    profesionalId, desde, datos.hasta());
            for (Horario horario : disponibles) {
                if (!horario.getInicio().isAfter(Instant.now())) continue;
                horario.setSolicitudId(solicitudId);
                reserva.setHorario(horario);
                return respuesta(reserva);
            }
        }
        // También se conserva el resultado sin disponibilidad: el reintento obtiene la misma respuesta.
        return respuesta(reserva);
    }
    private ReservaDatos respuesta(Reserva reserva) {
        Horario h = reserva.getHorario();
        return h == null ? new ReservaDatos(reserva.getSolicitudId(), null, null, null, null)
                : new ReservaDatos(reserva.getSolicitudId(), h.getId(), h.getProfesional().getUsuarioId(), h.getInicio(), h.getFin());
    }
}
