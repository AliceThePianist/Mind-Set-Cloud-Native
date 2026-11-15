package com.mindset.profesionales.service;
import com.mindset.profesionales.model.Profesional;
import com.mindset.profesionales.repository.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ProfesionalService {
    private final ProfesionalRepository profesionales;
    private final UsuariosClient usuarios;
    public ProfesionalService(ProfesionalRepository profesionales, UsuariosClient usuarios) {
        this.profesionales = profesionales;
        this.usuarios = usuarios;
    }

    public Profesional consultarPropio(Long usuarioId) {
        return profesionales.findById(usuarioId).orElseThrow(() -> new NoSuchElementException("Perfil no encontrado"));
    }
    public List<Profesional> listarPublicos() {
        return profesionales.findByActivoTrueOrderByNombreAsc().stream().filter(this::publicable).toList();
    }
    public Profesional consultarPublico(Long usuarioId) {
        Profesional perfil = consultarPropio(usuarioId);
        if (!publicable(perfil)) throw new NoSuchElementException("Perfil no encontrado");
        return perfil;
    }
    @Transactional
    public Profesional crear(Long usuarioId, Profesional datos) {
        verificarCuenta(usuarioId);
        if (profesionales.existsById(usuarioId)) throw new IllegalStateException("El perfil ya existe");
        Profesional perfil = new Profesional();
        perfil.setUsuarioId(usuarioId);
        copiarDatos(perfil, datos);
        return profesionales.saveAndFlush(perfil);
    }
    @Transactional
    public Profesional actualizar(Long usuarioId, Profesional datos) {
        verificarCuenta(usuarioId);
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
    public boolean publicable(Profesional perfil) {
        return perfil.isActivo() && completo(perfil) && usuarios.esProfesionalVigente(perfil.getUsuarioId());
    }
    public void verificarCuenta(Long usuarioId) {
        if (!usuarios.esProfesionalVigente(usuarioId))
            throw new org.springframework.security.access.AccessDeniedException("La cuenta profesional ya no está vigente");
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
