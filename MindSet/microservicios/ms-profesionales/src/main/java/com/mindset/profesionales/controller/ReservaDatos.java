package com.mindset.profesionales.controller;
import java.time.Instant;
public record ReservaDatos(Long solicitudId, Long horarioId, Long profesionalId, Instant inicio, Instant fin) {
    public record Preferencia(Instant desde, Instant hasta) {}
}
