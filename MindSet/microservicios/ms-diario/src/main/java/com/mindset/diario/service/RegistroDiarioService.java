
package com.mindset.diario.service;

import com.mindset.diario.model.RegistroDiario;
import com.mindset.diario.repository.RegistroDiarioRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RegistroDiarioService {

    private final RegistroDiarioRepository registroDiarioRepository;

    public RegistroDiarioService(RegistroDiarioRepository registroDiarioRepository) {
        this.registroDiarioRepository = registroDiarioRepository;
    }

    public RegistroDiario registrarEmocion(RegistroDiario datos) {

        validarDatos(datos);

        if (datos.getUsuarioId() == null || datos.getUsuarioId() <= 0) {
            throw new IllegalArgumentException("El ID del estudiante es obligatorio");
        }

        LocalDate fechaActual = LocalDate.now();

        if (registroDiarioRepository.existsByUsuarioIdAndFecha(
                datos.getUsuarioId(), fechaActual)) {

            throw new IllegalStateException(
                    "El estudiante ya tiene un registro emocional para hoy"
            );
        }

        RegistroDiario registro = new RegistroDiario();

        registro.setUsuarioId(datos.getUsuarioId());
        registro.setFecha(fechaActual);
        registro.setEstadoEmocional(datos.getEstadoEmocional());
        registro.setComentario(datos.getComentario());

        return registroDiarioRepository.save(registro);
    }

    public List<RegistroDiario> listarRegistros() {
        return registroDiarioRepository.findAll();
    }

    public RegistroDiario buscarPorId(Long id) {

        RegistroDiario registro = registroDiarioRepository.findById(id).orElse(null);

        if (registro == null) {
            throw new NoSuchElementException("Registro emocional no encontrado");
        }

        return registro;
    }

    public List<RegistroDiario> obtenerHistorial(Long usuarioId) {

        return registroDiarioRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public RegistroDiario actualizarRegistro(Long id, RegistroDiario datos) {

        RegistroDiario registro = buscarPorId(id);

        validarDatos(datos);

        registro.setEstadoEmocional(datos.getEstadoEmocional());
        registro.setComentario(datos.getComentario());

        return registroDiarioRepository.save(registro);
    }

    public void eliminarRegistro(Long id) {

        RegistroDiario registro = buscarPorId(id);

        registroDiarioRepository.delete(registro);
    }

    private void validarDatos(RegistroDiario datos) {

        if (datos == null) {
            throw new IllegalArgumentException("Los datos del registro son obligatorios");
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
