package com.mindset.citas.controller;
import com.mindset.citas.model.SolicitudCita;
import com.mindset.citas.service.SolicitudCitaService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class SolicitudCitaController {
    private final SolicitudCitaService servicio;
    public SolicitudCitaController(SolicitudCitaService servicio) { this.servicio = servicio; }
    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudCita> crear(Authentication auth, @RequestBody PreferenciaCita datos) {
        return ResponseEntity.status(201).body(servicio.crear(Long.valueOf(auth.getName()), datos));
    }
    @GetMapping("/solicitudes")
    public List<SolicitudCita> historial(Authentication auth) { return servicio.historial(Long.valueOf(auth.getName())); }
    @GetMapping("/solicitudes/{id}")
    public SolicitudCita consultar(Authentication auth, @PathVariable Long id) {
        return servicio.consultar(Long.valueOf(auth.getName()), id);
    }
    @GetMapping("/asignadas")
    public List<SolicitudCita> asignadas(Authentication auth) { return servicio.asignadas(Long.valueOf(auth.getName())); }
    @PutMapping("/asignadas/{id}/completar")
    public SolicitudCita completar(Authentication auth, @PathVariable Long id) {
        return servicio.completar(Long.valueOf(auth.getName()), id);
    }
}
