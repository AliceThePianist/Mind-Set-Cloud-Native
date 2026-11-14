package com.mindset.citas.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final SecretKey clave;
    public JwtFilter(@Value("${mindset.jwt.secret}") String secreto) {
        clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            try {
                Claims datos = Jwts.parser().verifyWith(clave).build()
                        .parseSignedClaims(encabezado.substring(7).trim()).getPayload();
                Long id = datos.get("usuarioId", Long.class);
                String rol = datos.get("rol", String.class);
                if (id == null || id <= 0 || rol == null || datos.getExpiration() == null
                        || !List.of("ESTUDIANTE", "PROFESIONAL", "ADMINISTRADOR").contains(rol)) {
                    response.sendError(401); return;
                }
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(id.toString(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                response.sendError(401); return;
            }
        }
        chain.doFilter(request, response);
    }
}
