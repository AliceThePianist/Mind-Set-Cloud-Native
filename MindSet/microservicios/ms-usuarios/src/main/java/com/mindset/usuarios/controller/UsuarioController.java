
package com.mindset.usuarios.controller;

import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarEstudiante(@RequestBody Usuario usuario) {

        try {
            Usuario registrado = usuarioService.registrarEstudiante(usuario);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", registrado.getId());
            respuesta.put("correo", registrado.getCorreo());
            respuesta.put("rol", registrado.getRol());

            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);

        } catch (IllegalArgumentException e) {

            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", e.getMessage());

            return ResponseEntity.badRequest().body(error);

        } catch (IllegalStateException e) {

            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }
}
