package com.mindset.citas.security;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ServicioInternoFilterTest {
    private final String clave = UUID.randomUUID().toString();
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }
    private MockHttpServletRequest peticion(String ruta, String recibida) {
        var r = new MockHttpServletRequest(); r.setServletPath(ruta);
        if (recibida != null) r.addHeader("X-MindSet-Internal-Key", recibida);
        return r;
    }
    @Test void rutaInternaSinClaveRechaza() throws Exception {
        var response = new MockHttpServletResponse();
        new ServicioInternoFilter(clave).doFilter(peticion("/interno/recurso", null), response, new MockFilterChain());
        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    @Test void claveValidaAutenticaSoloAlSistema() throws Exception {
        new ServicioInternoFilter(clave).doFilter(peticion("/interno/recurso", clave),
                new MockHttpServletResponse(), new MockFilterChain());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("ROLE_SISTEMA", auth.getAuthorities().iterator().next().getAuthority());
    }
    @Test void claveEnRutaPublicaNoConcedeIdentidad() throws Exception {
        new ServicioInternoFilter(clave).doFilter(peticion("/api/recurso", clave),
                new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    @Test void configuracionVaciaNoHabilitaContratoInterno() throws Exception {
        var response = new MockHttpServletResponse();
        new ServicioInternoFilter("").doFilter(peticion("/interno/recurso", ""), response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }
}
