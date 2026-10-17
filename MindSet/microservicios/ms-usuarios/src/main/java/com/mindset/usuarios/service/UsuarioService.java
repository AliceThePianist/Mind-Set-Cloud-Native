
package com.mindset.usuarios.service;

import com.mindset.usuarios.model.Usuario;
import com.mindset.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrarEstudiante(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        }

        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        String correo = usuario.getCorreo().trim().toLowerCase();

        if (usuarioRepository.findByCorreo(correo) != null) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        usuario.setCorreo(correo);
        usuario.setContrasena(codificador.encode(usuario.getContrasena()));
        usuario.setRol("ESTUDIANTE");

        return usuarioRepository.save(usuario);
    }
}
