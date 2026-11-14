package com.mindset.profesionales.controller;
import com.mindset.profesionales.model.Profesional;
import com.mindset.profesionales.service.ProfesionalService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {
    private final ProfesionalService servicio;
    public ProfesionalController(ProfesionalService servicio) { this.servicio = servicio; }
    @GetMapping
    public List<Profesional> listar() { return servicio.listarPublicos(); }
    @GetMapping("/{usuarioId}")
    public Profesional consultar(@PathVariable Long usuarioId) { return servicio.consultarPublico(usuarioId); }
    @GetMapping("/mi-perfil")
    public Profesional propio(Authentication auth) { return servicio.consultarPropio(Long.valueOf(auth.getName())); }
    @PostMapping("/mi-perfil")
    public ResponseEntity<Profesional> crear(Authentication auth, @RequestBody Profesional datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(Long.valueOf(auth.getName()), datos));
    }
    @PutMapping("/mi-perfil")
    public Profesional actualizar(Authentication auth, @RequestBody Profesional datos) {
        return servicio.actualizar(Long.valueOf(auth.getName()), datos);
    }
}
