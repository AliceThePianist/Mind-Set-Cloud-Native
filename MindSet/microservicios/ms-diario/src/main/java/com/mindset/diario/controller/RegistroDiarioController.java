
package com.mindset.diario.controller;

import com.mindset.diario.model.RegistroDiario;
import com.mindset.diario.service.RegistroDiarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/diario/registros")
public class RegistroDiarioController {

    private final RegistroDiarioService registroDiarioService;

    public RegistroDiarioController(RegistroDiarioService registroDiarioService) {
        this.registroDiarioService = registroDiarioService;
    }

    @PostMapping
    public ResponseEntity<RegistroDiario> registrarEmocion(
            Authentication autenticacion,
            @RequestBody RegistroDiario datos) {

        Long usuarioId = obtenerUsuarioId(autenticacion);

        RegistroDiario registrado =
                registroDiarioService.registrarEmocion(usuarioId, datos);

        return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
    }

    @GetMapping
    public List<RegistroDiario> listarRegistros(Authentication autenticacion) {

        Long usuarioId = obtenerUsuarioId(autenticacion);

        return registroDiarioService.listarRegistros(usuarioId);
    }

    @GetMapping("/resumen-7-dias")
    public Map<String, Object> obtenerResumen(Authentication autenticacion) {

        Long usuarioId = obtenerUsuarioId(autenticacion);

        return registroDiarioService.obtenerResumenSieteDias(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<RegistroDiario> obtenerHistorial(
            @PathVariable Long usuarioId,
            Authentication autenticacion) {

        Long usuarioAutenticado = obtenerUsuarioId(autenticacion);

        if (!usuarioId.equals(usuarioAutenticado)) {
            throw new AccessDeniedException(
                    "No tienes permiso para consultar este historial"
            );
        }

        return registroDiarioService.obtenerHistorial(usuarioAutenticado);
    }

    @GetMapping("/{id}")
    public RegistroDiario buscarRegistro(
            @PathVariable Long id,
            Authentication autenticacion) {

        Long usuarioId = obtenerUsuarioId(autenticacion);

        return registroDiarioService.buscarPorId(id, usuarioId);
    }

    @PutMapping("/{id}")
    public RegistroDiario actualizarRegistro(
            @PathVariable Long id,
            Authentication autenticacion,
            @RequestBody RegistroDiario datos) {

        Long usuarioId = obtenerUsuarioId(autenticacion);

        return registroDiarioService.actualizarRegistro(id, usuarioId, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegistro(
            @PathVariable Long id,
            Authentication autenticacion) {

        Long usuarioId = obtenerUsuarioId(autenticacion);

        registroDiarioService.eliminarRegistro(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    private Long obtenerUsuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> errorDatos(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest().body(mensaje(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> errorDuplicado(
            IllegalStateException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mensaje(e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> errorNoEncontrado(
            NoSuchElementException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mensaje(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> errorAcceso(
            AccessDeniedException e) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mensaje(e.getMessage()));
    }

    private Map<String, String> mensaje(String texto) {

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", texto);

        return respuesta;
    }
}
