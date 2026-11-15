
package com.mindset.usuarios.service;

import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.repository.UsuarioRepository;
import com.mindset.usuarios.security.JwtService;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public Usuario registrarEstudiante(Usuario usuario) {
        return registrarUsuario(usuario, "ESTUDIANTE");
    }

    public Usuario registrarProfesional(Usuario usuario) {
        return registrarUsuario(usuario, "PROFESIONAL");
    }

    private Usuario registrarUsuario(Usuario usuario, String rol) {

        validarDatos(usuario);

        String correo = usuario.getCorreo().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.findByCorreo(correo) != null) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        // Nunca persistir la entidad recibida: podría contener un ID enviado por el cliente.
        Usuario nuevo = new Usuario();
        nuevo.setCorreo(correo);
        nuevo.setContrasena(codificador.encode(usuario.getContrasena()));
        nuevo.setRol(rol);
        return usuarioRepository.save(nuevo);
    }

    public String iniciarSesion(String correo, String contrasena) {

        if (correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByCorreo(correoNormalizado);

        if (usuario == null) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (!codificador.matches(contrasena, usuario.getContrasena())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        return jwtService.generarToken(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public boolean esProfesionalVigente(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> "PROFESIONAL".equals(usuario.getRol())).orElse(false);
    }

    public Usuario buscarPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            throw new NoSuchElementException("Usuario no encontrado");
        }

        return usuario;
    }


    public Usuario buscarPorCorreo(String correo) {

        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            throw new NoSuchElementException("Usuario no encontrado");
        }

        return usuario;
    }


    public Usuario actualizarUsuario(Long id, Usuario datos) {

        Usuario usuario = buscarPorId(id);

        validarDatos(datos);

        String correo = datos.getCorreo().trim().toLowerCase(Locale.ROOT);
        Usuario existente = usuarioRepository.findByCorreo(correo);

        if (existente != null && !existente.getId().equals(id)) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        usuario.setCorreo(correo);
        usuario.setContrasena(codificador.encode(datos.getContrasena()));

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {

        Usuario usuario = buscarPorId(id);

        usuarioRepository.delete(usuario);
    }

    private void validarDatos(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        }

        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (!usuario.getCorreo().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || usuario.getCorreo().trim().length() > 255) {
            throw new IllegalArgumentException("El correo no tiene un formato válido");
        }
        if (usuario.getContrasena().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes UTF-8");
        }
    }
}
