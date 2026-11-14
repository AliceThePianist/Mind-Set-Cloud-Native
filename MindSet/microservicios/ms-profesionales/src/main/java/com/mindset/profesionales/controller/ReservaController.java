package com.mindset.profesionales.controller;
import com.mindset.profesionales.service.ReservaService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/interno/reservas")
public class ReservaController {
    private final ReservaService servicio;
    public ReservaController(ReservaService servicio) { this.servicio = servicio; }
    @PostMapping("/{solicitudId}")
    public ReservaDatos reservar(@PathVariable Long solicitudId, @RequestBody ReservaDatos.Preferencia datos) {
        return servicio.reservar(solicitudId, datos);
    }
}
