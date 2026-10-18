
package com.mindset.usuarios.service;

import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.repository.UsuarioRepository;
import com.mindset.usuarios.security.JwtService;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;
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

        validarDatos(usuario);

        String correo = usuario.getCorreo().trim().toLowerCase();

        if (usuarioRepository.findByCorreo(correo) != null) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        usuario.setCorreo(correo);
        usuario.setContrasena(codificador.encode(usuario.getContrasena()));
        usuario.setRol("ESTUDIANTE");

        return usuarioRepository.save(usuario);
    }

    public String iniciarSesion(String correo, String contrasena) {

        if (correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String correoNormalizado = correo.trim().toLowerCase();

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

        String correo = datos.getCorreo().trim().toLowerCase();
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
    }
}
