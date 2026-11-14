package com.mindset.usuarios.security;
import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }
    @Test void unCorreoReutilizadoNoTransfiereLaIdentidadDelToken() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        Claims datos = mock(Claims.class);
        when(jwt.obtenerDatos("token")).thenReturn(datos);
        when(datos.get("usuarioId", Long.class)).thenReturn(10L);
        when(datos.getSubject()).thenReturn("anterior@example.org");
        when(datos.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60000));
        Usuario propietario = new Usuario();
        propietario.setCorreo("nuevo@example.org"); propietario.setRol("ESTUDIANTE");
        when(usuarios.findById(10L)).thenReturn(Optional.of(propietario));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        new JwtFilter(jwt, usuarios).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(usuarios, never()).findByCorreo(anyString());
        verify(jwt, times(1)).obtenerDatos("token");
    }
    @Test void tokenSinIdentificadorSeRechaza() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(jwt.obtenerDatos("token")).thenReturn(mock(Claims.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new JwtFilter(jwt, usuarios).doFilter(request, response, new MockFilterChain());
        assertEquals(401, response.getStatus());
        verifyNoInteractions(usuarios);
    }
}
