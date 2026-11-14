package com.mindset.profesionales.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;

@RestControllerAdvice
public class ErrorControllerAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> datos(IllegalArgumentException e) { return error(400, e.getMessage()); }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> conflicto(IllegalStateException e) { return error(409, e.getMessage()); }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> ausente(NoSuchElementException e) { return error(404, e.getMessage()); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> acceso(AccessDeniedException e) { return error(403, e.getMessage()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> duplicado() { return error(409, "La operación entra en conflicto con un registro existente"); }
    private ResponseEntity<?> error(int estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
