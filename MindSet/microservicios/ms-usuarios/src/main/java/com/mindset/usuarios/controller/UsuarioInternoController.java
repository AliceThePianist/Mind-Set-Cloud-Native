package com.mindset.usuarios.controller;

import com.mindset.usuarios.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno/usuarios")
public class UsuarioInternoController {
    private final UsuarioService usuarios;
    public UsuarioInternoController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @GetMapping("/{id}/profesional-vigente")
    public Vigencia consultar(@PathVariable Long id) {
        return new Vigencia(id, usuarios.esProfesionalVigente(id));
    }
    public record Vigencia(Long usuarioId, boolean vigente) {}
}
