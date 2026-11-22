package cl.triskeledu.usuarios.controller;

import cl.triskeledu.usuarios.dto.Dtos.*;
import cl.triskeledu.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    // --- Autenticación y CRUD de usuarios ---
    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return servicio.registrar(datos);
    }

    @PostMapping("/auth/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest datos) {
        return servicio.iniciarSesion(datos);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarPerfilRequest datos) {
        return servicio.actualizarPerfil(id, datos);
    }

    @PutMapping("/usuarios/{id}/clave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarClave(@PathVariable Long id, @Valid @RequestBody CambiarClaveRequest datos) {
        servicio.cambiarClave(id, datos);
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    // --- Progreso y ranking ---
    @GetMapping("/ranking")
    public List<UsuarioResponse> ranking() {
        return servicio.ranking();
    }

    @PostMapping("/usuarios/{id}/progreso/{capsulaId}")
    public ProgresoResponse completarCapsula(@PathVariable Long id, @PathVariable Long capsulaId,
                                             @Valid @RequestBody ProgresoRequest datos) {
        return servicio.completarCapsula(id, capsulaId, datos.puntos());
    }

    @GetMapping("/usuarios/{id}/progreso")
    public List<Long> capsulasCompletadas(@PathVariable Long id) {
        return servicio.capsulasCompletadas(id);
    }
}
