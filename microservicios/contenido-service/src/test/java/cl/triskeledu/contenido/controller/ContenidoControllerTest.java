package cl.triskeledu.contenido.controller;

import cl.triskeledu.contenido.model.Punto;
import cl.triskeledu.contenido.service.ContenidoService;
import cl.triskeledu.contenido.service.Excepciones.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContenidoController.class)
class ContenidoControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ContenidoService servicio;

    private Punto plaza() {
        Punto p = new Punto();
        p.setId(1L);
        p.setNombre("Plaza de Armas");
        return p;
    }

    @Test
    void listaPuntos() throws Exception {
        when(servicio.listarPuntos()).thenReturn(List.of(plaza()));

        mvc.perform(get("/api/puntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Plaza de Armas"));
    }

    @Test
    void crearPuntoValidoDevuelve201() throws Exception {
        when(servicio.crearPunto(any())).thenReturn(plaza());

        mvc.perform(post("/api/puntos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Plaza\",\"descripcion\":\"Centro\",\"latitud\":-33.4,\"longitud\":-70.6,\"altitud\":540}"))
                .andExpect(status().isCreated());
    }

    @Test
    void crearPuntoConLatitudFueraDeRangoDevuelve400() throws Exception {
        mvc.perform(post("/api/puntos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Plaza\",\"descripcion\":\"Centro\",\"latitud\":200,\"longitud\":-70.6,\"altitud\":540}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void puntoInexistenteDevuelve404() throws Exception {
        when(servicio.obtenerPunto(9L)).thenThrow(new NoEncontrado("no existe"));

        mvc.perform(get("/api/puntos/9")).andExpect(status().isNotFound());
    }

    @Test
    void qrRepetidoDevuelve409() throws Exception {
        when(servicio.crearCapsula(any())).thenThrow(new Conflicto("QR en uso"));

        mvc.perform(post("/api/capsulas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntoId\":1,\"titulo\":\"IA\",\"urlVideo\":\"u\",\"codigoQr\":\"AIJS-001\",\"puntos\":10}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarCapsulaDevuelve204() throws Exception {
        mvc.perform(delete("/api/capsulas/1")).andExpect(status().isNoContent());
    }
}
