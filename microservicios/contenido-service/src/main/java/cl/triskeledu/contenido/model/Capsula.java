package cl.triskeledu.contenido.model;

import jakarta.persistence.*;

@Entity
@Table(name = "capsulas", uniqueConstraints = @UniqueConstraint(columnNames = "codigo_qr"))
public class Capsula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "punto_id", nullable = false)
    private Long puntoId;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(name = "url_video", nullable = false, length = 300)
    private String urlVideo;

    @Column(name = "codigo_qr", nullable = false, length = 40)
    private String codigoQr;

    @Column(nullable = false)
    private int puntos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPuntoId() { return puntoId; }
    public void setPuntoId(Long puntoId) { this.puntoId = puntoId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
}
