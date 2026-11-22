package cl.triskeledu.usuarios.service;

import cl.triskeledu.usuarios.dto.Dtos.*;
import cl.triskeledu.usuarios.model.Usuario;
import cl.triskeledu.usuarios.repository.ProgresoRepository;
import cl.triskeledu.usuarios.repository.UsuarioRepository;
import cl.triskeledu.usuarios.service.Excepciones.*;
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
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock ProgresoRepository progresos;
    UsuarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioService(usuarios, progresos);
    }

    private Usuario usuario(long id, int puntaje) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombreUsuario("ana");
        u.setNombreVisible("Ana");
        u.setPuntaje(puntaje);
        return u;
    }

    @Test
    void registrarGuardaLaClaveCifradaNoEnTextoPlano() {
        when(usuarios.existsByNombreUsuarioIgnoreCase("ana")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = servicio.registrar(new RegistroRequest("Ana", "ana", "secreta1"));

        assertEquals("ana", r.nombreUsuario());
        verify(usuarios).save(argThat(u -> !u.getClaveHash().equals("secreta1") && u.getClaveHash().startsWith("$2")));
    }

    @Test
    void registrarAliasRepetidoLanzaConflicto() {
        when(usuarios.existsByNombreUsuarioIgnoreCase("ana")).thenReturn(true);

        assertThrows(Conflicto.class, () -> servicio.registrar(new RegistroRequest("Ana", "ana", "secreta1")));
        verify(usuarios, never()).save(any());
    }

    @Test
    void loginCorrectoYIncorrecto() {
        when(usuarios.existsByNombreUsuarioIgnoreCase("ana")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        servicio.registrar(new RegistroRequest("Ana", "ana", "secreta1"));
        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        when(usuarios.findByNombreUsuarioIgnoreCase("ana")).thenReturn(Optional.of(captor.getValue()));

        assertEquals("Ana", servicio.iniciarSesion(new LoginRequest("ana", "secreta1")).nombreVisible());
        assertThrows(NoAutorizado.class, () -> servicio.iniciarSesion(new LoginRequest("ana", "mala")));
    }

    @Test
    void loginDeUsuarioInexistenteEsNoAutorizado() {
        when(usuarios.findByNombreUsuarioIgnoreCase("nadie")).thenReturn(Optional.empty());

        assertThrows(NoAutorizado.class, () -> servicio.iniciarSesion(new LoginRequest("nadie", "x")));
    }

    @Test
    void completarCapsulaNuevaSumaPuntos() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario(1, 10)));
        when(progresos.existsByUsuarioIdAndCapsulaId(1L, 5L)).thenReturn(false);

        ProgresoResponse r = servicio.completarCapsula(1L, 5L, 15);

        assertTrue(r.nuevo());
        assertEquals(25, r.puntajeTotal());
        verify(progresos).save(any());
    }

    @Test
    void completarLaMismaCapsulaNoSumaDeNuevo() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario(1, 10)));
        when(progresos.existsByUsuarioIdAndCapsulaId(1L, 5L)).thenReturn(true);

        ProgresoResponse r = servicio.completarCapsula(1L, 5L, 15);

        assertFalse(r.nuevo());
        assertEquals(10, r.puntajeTotal());
        verify(progresos, never()).save(any());
    }

    @Test
    void usuarioInexistenteLanzaNoEncontrado() {
        when(usuarios.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NoEncontrado.class, () -> servicio.obtener(9L));
        assertThrows(NoEncontrado.class, () -> servicio.eliminar(9L));
    }

    @Test
    void actualizarPerfilCambiaNombreCorreoYTelefono() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario(1, 0)));
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = servicio.actualizarPerfil(1L, new ActualizarPerfilRequest(" Ana M ", "a@b.cl", null));

        assertEquals("Ana M", r.nombreVisible());
        assertEquals("a@b.cl", r.correo());
        assertEquals("", r.telefono());
    }

    @Test
    void eliminarBorraPrimeroSuProgreso() {
        Usuario u = usuario(1, 0);
        when(usuarios.findById(1L)).thenReturn(Optional.of(u));

        servicio.eliminar(1L);

        var orden = inOrder(progresos, usuarios);
        orden.verify(progresos).deleteByUsuarioId(1L);
        orden.verify(usuarios).delete(u);
    }

    @Test
    void rankingDevuelveLoQueEntregaElRepositorioEnOrden() {
        when(usuarios.findAllByOrderByPuntajeDescNombreVisibleAsc()).thenReturn(List.of(usuario(2, 30), usuario(1, 10)));

        List<UsuarioResponse> r = servicio.ranking();

        assertEquals(30, r.get(0).puntaje());
        assertEquals(2, r.size());
    }

    @Test
    void cambiarClaveConLaActualCorrectaGuardaLaNueva() {
        when(usuarios.existsByNombreUsuarioIgnoreCase("ana")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        servicio.registrar(new RegistroRequest("Ana", "ana", "secreta1"));
        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario guardado = captor.getValue();
        guardado.setId(1L);
        String hashViejo = guardado.getClaveHash();
        when(usuarios.findById(1L)).thenReturn(Optional.of(guardado));

        assertThrows(NoAutorizado.class, () -> servicio.cambiarClave(1L, new CambiarClaveRequest("mala", "nueva123")));
        servicio.cambiarClave(1L, new CambiarClaveRequest("secreta1", "nueva123"));

        assertNotEquals(hashViejo, guardado.getClaveHash());
    }
}
