package cl.triskeledu.usuarios.controller;

import cl.triskeledu.usuarios.dto.Dtos.UsuarioResponse;
import cl.triskeledu.usuarios.service.Excepciones.*;
import cl.triskeledu.usuarios.service.UsuarioService;
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

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired MockMvc mvc;
    @MockBean UsuarioService servicio;

    private final UsuarioResponse ana = new UsuarioResponse(1L, "ana", "Ana", 10, "", "");

    @Test
    void registrarDevuelve201() throws Exception {
        when(servicio.registrar(any())).thenReturn(ana);

        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreVisible\":\"Ana\",\"nombreUsuario\":\"ana\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreUsuario").value("ana"))
                .andExpect(jsonPath("$.claveHash").doesNotExist());
    }

    @Test
    void registrarConDatosInvalidosDevuelve400() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreVisible\":\"\",\"nombreUsuario\":\"a\",\"clave\":\"1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aliasRepetidoDevuelve409() throws Exception {
        when(servicio.registrar(any())).thenThrow(new Conflicto("ya existe"));

        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreVisible\":\"Ana\",\"nombreUsuario\":\"ana\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        when(servicio.iniciarSesion(any())).thenThrow(new NoAutorizado("mal"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"ana\",\"clave\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioInexistenteDevuelve404() throws Exception {
        when(servicio.obtener(9L)).thenThrow(new NoEncontrado("no existe"));

        mvc.perform(get("/api/usuarios/9")).andExpect(status().isNotFound());
    }

    @Test
    void rankingDevuelveLista() throws Exception {
        when(servicio.ranking()).thenReturn(List.of(ana));

        mvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].puntaje").value(10));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());
    }

    @Test
    void cambiarClaveDevuelve204() throws Exception {
        mvc.perform(put("/api/usuarios/1/clave").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"secreta1\",\"claveNueva\":\"nueva123\"}"))
                .andExpect(status().isNoContent());
    }
}
