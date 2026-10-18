
package com.mindset.usuarios.controller;

import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarEstudiante(
            @RequestBody Usuario usuario) {

        Usuario registrado = usuarioService.registrarEstudiante(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(datosUsuario(registrado));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> iniciarSesion(
            @RequestBody Map<String, String> datos) {

        String token = usuarioService.iniciarSesion(
                datos.get("correo"),
                datos.get("contrasena")
        );

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", token);
        respuesta.put("tipo", "Bearer");

        return ResponseEntity.ok(respuesta);
    }


    @GetMapping("/mi-cuenta")
    public Map<String, Object> consultarMiCuenta(Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return datosUsuario(usuario);
    }

    @PutMapping("/mi-cuenta")
    public Map<String, Object> actualizarMiCuenta(
            Authentication autenticacion,
            @RequestBody Usuario datos) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        Usuario actualizado = usuarioService.actualizarUsuario(
                usuario.getId(), datos
        );

        return datosUsuario(actualizado);
    }

    @DeleteMapping("/mi-cuenta")
    public ResponseEntity<Void> eliminarMiCuenta(Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        usuarioService.eliminarUsuario(usuario.getId());

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public List<Map<String, Object>> listarUsuarios() {

        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (Usuario usuario : usuarioService.listarUsuarios()) {
            respuesta.add(datosUsuario(usuario));
        }

        return respuesta;
    }

    @GetMapping("/{id}")
    public Map<String, Object> buscarUsuario(@PathVariable Long id) {

        Usuario usuario = usuarioService.buscarPorId(id);

        return datosUsuario(usuario);
    }

    @PutMapping("/{id}")
    public Map<String, Object> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario datos) {

        Usuario actualizado = usuarioService.actualizarUsuario(id, datos);

        return datosUsuario(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {

        usuarioService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> datosUsuario(Usuario usuario) {

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("id", usuario.getId());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("rol", usuario.getRol());

        return respuesta;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> errorCredenciales(
            BadCredentialsException e) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mensaje("Credenciales incorrectas"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> errorDatos(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest()
                .body(mensaje(e.getMessage()));
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
