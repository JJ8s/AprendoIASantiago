package cl.triskeledu.usuarios.service;

import cl.triskeledu.usuarios.dto.Dtos.*;
import cl.triskeledu.usuarios.model.Progreso;
import cl.triskeledu.usuarios.model.Usuario;
import cl.triskeledu.usuarios.repository.ProgresoRepository;
import cl.triskeledu.usuarios.repository.UsuarioRepository;
import cl.triskeledu.usuarios.service.Excepciones.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final ProgresoRepository progresos;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarios, ProgresoRepository progresos) {
        this.usuarios = usuarios;
        this.progresos = progresos;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest datos) {
        String alias = datos.nombreUsuario().trim();
        if (usuarios.existsByNombreUsuarioIgnoreCase(alias)) {
            throw new Conflicto("El nombre de usuario ya existe");
        }
        Usuario u = new Usuario();
        u.setNombreUsuario(alias);
        u.setNombreVisible(datos.nombreVisible().trim());
        u.setClaveHash(codificador.encode(datos.clave()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    public UsuarioResponse iniciarSesion(LoginRequest datos) {
        Usuario u = usuarios.findByNombreUsuarioIgnoreCase(datos.nombreUsuario().trim())
                .filter(x -> codificador.matches(datos.clave(), x.getClaveHash()))
                .orElseThrow(() -> new NoAutorizado("Usuario o contraseña incorrectos"));
        return UsuarioResponse.de(u);
    }

    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, ActualizarPerfilRequest datos) {
        Usuario u = buscar(id);
        u.setNombreVisible(datos.nombreVisible().trim());
        u.setCorreo(datos.correo() == null ? "" : datos.correo().trim());
        u.setTelefono(datos.telefono() == null ? "" : datos.telefono().trim());
        return UsuarioResponse.de(usuarios.save(u));
    }

    /** Cambia la contraseña si la actual es correcta (401 si no lo es). */
    @Transactional
    public void cambiarClave(Long id, CambiarClaveRequest datos) {
        Usuario u = buscar(id);
        if (!codificador.matches(datos.claveActual(), u.getClaveHash())) {
            throw new NoAutorizado("La contraseña actual no es correcta");
        }
        u.setClaveHash(codificador.encode(datos.claveNueva()));
        usuarios.save(u);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario u = buscar(id);
        progresos.deleteByUsuarioId(id);
        usuarios.delete(u);
    }

    /** Ranking: mayor puntaje primero. */
    public List<UsuarioResponse> ranking() {
        return usuarios.findAllByOrderByPuntajeDescNombreVisibleAsc().stream().map(UsuarioResponse::de).toList();
    }

    /**
     * Registra que el usuario completó una cápsula y le suma los puntos.
     * Es idempotente: repetir la misma cápsula no suma de nuevo (anti-duplicados, igual que en la app).
     */
    @Transactional
    public ProgresoResponse completarCapsula(Long usuarioId, Long capsulaId, int puntos) {
        Usuario u = buscar(usuarioId);
        if (progresos.existsByUsuarioIdAndCapsulaId(usuarioId, capsulaId)) {
            return new ProgresoResponse(usuarioId, capsulaId, false, u.getPuntaje());
        }
        progresos.save(new Progreso(usuarioId, capsulaId, System.currentTimeMillis()));
        u.setPuntaje(u.getPuntaje() + puntos);
        usuarios.save(u);
        return new ProgresoResponse(usuarioId, capsulaId, true, u.getPuntaje());
    }

    public List<Long> capsulasCompletadas(Long usuarioId) {
        buscar(usuarioId);
        return progresos.findByUsuarioId(usuarioId).stream().map(Progreso::getCapsulaId).toList();
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NoEncontrado("Usuario " + id + " no existe"));
    }
}
