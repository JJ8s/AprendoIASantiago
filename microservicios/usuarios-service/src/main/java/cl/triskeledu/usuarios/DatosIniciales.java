package cl.triskeledu.usuarios;

import cl.triskeledu.usuarios.dto.Dtos.RegistroRequest;
import cl.triskeledu.usuarios.repository.UsuarioRepository;
import cl.triskeledu.usuarios.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cuentas sintéticas del caso (solo datos ficticios). Se crean una vez, si la base está vacía. */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner sembrar(UsuarioRepository repo, UsuarioService servicio) {
        return args -> {
            if (repo.count() > 0) return;
            servicio.registrar(new RegistroRequest("Administrador", "Admin", "123"));
            servicio.registrar(new RegistroRequest("Creador de contenido", "GenCont", "123"));
            servicio.registrar(new RegistroRequest("Usuario Demo", "demo", "demo1234"));
        };
    }
}
