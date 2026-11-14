package com.mindset.profesionales.service;
import com.mindset.profesionales.model.Reserva;
import com.mindset.profesionales.repository.*;
import com.mindset.profesionales.controller.ReservaDatos;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaServiceTest {
    private final ReservaRepository reservas = mock(ReservaRepository.class);
    private final HorarioRepository horarios = mock(HorarioRepository.class);
    private final ProfesionalRepository profesionales = mock(ProfesionalRepository.class);
    private final ProfesionalService perfiles = mock(ProfesionalService.class);
    private final ReservaService servicio = new ReservaService(reservas, horarios, profesionales, perfiles);
    @Test void resultadoSinDisponibilidadTambienEsIdempotente() {
        var datos = new ReservaDatos.Preferencia(Instant.parse("2030-01-01T10:00:00Z"), Instant.parse("2030-01-01T12:00:00Z"));
        Reserva reserva = new Reserva(); reserva.setSolicitudId(1L);
        reserva.setDesde(datos.desde()); reserva.setHasta(datos.hasta());
        when(reservas.findById(1L)).thenReturn(Optional.of(reserva));
        assertNull(servicio.reservar(1L, datos).horarioId());
        verifyNoInteractions(horarios, profesionales, perfiles);
    }
    @Test void mismaClaveConOtrasPreferenciasEsConflicto() {
        var datos = new ReservaDatos.Preferencia(Instant.parse("2030-01-01T10:00:00Z"), Instant.parse("2030-01-01T12:00:00Z"));
        Reserva reserva = new Reserva(); reserva.setSolicitudId(1L);
        reserva.setDesde(datos.desde().minusSeconds(3600)); reserva.setHasta(datos.hasta());
        when(reservas.findById(1L)).thenReturn(Optional.of(reserva));
        assertThrows(IllegalStateException.class, () -> servicio.reservar(1L, datos));
        verifyNoInteractions(horarios, profesionales);
    }
}
