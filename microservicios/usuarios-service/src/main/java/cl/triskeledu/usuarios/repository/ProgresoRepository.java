package cl.triskeledu.usuarios.repository;

import cl.triskeledu.usuarios.model.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgresoRepository extends JpaRepository<Progreso, Long> {
    boolean existsByUsuarioIdAndCapsulaId(Long usuarioId, Long capsulaId);
    List<Progreso> findByUsuarioId(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}
