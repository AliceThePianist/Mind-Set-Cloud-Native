package com.mindset.profesionales.service;
import com.mindset.profesionales.model.*;
import com.mindset.profesionales.repository.*;
import com.mindset.profesionales.controller.HorarioDatos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.*;

@Service
public class HorarioService {
    private final ProfesionalRepository profesionales;
    private final HorarioRepository horarios;
    private final ProfesionalService perfiles;
    public HorarioService(ProfesionalRepository profesionales, HorarioRepository horarios, ProfesionalService perfiles) {
        this.profesionales = profesionales; this.horarios = horarios; this.perfiles = perfiles;
    }
    @Transactional(readOnly = true)
    public List<HorarioDatos> listar(Long usuarioId) {
        return horarios.findByProfesionalUsuarioIdOrderByInicioAsc(usuarioId).stream().map(HorarioDatos::de).toList();
    }
    @Transactional
    public HorarioDatos crear(Long usuarioId, HorarioDatos.Entrada datos) {
        Profesional profesional = bloquearProfesional(usuarioId);
        if (!profesional.isActivo() || !perfiles.completo(profesional))
            throw new IllegalStateException("Completa y activa tu perfil antes de publicar disponibilidad");
        validar(usuarioId, datos, null);
        Horario horario = new Horario();
        horario.setProfesional(profesional); horario.setInicio(datos.inicio()); horario.setFin(datos.fin());
        return HorarioDatos.de(horarios.saveAndFlush(horario));
    }
    @Transactional
    public HorarioDatos modificar(Long usuarioId, Long id, HorarioDatos.Entrada datos) {
        bloquearProfesional(usuarioId);
        Horario horario = propioDisponible(usuarioId, id);
        validar(usuarioId, datos, id);
        horario.setInicio(datos.inicio()); horario.setFin(datos.fin());
        return HorarioDatos.de(horario);
    }
    @Transactional
    public void eliminar(Long usuarioId, Long id) {
        bloquearProfesional(usuarioId);
        horarios.delete(propioDisponible(usuarioId, id));
    }
    private Profesional bloquearProfesional(Long usuarioId) {
        // Todas las escrituras de la agenda y las reservas toman el mismo bloqueo.
        return profesionales.bloquearPorId(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Perfil no encontrado"));
    }
    private Horario propioDisponible(Long usuarioId, Long id) {
        Horario horario = horarios.findById(id).orElseThrow(() -> new NoSuchElementException("Horario no encontrado"));
        if (!horario.getProfesional().getUsuarioId().equals(usuarioId)) throw new AccessDeniedException("Horario ajeno");
        if (horario.getSolicitudId() != null) throw new IllegalStateException("El horario está reservado");
        if (!horario.getInicio().isAfter(Instant.now())) throw new IllegalStateException("El horario ya no es futuro");
        return horario;
    }
    private void validar(Long usuarioId, HorarioDatos.Entrada datos, Long excluir) {
        if (datos == null || datos.inicio() == null || datos.fin() == null
                || !datos.inicio().isAfter(Instant.now()) || !datos.fin().isAfter(datos.inicio()))
            throw new IllegalArgumentException("El horario debe ser futuro y el fin posterior al inicio");
        if (horarios.haySuperposicion(usuarioId, datos.inicio(), datos.fin(), excluir))
            throw new IllegalStateException("El horario se superpone con otro");
    }
}
