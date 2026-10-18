
package com.mindset.diario.repository;

import com.mindset.diario.model.RegistroDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegistroDiarioRepository extends JpaRepository<RegistroDiario, Long> {

    boolean existsByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    List<RegistroDiario> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
