package com.mindset.citas.service;
import com.mindset.citas.model.*;
import com.mindset.citas.repository.SolicitudCitaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcesamientoCitaServiceTest {
    private final SolicitudCitaRepository solicitudes = mock(SolicitudCitaRepository.class);
    private final ProfesionalesClient profesionales = mock(ProfesionalesClient.class);
    private final ProcesamientoCitaService servicio = new ProcesamientoCitaService(solicitudes, profesionales);
    private SolicitudCita pendiente() {
        SolicitudCita s = new SolicitudCita();
        s.setEstado(EstadoSolicitud.PENDIENTE); s.setEstudianteActivo(7L);
        s.setDesde(Instant.parse("2030-01-01T10:00:00Z")); s.setHasta(Instant.parse("2030-01-01T12:00:00Z"));
        when(solicitudes.bloquearPorId(1L)).thenReturn(Optional.of(s));
        return s;
    }
    @Test void falloTecnicoNoRechazaLaSolicitud() {
        SolicitudCita s = pendiente();
        when(profesionales.reservar(1L, s.getDesde(), s.getHasta()))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));
        assertThrows(ResponseStatusException.class, () -> servicio.procesar(1L));
        assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
        assertEquals(7L, s.getEstudianteActivo());
    }
    @Test void sinDisponibilidadLiberaLaSolicitudActiva() {
        SolicitudCita s = pendiente();
        when(profesionales.reservar(1L, s.getDesde(), s.getHasta()))
                .thenReturn(new ProfesionalesClient.ReservaResultado(1L, null, null, null, null));
        servicio.procesar(1L);
        assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
        assertNull(s.getEstudianteActivo());
    }
    @Test void repetirUnaConfirmadaNoVuelveAReservar() {
        SolicitudCita s = pendiente(); s.setEstado(EstadoSolicitud.CONFIRMADA);
        assertSame(s, servicio.procesar(1L));
        verifyNoInteractions(profesionales);
    }
    @Test void confirmacionMantieneLaRestriccionDeCitaActiva() {
        SolicitudCita s = pendiente();
        when(profesionales.reservar(1L, s.getDesde(), s.getHasta()))
                .thenReturn(new ProfesionalesClient.ReservaResultado(1L, 12L, 9L, s.getDesde(), s.getHasta()));
        servicio.procesar(1L);
        assertEquals(EstadoSolicitud.CONFIRMADA, s.getEstado());
        assertEquals(12L, s.getHorarioId());
        assertEquals(9L, s.getProfesionalId());
        assertEquals(7L, s.getEstudianteActivo());
    }
}
