package com.mindset.citas.controller;
import com.mindset.citas.model.SolicitudCita;
import com.mindset.citas.service.ProcesamientoCitaService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno/solicitudes")
@ConditionalOnProperty(name = "mindset.procesamiento-local.habilitado", havingValue = "true")
public class ProcesamientoLocalController {
    private final ProcesamientoCitaService servicio;
    public ProcesamientoLocalController(ProcesamientoCitaService servicio) { this.servicio = servicio; }
    // Adaptador provisional síncrono. No publica eventos ni envía notificaciones.
    @PostMapping("/{id}/procesar")
    public SolicitudCita procesar(@PathVariable Long id) { return servicio.procesar(id); }
}
