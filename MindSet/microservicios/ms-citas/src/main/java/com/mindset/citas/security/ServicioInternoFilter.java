package com.mindset.citas.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class ServicioInternoFilter extends OncePerRequestFilter {
    private final String clave;
    public ServicioInternoFilter(@Value("${mindset.interno.clave:}") String clave) { this.clave = clave; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getServletPath().startsWith("/interno/")) {
            String recibida = request.getHeader("X-MindSet-Internal-Key");
            if (clave.length() < 32 || recibida == null || !MessageDigest.isEqual(
                    clave.getBytes(StandardCharsets.UTF_8), recibida.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(401); return;
            }
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "servicio-interno", null, List.of(new SimpleGrantedAuthority("ROLE_SISTEMA"))));
        }
        chain.doFilter(request, response);
    }
}
