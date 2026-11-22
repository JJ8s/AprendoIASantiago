package cl.triskeledu.contenido;

import cl.triskeledu.contenido.dto.Dtos.CapsulaRequest;
import cl.triskeledu.contenido.dto.Dtos.PuntoRequest;
import cl.triskeledu.contenido.service.ContenidoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Mismos 7 puntos y 7 cápsulas sintéticos que trae la app (se cargan una vez, si la base está vacía). */
@Configuration
public class DatosIniciales {

    private static final String VIDEO = "android.resource://com.example.aprendoiasantiago/raw/capsula_educacion";

    @Bean
    CommandLineRunner sembrar(ContenidoService s) {
        return args -> {
            if (!s.listarPuntos().isEmpty()) return;
            Object[][] puntos = {
                    {"Plaza de Armas", "Corazón histórico de Santiago, rodeado de la Catedral y el Correo Central.", -33.4372, -70.6506, 540.0},
                    {"Cerro Santa Lucía", "Parque-mirador en pleno centro con senderos, fuentes y vistas de la ciudad.", -33.4404, -70.6430, 600.0},
                    {"Cerro San Cristóbal", "El mayor cerro-parque de la ciudad, con el santuario de la Virgen en su cima.", -33.4223, -70.6313, 860.0},
                    {"Palacio de La Moneda", "Sede del Gobierno de Chile y del Centro Cultural La Moneda.", -33.4429, -70.6537, 545.0},
                    {"Centro Cultural GAM", "Espacio de artes escénicas, música y exposiciones en la Alameda.", -33.4375, -70.6399, 555.0},
                    {"Mercado Central", "Mercado tradicional de pescados y mariscos, de estructura de hierro.", -33.4331, -70.6517, 525.0},
                    {"Museo Nacional de Bellas Artes", "Edificio patrimonial del Parque Forestal dedicado al arte chileno.", -33.4358, -70.6437, 550.0}
            };
            String[] titulos = {
                    "¿Qué es la Inteligencia Artificial?", "Cómo aprende una máquina", "Reconocimiento de imágenes",
                    "La IA y los datos abiertos", "Cómo entiende una IA la voz y el idioma",
                    "La IA en la vida diaria", "Cuando la IA se equivoca: sesgos y errores"
            };
            int[] valor = {10, 10, 15, 10, 15, 10, 15};
            for (int i = 0; i < puntos.length; i++) {
                Object[] p = puntos[i];
                var punto = s.crearPunto(new PuntoRequest((String) p[0], (String) p[1],
                        (double) p[2], (double) p[3], (double) p[4]));
                s.crearCapsula(new CapsulaRequest(punto.getId(), titulos[i], VIDEO,
                        String.format("AIJS-%03d", i + 1), valor[i]));
            }
        };
    }
}
