package com.mindset.profesionales.service;
import com.mindset.profesionales.model.*;
import com.mindset.profesionales.repository.*;
import com.mindset.profesionales.controller.HorarioDatos;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HorarioServiceTest {
    private final ProfesionalRepository profesionales = mock(ProfesionalRepository.class);
    private final HorarioRepository horarios = mock(HorarioRepository.class);
    private final ProfesionalService perfiles = mock(ProfesionalService.class);
    private final HorarioService servicio = new HorarioService(profesionales, horarios, perfiles);
    private Horario horario(Long propietario) {
        Profesional p = new Profesional(); p.setUsuarioId(propietario); p.setActivo(true);
        Horario h = new Horario(); h.setProfesional(p);
        h.setInicio(Instant.now().plusSeconds(3600)); h.setFin(h.getInicio().plusSeconds(3600));
        when(profesionales.bloquearPorId(9L)).thenReturn(Optional.of(p));
        when(horarios.findById(1L)).thenReturn(Optional.of(h));
        return h;
    }
    @Test void reservaImpideModificarYEliminar() {
        Horario h = horario(9L); h.setSolicitudId(25L);
        assertThrows(IllegalStateException.class, () -> servicio.modificar(9L, 1L, new HorarioDatos.Entrada(h.getInicio(), h.getFin())));
        assertThrows(IllegalStateException.class, () -> servicio.eliminar(9L, 1L));
        verify(horarios, never()).delete(any());
    }
    @Test void horarioAjenoNoPuedeEliminarse() {
        horario(8L);
        assertThrows(AccessDeniedException.class, () -> servicio.eliminar(9L, 1L));
        verify(horarios, never()).delete(any());
    }
    @Test void superposicionRechazaLaModificacionSinAlterarFechas() {
        Horario h = horario(9L);
        Instant original = h.getInicio();
        var entrada = new HorarioDatos.Entrada(original.plusSeconds(60), h.getFin().plusSeconds(60));
        when(horarios.haySuperposicion(9L, entrada.inicio(), entrada.fin(), 1L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> servicio.modificar(9L, 1L, entrada));
        assertEquals(original, h.getInicio());
    }
}
