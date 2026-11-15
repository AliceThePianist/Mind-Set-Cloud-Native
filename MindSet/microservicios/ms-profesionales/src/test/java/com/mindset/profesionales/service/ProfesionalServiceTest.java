package com.mindset.profesionales.service;
import com.mindset.profesionales.model.*;
import com.mindset.profesionales.repository.ProfesionalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfesionalServiceTest {
    private final ProfesionalRepository repositorio = mock(ProfesionalRepository.class);
    private final UsuariosClient usuarios = mock(UsuariosClient.class);
    private final ProfesionalService servicio = new ProfesionalService(repositorio, usuarios);
    private Profesional perfil() {
        Profesional p = new Profesional(); p.setUsuarioId(9L); p.setActivo(true);
        p.setNombre("Profesional"); p.setEspecialidad("Bienestar"); p.setDescripcion("Apoyo estudiantil");
        return p;
    }
    @Test void cuentaEliminadaNoSePublica() {
        when(repositorio.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(perfil()));
        when(usuarios.esProfesionalVigente(9L)).thenReturn(false);
        assertTrue(servicio.listarPublicos().isEmpty());
    }
    @Test void caidaDeUsuariosNoSeConfundeConListaVacia() {
        when(repositorio.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(perfil()));
        when(usuarios.esProfesionalVigente(9L)).thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));
        assertThrows(ResponseStatusException.class, servicio::listarPublicos);
    }
    @Test void tokenAntiguoNoPermiteRecrearPerfilDeCuentaEliminada() {
        assertThrows(AccessDeniedException.class, () -> servicio.crear(9L, perfil()));
        verifyNoInteractions(repositorio);
    }
    @Test void perfilInactivoNoConsultaLaCuenta() {
        Profesional p = perfil(); p.setActivo(false);
        assertFalse(servicio.publicable(p));
        verifyNoInteractions(usuarios);
    }
    @Test void relacionDeHorariosNoSeSerializaNiSeAceptaDesdeJson() {
        var mapper = JsonMapper.builder().build();
        var json = mapper.readTree(mapper.writeValueAsString(perfil()));
        assertFalse(json.has("horarios"));
        Profesional entrada = mapper.readValue("{\"nombre\":\"Prueba\",\"horarios\":[{\"id\":99}]}", Profesional.class);
        assertTrue(entrada.getHorarios().isEmpty());
    }
}
