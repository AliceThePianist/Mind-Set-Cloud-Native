package com.mindset.profesionales.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class UsuariosClient {
    private final RestClient cliente;
    private final String clave;

    public UsuariosClient(@Value("${mindset.usuarios.url:http://localhost:8081}") String url,
                          @Value("${mindset.interno.clave:}") String clave) {
        this.clave = clave;
        var transporte = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var fabrica = new JdkClientHttpRequestFactory(transporte);
        fabrica.setReadTimeout(Duration.ofSeconds(3));
        cliente = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    public boolean esProfesionalVigente(Long id) {
        if (clave.length() < 32) throw noDisponible();
        try {
            Vigencia respuesta = cliente.get().uri("/interno/usuarios/{id}/profesional-vigente", id)
                    .header("X-MindSet-Internal-Key", clave).retrieve().body(Vigencia.class);
            if (respuesta == null || !id.equals(respuesta.usuarioId()) || respuesta.vigente() == null)
                throw noDisponible();
            return respuesta.vigente();
        } catch (RestClientException e) {
            // Una caída de Usuarios no equivale a una cuenta inexistente ni a falta de horarios.
            throw noDisponible();
        }
    }
    private ResponseStatusException noDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible verificar la cuenta profesional");
    }
    public record Vigencia(Long usuarioId, Boolean vigente) {}
}
