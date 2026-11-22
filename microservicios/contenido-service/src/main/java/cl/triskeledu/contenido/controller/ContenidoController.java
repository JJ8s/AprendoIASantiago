package cl.triskeledu.contenido.controller;

import cl.triskeledu.contenido.dto.Dtos.*;
import cl.triskeledu.contenido.model.Capsula;
import cl.triskeledu.contenido.model.Punto;
import cl.triskeledu.contenido.service.ContenidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContenidoController {

    private final ContenidoService servicio;

    public ContenidoController(ContenidoService servicio) {
        this.servicio = servicio;
    }

    // --- Puntos turísticos ---
    @GetMapping("/puntos")
    public List<Punto> puntos() { return servicio.listarPuntos(); }

    @GetMapping("/puntos/{id}")
    public Punto punto(@PathVariable Long id) { return servicio.obtenerPunto(id); }

    @PostMapping("/puntos")
    @ResponseStatus(HttpStatus.CREATED)
    public Punto crearPunto(@Valid @RequestBody PuntoRequest d) { return servicio.crearPunto(d); }

    @PutMapping("/puntos/{id}")
    public Punto actualizarPunto(@PathVariable Long id, @Valid @RequestBody PuntoRequest d) {
        return servicio.actualizarPunto(id, d);
    }

    @DeleteMapping("/puntos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPunto(@PathVariable Long id) { servicio.eliminarPunto(id); }

    @GetMapping("/puntos/{id}/capsulas")
    public List<Capsula> capsulasDelPunto(@PathVariable Long id) { return servicio.capsulasDelPunto(id); }

    // --- Cápsulas ---
    @GetMapping("/capsulas")
    public List<Capsula> capsulas() { return servicio.listarCapsulas(); }

    @GetMapping("/capsulas/{id}")
    public Capsula capsula(@PathVariable Long id) { return servicio.obtenerCapsula(id); }

    /** El escáner QR de la app consulta aquí el código leído. */
    @GetMapping("/capsulas/qr/{codigo}")
    public Capsula porQr(@PathVariable String codigo) { return servicio.buscarPorQr(codigo); }

    @PostMapping("/capsulas")
    @ResponseStatus(HttpStatus.CREATED)
    public Capsula crearCapsula(@Valid @RequestBody CapsulaRequest d) { return servicio.crearCapsula(d); }

    @PutMapping("/capsulas/{id}")
    public Capsula actualizarCapsula(@PathVariable Long id, @Valid @RequestBody CapsulaRequest d) {
        return servicio.actualizarCapsula(id, d);
    }

    @DeleteMapping("/capsulas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCapsula(@PathVariable Long id) { servicio.eliminarCapsula(id); }
}
