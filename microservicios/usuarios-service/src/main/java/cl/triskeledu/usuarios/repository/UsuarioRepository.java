package cl.triskeledu.usuarios.repository;

import cl.triskeledu.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);
    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);
    List<Usuario> findAllByOrderByPuntajeDescNombreVisibleAsc();
}
