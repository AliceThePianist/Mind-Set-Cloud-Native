package com.mindset.profesionales.controller;
import com.mindset.profesionales.model.Horario;
import java.time.Instant;

public record HorarioDatos(Long id, Long profesionalId, Instant inicio, Instant fin, boolean reservado) {
    public static HorarioDatos de(Horario horario) {
        return new HorarioDatos(horario.getId(), horario.getProfesional().getUsuarioId(),
                horario.getInicio(), horario.getFin(), horario.getSolicitudId() != null);
    }
    public record Entrada(Instant inicio, Instant fin) {
        public Entrada {
            if (inicio != null) inicio = inicio.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
            if (fin != null) fin = fin.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        }
    }
}
