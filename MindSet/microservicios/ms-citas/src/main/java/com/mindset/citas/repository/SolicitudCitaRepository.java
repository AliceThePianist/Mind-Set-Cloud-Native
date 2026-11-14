package com.mindset.citas.repository;
import com.mindset.citas.model.SolicitudCita;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface SolicitudCitaRepository extends JpaRepository<SolicitudCita, Long> {
    boolean existsByEstudianteActivo(Long estudianteId);
    List<SolicitudCita> findByEstudianteIdOrderByCreadaEnDesc(Long estudianteId);
    List<SolicitudCita> findByProfesionalIdOrderByInicioAsc(Long profesionalId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitudCita s where s.id = :id")
    Optional<SolicitudCita> bloquearPorId(@Param("id") Long id);
}
