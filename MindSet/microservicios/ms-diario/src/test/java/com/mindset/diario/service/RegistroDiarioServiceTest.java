package com.mindset.diario.service;
import com.mindset.diario.model.RegistroDiario;
import com.mindset.diario.repository.RegistroDiarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistroDiarioServiceTest {
    private final RegistroDiarioRepository repositorio = mock(RegistroDiarioRepository.class);
    private final RegistroDiarioService servicio = new RegistroDiarioService(repositorio);
    @Test void registroAjenoNoSeConsulta() {
        RegistroDiario r = new RegistroDiario(); r.setUsuarioId(8L);
        when(repositorio.findById(1L)).thenReturn(Optional.of(r));
        assertThrows(AccessDeniedException.class, () -> servicio.buscarPorId(1L, 9L));
    }
    @Test void resumenExcluyeDiasFueraDelIntervalo() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Santiago"));
        when(repositorio.findByUsuarioIdOrderByFechaDesc(9L)).thenReturn(List.of(
                registro(hoy, 5), registro(hoy.minusDays(6), 1),
                registro(hoy.minusDays(7), 5), registro(hoy.plusDays(1), 5)));
        Map<String, Object> resumen = servicio.obtenerResumenSieteDias(9L);
        assertEquals(2, resumen.get("totalRegistros"));
        assertEquals(3.0, resumen.get("promedioEmocional"));
        assertNotNull(resumen.get("aviso"));
    }
    private RegistroDiario registro(LocalDate fecha, int emocion) {
        RegistroDiario r = new RegistroDiario(); r.setFecha(fecha); r.setEstadoEmocional(emocion); return r;
    }
}
