package cl.triskeledu.usuarios.dto;

import cl.triskeledu.usuarios.model.Usuario;
import jakarta.validation.constraints.*;

/** Objetos de entrada y salida de la API (la entidad nunca se expone: así no se filtra el hash). */
public final class Dtos {
    private Dtos() {}

    public record RegistroRequest(
            @NotBlank @Size(max = 40) String nombreVisible,
            @NotBlank @Size(min = 3, max = 30) String nombreUsuario,
            @NotBlank @Size(min = 3, max = 60) String clave) {}

    public record LoginRequest(@NotBlank String nombreUsuario, @NotBlank String clave) {}

    public record ActualizarPerfilRequest(
            @NotBlank @Size(max = 40) String nombreVisible,
            @Email @Size(max = 120) String correo,
            @Size(max = 20) String telefono) {}

    public record CambiarClaveRequest(
            @NotBlank String claveActual,
            @NotBlank @Size(min = 3, max = 60) String claveNueva) {}

    public record ProgresoRequest(@Positive int puntos) {}

    public record UsuarioResponse(Long id, String nombreUsuario, String nombreVisible, int puntaje,
                                  String correo, String telefono) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombreUsuario(), u.getNombreVisible(), u.getPuntaje(),
                    u.getCorreo(), u.getTelefono());
        }
    }

    public record ProgresoResponse(Long usuarioId, Long capsulaId, boolean nuevo, int puntajeTotal) {}

    public record ErrorResponse(int estado, String mensaje) {}
}
