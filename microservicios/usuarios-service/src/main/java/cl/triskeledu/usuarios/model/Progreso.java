package cl.triskeledu.usuarios.model;

import jakarta.persistence.*;

/** Una cápsula completada por un usuario (un registro por par usuario + cápsula). */
@Entity
@Table(name = "progreso", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "capsula_id"}))
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "capsula_id", nullable = false)
    private Long capsulaId;

    @Column(name = "fecha_millis", nullable = false)
    private long fechaMillis;

    public Progreso() {}

    public Progreso(Long usuarioId, Long capsulaId, long fechaMillis) {
        this.usuarioId = usuarioId;
        this.capsulaId = capsulaId;
        this.fechaMillis = fechaMillis;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getCapsulaId() { return capsulaId; }
    public long getFechaMillis() { return fechaMillis; }
}
