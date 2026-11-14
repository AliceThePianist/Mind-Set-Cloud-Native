package com.mindset.citas.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.http.HttpClient;
import java.time.*;

@Component
public class ProfesionalesClient {
    private final RestClient cliente;
    private final String clave;
    public ProfesionalesClient(@Value("${mindset.profesionales.url:http://localhost:8083}") String url,
                               @Value("${mindset.interno.clave:}") String clave) {
        this.clave = clave;
        var transporte = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var fabrica = new JdkClientHttpRequestFactory(transporte);
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        cliente = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }
    public ReservaResultado reservar(Long solicitudId, Instant desde, Instant hasta) {
        if (clave.length() < 32) throw noDisponible();
        try {
            ReservaResultado resultado = cliente.post().uri("/interno/reservas/{id}", solicitudId)
                    .header("X-MindSet-Internal-Key", clave).contentType(MediaType.APPLICATION_JSON)
                    .body(new Preferencia(desde, hasta)).retrieve().body(ReservaResultado.class);
            if (resultado == null || !solicitudId.equals(resultado.solicitudId())) throw noDisponible();
            boolean vacio = resultado.horarioId() == null && resultado.profesionalId() == null
                    && resultado.inicio() == null && resultado.fin() == null;
            if (!vacio && (resultado.horarioId() == null || resultado.horarioId() <= 0
                    || resultado.profesionalId() == null || resultado.profesionalId() <= 0
                    || resultado.inicio() == null || resultado.fin() == null
                    || resultado.inicio().isBefore(desde) || resultado.fin().isAfter(hasta)
                    || !resultado.fin().isAfter(resultado.inicio()))) throw noDisponible();
            return resultado;
        } catch (RestClientException e) {
            // Un fallo técnico no significa falta de disponibilidad: conservar PENDIENTE.
            throw noDisponible();
        }
    }
    private ResponseStatusException noDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible verificar la reserva; reintenta la misma solicitud");
    }
    public record Preferencia(Instant desde, Instant hasta) {}
    public record ReservaResultado(Long solicitudId, Long horarioId, Long profesionalId, Instant inicio, Instant fin) {}
}
