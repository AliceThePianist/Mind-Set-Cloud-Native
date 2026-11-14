package com.mindset.citas.controller;
import java.time.*;
import java.time.temporal.ChronoUnit;

public record PreferenciaCita(LocalDate fecha, Instant desde, Instant hasta) {
    public PreferenciaCita {
        if (desde != null) desde = desde.truncatedTo(ChronoUnit.MICROS);
        if (hasta != null) hasta = hasta.truncatedTo(ChronoUnit.MICROS);
    }
}
