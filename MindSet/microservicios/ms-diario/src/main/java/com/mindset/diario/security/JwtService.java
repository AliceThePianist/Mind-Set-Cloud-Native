
package com.mindset.diario.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${mindset.jwt.secret}")
    private String claveSecreta;

    private SecretKey obtenerClave() {
        byte[] clave = Decoders.BASE64.decode(claveSecreta);
        return Keys.hmacShaKeyFor(clave);
    }

    public Claims obtenerDatos(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validarToken(String token) {
        try {
            obtenerDatos(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long obtenerUsuarioId(String token) {
        Claims datos = obtenerDatos(token);

        Number id = datos.get("usuarioId", Number.class);

        if (id == null) {
            return null;
        }

        return id.longValue();
    }

    public String obtenerCorreo(String token) {
        return obtenerDatos(token).getSubject();
    }

    public String obtenerRol(String token) {
        return obtenerDatos(token).get("rol", String.class);
    }
}
