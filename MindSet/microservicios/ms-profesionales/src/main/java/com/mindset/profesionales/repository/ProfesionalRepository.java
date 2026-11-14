package com.mindset.profesionales.repository;
import com.mindset.profesionales.model.Profesional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    List<Profesional> findByActivoTrueOrderByNombreAsc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profesional p where p.usuarioId = :id")
    Optional<Profesional> bloquearPorId(@Param("id") Long id);
}
