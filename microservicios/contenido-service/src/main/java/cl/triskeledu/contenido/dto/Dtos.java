package cl.triskeledu.contenido.dto;

import jakarta.validation.constraints.*;

public final class Dtos {
    private Dtos() {}

    public record PuntoRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Size(max = 400) String descripcion,
            @DecimalMin("-90.0") @DecimalMax("90.0") double latitud,
            @DecimalMin("-180.0") @DecimalMax("180.0") double longitud,
            double altitud) {}

    public record CapsulaRequest(
            @NotNull Long puntoId,
            @NotBlank @Size(max = 120) String titulo,
            @NotBlank @Size(max = 300) String urlVideo,
            @NotBlank @Size(max = 40) String codigoQr,
            @Min(1) @Max(1000) int puntos) {}

    public record ErrorResponse(int estado, String mensaje) {}
}
