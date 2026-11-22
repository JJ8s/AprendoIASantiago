package cl.triskeledu.contenido.service;

import cl.triskeledu.contenido.dto.Dtos.*;
import cl.triskeledu.contenido.model.Capsula;
import cl.triskeledu.contenido.model.Punto;
import cl.triskeledu.contenido.repository.CapsulaRepository;
import cl.triskeledu.contenido.repository.PuntoRepository;
import cl.triskeledu.contenido.service.Excepciones.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContenidoServiceTest {

    @Mock PuntoRepository puntos;
    @Mock CapsulaRepository capsulas;
    ContenidoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ContenidoService(puntos, capsulas);
    }

    private Punto punto(long id) {
        Punto p = new Punto();
        p.setId(id);
        p.setNombre("Plaza");
        return p;
    }

    private Capsula capsula(long id, String qr) {
        Capsula c = new Capsula();
        c.setId(id);
        c.setPuntoId(1L);
        c.setCodigoQr(qr);
        return c;
    }

    private final PuntoRequest datosPunto = new PuntoRequest("  Plaza  ", " Centro ", -33.4, -70.6, 540);
    private final CapsulaRequest datosCapsula = new CapsulaRequest(1L, "IA", "url", "AIJS-100", 10);

    @Test
    void crearPuntoRecortaEspaciosYGuarda() {
        when(puntos.save(any(Punto.class))).thenAnswer(i -> i.getArgument(0));

        Punto p = servicio.crearPunto(datosPunto);

        assertEquals("Plaza", p.getNombre());
        assertEquals("Centro", p.getDescripcion());
    }

    @Test
    void actualizarPuntoInexistenteLanzaNoEncontrado() {
        when(puntos.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NoEncontrado.class, () -> servicio.actualizarPunto(9L, datosPunto));
    }

    @Test
    void eliminarPuntoBorraTambienSusCapsulas() {
        Punto p = punto(1);
        when(puntos.findById(1L)).thenReturn(Optional.of(p));

        servicio.eliminarPunto(1L);

        var orden = inOrder(capsulas, puntos);
        orden.verify(capsulas).deleteByPuntoId(1L);
        orden.verify(puntos).delete(p);
    }

    @Test
    void crearCapsulaEnPuntoInexistenteFalla() {
        when(puntos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoEncontrado.class, () -> servicio.crearCapsula(datosCapsula));
        verify(capsulas, never()).save(any());
    }

    @Test
    void crearCapsulaConQrRepetidoLanzaConflicto() {
        when(puntos.findById(1L)).thenReturn(Optional.of(punto(1)));
        when(capsulas.existsByCodigoQr("AIJS-100")).thenReturn(true);

        assertThrows(Conflicto.class, () -> servicio.crearCapsula(datosCapsula));
    }

    @Test
    void crearCapsulaValidaLaGuarda() {
        when(puntos.findById(1L)).thenReturn(Optional.of(punto(1)));
        when(capsulas.existsByCodigoQr("AIJS-100")).thenReturn(false);
        when(capsulas.save(any(Capsula.class))).thenAnswer(i -> i.getArgument(0));

        Capsula c = servicio.crearCapsula(datosCapsula);

        assertEquals("AIJS-100", c.getCodigoQr());
        assertEquals(10, c.getPuntos());
    }

    @Test
    void actualizarCapsulaConservandoSuQrNoDaConflicto() {
        when(capsulas.findById(5L)).thenReturn(Optional.of(capsula(5, "AIJS-100")));
        when(puntos.findById(1L)).thenReturn(Optional.of(punto(1)));
        when(capsulas.save(any(Capsula.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> servicio.actualizarCapsula(5L, datosCapsula));
        verify(capsulas, never()).existsByCodigoQr(any());
    }

    @Test
    void actualizarCapsulaConQrDeOtraLanzaConflicto() {
        when(capsulas.findById(5L)).thenReturn(Optional.of(capsula(5, "AIJS-001")));
        when(puntos.findById(1L)).thenReturn(Optional.of(punto(1)));
        when(capsulas.existsByCodigoQr("AIJS-100")).thenReturn(true);

        assertThrows(Conflicto.class, () -> servicio.actualizarCapsula(5L, datosCapsula));
    }

    @Test
    void buscarPorQrEncuentraOFalla() {
        when(capsulas.findByCodigoQr("AIJS-001")).thenReturn(Optional.of(capsula(1, "AIJS-001")));
        when(capsulas.findByCodigoQr("XXX")).thenReturn(Optional.empty());

        assertEquals(1L, servicio.buscarPorQr(" AIJS-001 ").getId());
        assertThrows(NoEncontrado.class, () -> servicio.buscarPorQr("XXX"));
    }

    @Test
    void capsulasDelPuntoListaLasDeEsePunto() {
        when(puntos.findById(1L)).thenReturn(Optional.of(punto(1)));
        when(capsulas.findByPuntoId(1L)).thenReturn(List.of(capsula(1, "A"), capsula(2, "B")));

        assertEquals(2, servicio.capsulasDelPunto(1L).size());
    }

    @Test
    void eliminarCapsulaInexistenteLanzaNoEncontrado() {
        when(capsulas.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NoEncontrado.class, () -> servicio.eliminarCapsula(7L));
    }
}
