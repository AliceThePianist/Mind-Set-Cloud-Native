
package com.mindset.diario.controller;

import com.mindset.diario.model.RegistroDiario;
import com.mindset.diario.service.RegistroDiarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody RegistroDiario datos) {

        RegistroDiario registrado = registroDiarioService.registrarEmocion(datos);

        return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
    }

    @GetMapping
    public List<RegistroDiario> listarRegistros() {
        return registroDiarioService.listarRegistros();
    }

    @GetMapping("/{id}")
    public RegistroDiario buscarRegistro(@PathVariable Long id) {
        return registroDiarioService.buscarPorId(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<RegistroDiario> obtenerHistorial(@PathVariable Long usuarioId) {
        return registroDiarioService.obtenerHistorial(usuarioId);
    }

    @PutMapping("/{id}")
    public RegistroDiario actualizarRegistro(
            @PathVariable Long id,
            @RequestBody RegistroDiario datos) {

        return registroDiarioService.actualizarRegistro(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegistro(@PathVariable Long id) {

        registroDiarioService.eliminarRegistro(id);

        return ResponseEntity.noContent().build();
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

    private Map<String, String> mensaje(String texto) {

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", texto);

        return respuesta;
    }
}
