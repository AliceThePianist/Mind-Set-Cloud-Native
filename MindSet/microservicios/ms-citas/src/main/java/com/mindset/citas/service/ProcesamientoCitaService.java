package com.mindset.citas.service;
import com.mindset.citas.model.*;
import com.mindset.citas.repository.SolicitudCitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class ProcesamientoCitaService {
    private final SolicitudCitaRepository solicitudes;
    private final ProfesionalesClient profesionales;
    public ProcesamientoCitaService(SolicitudCitaRepository solicitudes, ProfesionalesClient profesionales) {
        this.solicitudes = solicitudes; this.profesionales = profesionales;
    }
    @Transactional
    public SolicitudCita procesar(Long id) {
        SolicitudCita solicitud = solicitudes.bloquearPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) return solicitud;
        var reserva = profesionales.reservar(id, solicitud.getDesde(), solicitud.getHasta());
        if (reserva.horarioId() == null) {
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
            solicitud.setEstudianteActivo(null);
        } else {
            solicitud.setProfesionalId(reserva.profesionalId());
            solicitud.setHorarioId(reserva.horarioId());
            solicitud.setInicio(reserva.inicio()); solicitud.setFin(reserva.fin());
            solicitud.setEstado(EstadoSolicitud.CONFIRMADA);
        }
        // La reserva remota es idempotente: si falla este commit, repetir recupera la misma asignación.
        return solicitud;
    }
}
