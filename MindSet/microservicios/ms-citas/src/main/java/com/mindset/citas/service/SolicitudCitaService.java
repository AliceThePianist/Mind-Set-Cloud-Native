package com.mindset.citas.service;
import com.mindset.citas.model.*;
import com.mindset.citas.repository.SolicitudCitaRepository;
import com.mindset.citas.controller.PreferenciaCita;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.time.*;
import java.util.*;

@Service
public class SolicitudCitaService {
    private final SolicitudCitaRepository solicitudes;
    public SolicitudCitaService(SolicitudCitaRepository solicitudes) { this.solicitudes = solicitudes; }
    @Transactional
    public SolicitudCita crear(Long estudianteId, PreferenciaCita datos) {
        if (datos == null) throw new IllegalArgumentException("Indica una fecha o un rango horario");
        Instant desde = datos.desde();
        Instant hasta = datos.hasta();
        if (datos.fecha() != null) {
            if (desde != null || hasta != null) throw new IllegalArgumentException("Usa fecha o rango, no ambos");
            ZoneId zona = ZoneId.of("America/Santiago");
            desde = datos.fecha().atStartOfDay(zona).toInstant();
            hasta = datos.fecha().plusDays(1).atStartOfDay(zona).toInstant();
            if (datos.fecha().isBefore(LocalDate.now(zona))) throw new IllegalArgumentException("La fecha no puede estar en el pasado");
        } else if (desde == null || hasta == null || !desde.isAfter(Instant.now())) {
            throw new IllegalArgumentException("El rango debe ser futuro");
        }
        if (!hasta.isAfter(desde) || !hasta.isAfter(Instant.now()))
            throw new IllegalArgumentException("El fin debe ser posterior al inicio y al momento actual");
        if (solicitudes.existsByEstudianteActivo(estudianteId)) throw new IllegalStateException("Ya tienes una solicitud activa");
        SolicitudCita solicitud = new SolicitudCita();
        solicitud.setEstudianteId(estudianteId);
        // Columna única nullable: garantiza una sola PENDIENTE/CONFIRMADA incluso con peticiones simultáneas.
        solicitud.setEstudianteActivo(estudianteId);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setDesde(desde); solicitud.setHasta(hasta); solicitud.setCreadaEn(Instant.now());
        return solicitudes.saveAndFlush(solicitud);
    }
    public List<SolicitudCita> historial(Long estudianteId) {
        return solicitudes.findByEstudianteIdOrderByCreadaEnDesc(estudianteId);
    }
    public SolicitudCita consultar(Long estudianteId, Long id) {
        SolicitudCita solicitud = solicitudes.findById(id).orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (!solicitud.getEstudianteId().equals(estudianteId)) throw new AccessDeniedException("Solicitud ajena");
        return solicitud;
    }
    public List<SolicitudCita> asignadas(Long profesionalId) {
        return solicitudes.findByProfesionalIdOrderByInicioAsc(profesionalId);
    }
    @Transactional
    public SolicitudCita completar(Long profesionalId, Long id) {
        SolicitudCita solicitud = solicitudes.bloquearPorId(id).orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (!profesionalId.equals(solicitud.getProfesionalId())) throw new AccessDeniedException("Cita no asignada a este profesional");
        if (solicitud.getEstado() != EstadoSolicitud.CONFIRMADA) throw new IllegalStateException("Solo una cita confirmada puede completarse");
        if (solicitud.getFin() == null || solicitud.getFin().isAfter(Instant.now()))
            throw new IllegalStateException("La cita todavía no ha finalizado");
        solicitud.setEstado(EstadoSolicitud.COMPLETADA);
        solicitud.setEstudianteActivo(null);
        return solicitud;
    }
}
