package com.mindset.profesionales.service;
import com.mindset.profesionales.model.Profesional;
import com.mindset.profesionales.repository.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ProfesionalService {
    private final ProfesionalRepository profesionales;
    public ProfesionalService(ProfesionalRepository profesionales) { this.profesionales = profesionales; }

    public Profesional consultarPropio(Long usuarioId) {
        return profesionales.findById(usuarioId).orElseThrow(() -> new NoSuchElementException("Perfil no encontrado"));
    }
    public List<Profesional> listarPublicos() {
        return profesionales.findByActivoTrueOrderByNombreAsc().stream().filter(this::completo).toList();
    }
    public Profesional consultarPublico(Long usuarioId) {
        Profesional perfil = consultarPropio(usuarioId);
        if (!perfil.isActivo() || !completo(perfil)) throw new NoSuchElementException("Perfil no encontrado");
        return perfil;
    }
    @Transactional
    public Profesional crear(Long usuarioId, Profesional datos) {
        if (profesionales.existsById(usuarioId)) throw new IllegalStateException("El perfil ya existe");
        Profesional perfil = new Profesional();
        perfil.setUsuarioId(usuarioId);
        copiarDatos(perfil, datos);
        return profesionales.saveAndFlush(perfil);
    }
    @Transactional
    public Profesional actualizar(Long usuarioId, Profesional datos) {
        Profesional perfil = profesionales.bloquearPorId(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Perfil no encontrado"));
        copiarDatos(perfil, datos);
        return perfil;
    }
    public boolean completo(Profesional perfil) {
        return perfil.getNombre() != null && !perfil.getNombre().isBlank()
                && perfil.getEspecialidad() != null && !perfil.getEspecialidad().isBlank()
                && perfil.getDescripcion() != null && !perfil.getDescripcion().isBlank();
    }
    private void copiarDatos(Profesional perfil, Profesional datos) {
        if (datos == null || !completo(datos)) throw new IllegalArgumentException("Nombre, especialidad y descripción son obligatorios");
        if (datos.getNombre().trim().length() > 120 || datos.getEspecialidad().trim().length() > 120
                || datos.getDescripcion().trim().length() > 1000) throw new IllegalArgumentException("Longitud de perfil inválida");
        perfil.setNombre(datos.getNombre().trim());
        perfil.setEspecialidad(datos.getEspecialidad().trim());
        perfil.setDescripcion(datos.getDescripcion().trim());
        perfil.setActivo(datos.isActivo());
    }
}
