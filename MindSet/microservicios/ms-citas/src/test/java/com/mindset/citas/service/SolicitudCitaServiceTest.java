package com.mindset.citas.service;
import com.mindset.citas.model.*;
import com.mindset.citas.repository.SolicitudCitaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudCitaServiceTest {
    private final SolicitudCitaRepository repositorio = mock(SolicitudCitaRepository.class);
    private final SolicitudCitaService servicio = new SolicitudCitaService(repositorio);
    private SolicitudCita cita(EstadoSolicitud estado) {
        SolicitudCita s = new SolicitudCita(); s.setProfesionalId(9L); s.setEstudianteActivo(7L);
        s.setEstado(estado); s.setFin(Instant.now().minusSeconds(60));
        when(repositorio.bloquearPorId(1L)).thenReturn(Optional.of(s));
        return s;
    }
    @Test void completarLiberaLaSolicitudActiva() {
        SolicitudCita s = cita(EstadoSolicitud.CONFIRMADA);
        servicio.completar(9L, 1L);
        assertEquals(EstadoSolicitud.COMPLETADA, s.getEstado());
        assertNull(s.getEstudianteActivo());
    }
    @Test void otroProfesionalNoCompletaLaCita() {
        SolicitudCita s = cita(EstadoSolicitud.CONFIRMADA);
        assertThrows(AccessDeniedException.class, () -> servicio.completar(8L, 1L));
        assertEquals(EstadoSolicitud.CONFIRMADA, s.getEstado());
    }
    @Test void rechazadaNoPuedeCompletarse() {
        cita(EstadoSolicitud.RECHAZADA);
        assertThrows(IllegalStateException.class, () -> servicio.completar(9L, 1L));
    }
    @Test void citaFuturaNoPuedeCompletarse() {
        SolicitudCita s = cita(EstadoSolicitud.CONFIRMADA); s.setFin(Instant.now().plusSeconds(3600));
        assertThrows(IllegalStateException.class, () -> servicio.completar(9L, 1L));
    }
}
