package com.mindset.profesionales.controller;
import com.mindset.profesionales.service.HorarioService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profesionales/mis-horarios")
public class HorarioController {
    private final HorarioService servicio;
    public HorarioController(HorarioService servicio) { this.servicio = servicio; }
    @GetMapping
    public List<HorarioDatos> listar(Authentication auth) { return servicio.listar(Long.valueOf(auth.getName())); }
    @PostMapping
    public ResponseEntity<HorarioDatos> crear(Authentication auth, @RequestBody HorarioDatos.Entrada datos) {
        return ResponseEntity.status(201).body(servicio.crear(Long.valueOf(auth.getName()), datos));
    }
    @PutMapping("/{id}")
    public HorarioDatos modificar(Authentication auth, @PathVariable Long id, @RequestBody HorarioDatos.Entrada datos) {
        return servicio.modificar(Long.valueOf(auth.getName()), id, datos);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(Authentication auth, @PathVariable Long id) {
        servicio.eliminar(Long.valueOf(auth.getName()), id);
        return ResponseEntity.noContent().build();
    }
}
