
package com.mindset.diario.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {

            String token = encabezado.substring(7);

            try {
                Claims datos = jwtService.obtenerDatos(token);
                Long usuarioId = datos.get("usuarioId", Long.class);
                String rol = datos.get("rol", String.class);
                if (usuarioId == null || usuarioId <= 0 || rol == null || datos.getExpiration() == null
                        || !List.of("ESTUDIANTE", "PROFESIONAL", "ADMINISTRADOR").contains(rol)) {
                    response.sendError(401);
                    return;
                }

                if (usuarioId != null && rol != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    usuarioId.toString(),
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                            );

                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                response.sendError(401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
