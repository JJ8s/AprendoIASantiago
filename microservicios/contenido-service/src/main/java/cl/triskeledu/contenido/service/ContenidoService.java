package cl.triskeledu.contenido.service;

import cl.triskeledu.contenido.dto.Dtos.*;
import cl.triskeledu.contenido.model.Capsula;
import cl.triskeledu.contenido.model.Punto;
import cl.triskeledu.contenido.repository.CapsulaRepository;
import cl.triskeledu.contenido.repository.PuntoRepository;
import cl.triskeledu.contenido.service.Excepciones.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContenidoService {

    private final PuntoRepository puntos;
    private final CapsulaRepository capsulas;

    public ContenidoService(PuntoRepository puntos, CapsulaRepository capsulas) {
        this.puntos = puntos;
        this.capsulas = capsulas;
    }

    // ---------- Puntos turísticos ----------
    public List<Punto> listarPuntos() { return puntos.findAll(); }

    public Punto obtenerPunto(Long id) {
        return puntos.findById(id).orElseThrow(() -> new NoEncontrado("Punto " + id + " no existe"));
    }

    @Transactional
    public Punto crearPunto(PuntoRequest d) {
        return puntos.save(aplicar(new Punto(), d));
    }

    @Transactional
    public Punto actualizarPunto(Long id, PuntoRequest d) {
        return puntos.save(aplicar(obtenerPunto(id), d));
    }

    /** Al borrar un punto se borran también sus cápsulas (igual que el CASCADE de la base local de la app). */
    @Transactional
    public void eliminarPunto(Long id) {
        Punto p = obtenerPunto(id);
        capsulas.deleteByPuntoId(id);
        puntos.delete(p);
    }

    // ---------- Cápsulas ----------
    public List<Capsula> listarCapsulas() { return capsulas.findAll(); }

    public List<Capsula> capsulasDelPunto(Long puntoId) {
        obtenerPunto(puntoId);
        return capsulas.findByPuntoId(puntoId);
    }

    public Capsula obtenerCapsula(Long id) {
        return capsulas.findById(id).orElseThrow(() -> new NoEncontrado("Cápsula " + id + " no existe"));
    }

    public Capsula buscarPorQr(String codigo) {
        return capsulas.findByCodigoQr(codigo.trim())
                .orElseThrow(() -> new NoEncontrado("No hay cápsula con el código " + codigo));
    }

    @Transactional
    public Capsula crearCapsula(CapsulaRequest d) {
        obtenerPunto(d.puntoId());
        if (capsulas.existsByCodigoQr(d.codigoQr().trim())) {
            throw new Conflicto("El código QR ya está en uso");
        }
        return capsulas.save(aplicar(new Capsula(), d));
    }

    @Transactional
    public Capsula actualizarCapsula(Long id, CapsulaRequest d) {
        Capsula c = obtenerCapsula(id);
        obtenerPunto(d.puntoId());
        boolean cambioQr = !c.getCodigoQr().equals(d.codigoQr().trim());
        if (cambioQr && capsulas.existsByCodigoQr(d.codigoQr().trim())) {
            throw new Conflicto("El código QR ya está en uso");
        }
        return capsulas.save(aplicar(c, d));
    }

    @Transactional
    public void eliminarCapsula(Long id) {
        capsulas.delete(obtenerCapsula(id));
    }

    private Punto aplicar(Punto p, PuntoRequest d) {
        p.setNombre(d.nombre().trim());
        p.setDescripcion(d.descripcion().trim());
        p.setLatitud(d.latitud());
        p.setLongitud(d.longitud());
        p.setAltitud(d.altitud());
        return p;
    }

    private Capsula aplicar(Capsula c, CapsulaRequest d) {
        c.setPuntoId(d.puntoId());
        c.setTitulo(d.titulo().trim());
        c.setUrlVideo(d.urlVideo().trim());
        c.setCodigoQr(d.codigoQr().trim());
        c.setPuntos(d.puntos());
        return c;
    }
}
