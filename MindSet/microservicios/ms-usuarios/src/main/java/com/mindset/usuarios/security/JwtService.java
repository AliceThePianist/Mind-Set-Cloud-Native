
package com.mindset.usuarios.security;

import com.mindset.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${mindset.jwt.secret}")
    private String claveSecreta;

    private SecretKey obtenerClave() {
        byte[] clave = Decoders.BASE64.decode(claveSecreta);
        return Keys.hmacShaKeyFor(clave);
    }

    public String generarToken(Usuario usuario) {

        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(
                fechaActual.getTime() + 3600000
        );

        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol())
                .claim("usuarioId", usuario.getId())
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(obtenerClave(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims obtenerDatos(String token) {

        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String obtenerCorreo(String token) {
        return obtenerDatos(token).getSubject();
    }

    public boolean validarToken(String token) {

        try {
            obtenerDatos(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
