
package com.mindset.diario.service;

import com.mindset.diario.model.RegistroDiario;
import com.mindset.diario.repository.RegistroDiarioRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class RegistroDiarioService {

    private final RegistroDiarioRepository registroDiarioRepository;

    public RegistroDiarioService(RegistroDiarioRepository registroDiarioRepository) {
        this.registroDiarioRepository = registroDiarioRepository;
    }

    public RegistroDiario registrarEmocion(Long usuarioId, RegistroDiario datos) {

        validarUsuarioId(usuarioId);
        validarDatos(datos);

        LocalDate fechaActual = LocalDate.now();

        if (registroDiarioRepository.existsByUsuarioIdAndFecha(usuarioId, fechaActual)) {
            throw new IllegalStateException(
                    "El estudiante ya tiene un registro emocional para hoy"
            );
        }

        RegistroDiario registro = new RegistroDiario();
        registro.setUsuarioId(usuarioId);
        registro.setFecha(fechaActual);
        registro.setEstadoEmocional(datos.getEstadoEmocional());
        registro.setComentario(datos.getComentario());

        return registroDiarioRepository.save(registro);
    }

    public List<RegistroDiario> listarRegistros(Long usuarioId) {
        validarUsuarioId(usuarioId);
        return registroDiarioRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public RegistroDiario buscarPorId(Long id, Long usuarioId) {

        validarUsuarioId(usuarioId);

        RegistroDiario registro = registroDiarioRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Registro emocional no encontrado")
                );

        if (!registro.getUsuarioId().equals(usuarioId)) {
            throw new AccessDeniedException(
                    "No tienes permiso para acceder a este registro"
            );
        }

        return registro;
    }

    public List<RegistroDiario> obtenerHistorial(Long usuarioId) {
        return listarRegistros(usuarioId);
    }

    public RegistroDiario actualizarRegistro(
            Long id, Long usuarioId, RegistroDiario datos) {

        RegistroDiario registro = buscarPorId(id, usuarioId);

        validarDatos(datos);

        registro.setEstadoEmocional(datos.getEstadoEmocional());
        registro.setComentario(datos.getComentario());

        return registroDiarioRepository.save(registro);
    }

    public void eliminarRegistro(Long id, Long usuarioId) {

        RegistroDiario registro = buscarPorId(id, usuarioId);
        registroDiarioRepository.delete(registro);
    }

    public Map<String, Object> obtenerResumenSieteDias(Long usuarioId) {

        validarUsuarioId(usuarioId);

        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(6);

        List<RegistroDiario> historial =
                registroDiarioRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);

        int totalRegistros = 0;
        int sumaEmociones = 0;

        for (RegistroDiario registro : historial) {

            if (!registro.getFecha().isBefore(desde)
                    && !registro.getFecha().isAfter(hoy)) {

                totalRegistros++;
                sumaEmociones += registro.getEstadoEmocional();
            }
        }

        double promedio = 0.0;

        if (totalRegistros > 0) {
            promedio = (double) sumaEmociones / totalRegistros;
        }

        Map<String, Object> resumen = new HashMap<>();

        resumen.put("usuarioId", usuarioId);
        resumen.put("fechaInicio", desde);
        resumen.put("fechaFin", hoy);
        resumen.put("totalRegistros", totalRegistros);
        resumen.put("promedioEmocional", promedio);

        return resumen;
    }

    private void validarUsuarioId(Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del estudiante es obligatorio"
            );
        }
    }

    private void validarDatos(RegistroDiario datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del registro son obligatorios"
            );
        }

        if (datos.getEstadoEmocional() == null
                || datos.getEstadoEmocional() < 1
                || datos.getEstadoEmocional() > 5) {

            throw new IllegalArgumentException(
                    "El estado emocional debe estar entre 1 y 5"
            );
        }

        if (datos.getComentario() != null
                && datos.getComentario().length() > 500) {

            throw new IllegalArgumentException(
                    "El comentario no puede superar los 500 caracteres"
            );
        }
    }
}
