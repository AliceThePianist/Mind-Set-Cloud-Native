package com.mindset.profesionales.repository;
import com.mindset.profesionales.model.Horario;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByProfesionalUsuarioIdOrderByInicioAsc(Long usuarioId);
    @Query("select count(h) > 0 from Horario h where h.profesional.usuarioId = :profesionalId "
            + "and h.inicio < :fin and h.fin > :inicio and (:excluir is null or h.id <> :excluir)")
    boolean haySuperposicion(@Param("profesionalId") Long profesionalId, @Param("inicio") Instant inicio,
                            @Param("fin") Instant fin, @Param("excluir") Long excluir);
    @Query("select distinct h.profesional.usuarioId from Horario h where h.profesional.activo = true "
            + "and h.solicitudId is null and h.inicio >= :desde and h.fin <= :hasta order by h.profesional.usuarioId")
    List<Long> candidatos(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
    List<Horario> findByProfesionalUsuarioIdAndSolicitudIdIsNullAndInicioGreaterThanEqualAndFinLessThanEqualOrderByInicioAsc(
            Long usuarioId, Instant desde, Instant hasta);
}
