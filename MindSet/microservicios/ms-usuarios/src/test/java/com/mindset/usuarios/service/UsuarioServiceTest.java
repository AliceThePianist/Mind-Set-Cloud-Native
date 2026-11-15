package com.mindset.usuarios.service;
import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.repository.UsuarioRepository;
import com.mindset.usuarios.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository repositorio = mock(UsuarioRepository.class);
    private final UsuarioService servicio = new UsuarioService(repositorio, mock(JwtService.class));
    private Usuario entrada() {
        Usuario datos = new Usuario();
        ReflectionTestUtils.setField(datos, "id", 123L);
        datos.setCorreo("  Prueba@EXAMPLE.ORG  ");
        datos.setContrasena("clave-solo-prueba");
        datos.setRol("ADMINISTRADOR");
        when(repositorio.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        return datos;
    }
    @Test void registroPublicoIgnoraIdYRolDelCliente() {
        Usuario datos = entrada();
        Usuario creado = servicio.registrarEstudiante(datos);
        assertNotSame(datos, creado);
        assertNull(creado.getId());
        assertEquals("ESTUDIANTE", creado.getRol());
        assertEquals("prueba@example.org", creado.getCorreo());
        assertTrue(new BCryptPasswordEncoder().matches(datos.getContrasena(), creado.getContrasena()));
    }
    @Test void altaProfesionalFuerzaRolYEntidadNueva() {
        Usuario creado = servicio.registrarProfesional(entrada());
        assertNull(creado.getId());
        assertEquals("PROFESIONAL", creado.getRol());
    }
}
