package cl.triskeledu.contenido.repository;

import cl.triskeledu.contenido.model.Capsula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CapsulaRepository extends JpaRepository<Capsula, Long> {
    List<Capsula> findByPuntoId(Long puntoId);
    Optional<Capsula> findByCodigoQr(String codigoQr);
    boolean existsByCodigoQr(String codigoQr);
    void deleteByPuntoId(Long puntoId);
}
