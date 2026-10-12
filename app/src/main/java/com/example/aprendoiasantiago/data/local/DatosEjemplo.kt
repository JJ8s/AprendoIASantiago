package com.example.aprendoiasantiago.data.local

import com.example.aprendoiasantiago.utils.Hash

/**
 * Datos ficticios para poblar la base local (regla del caso: solo datos sintéticos).
 * Las coordenadas y altitudes son aproximadas: reemplázalas por el listado oficial que entrega el caso.
 * Todas las cápsulas apuntan a un mismo video de muestra: reemplaza [VIDEO_PRUEBA] (o cada urlVideo)
 * por los enlaces de los videos piloto que entrega el caso.
 */
object DatosEjemplo {
    const val USUARIO_DEMO = "demo"
    const val CLAVE_DEMO = "demo1234"

    /** Video público de muestra (Big Buck Bunny) para probar el reproductor. */
    const val VIDEO_PRUEBA = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    val puntos: List<PuntoTuristicoEntity> = listOf(
        PuntoTuristicoEntity(
            id = 1, nombre = "Plaza de Armas",
            descripcion = "Corazón histórico de Santiago, rodeado de la Catedral y el Correo Central.",
            latitud = -33.4372, longitud = -70.6506, altitud = 540.0
        ),
        PuntoTuristicoEntity(
            id = 2, nombre = "Cerro Santa Lucía",
            descripcion = "Parque-mirador en pleno centro con senderos, fuentes y vistas de la ciudad.",
            latitud = -33.4404, longitud = -70.6430, altitud = 600.0
        ),
        PuntoTuristicoEntity(
            id = 3, nombre = "Cerro San Cristóbal",
            descripcion = "El mayor cerro-parque de la ciudad, con el santuario de la Virgen en su cima.",
            latitud = -33.4223, longitud = -70.6313, altitud = 860.0
        ),
        PuntoTuristicoEntity(
            id = 4, nombre = "Palacio de La Moneda",
            descripcion = "Sede del Gobierno de Chile y del Centro Cultural La Moneda.",
            latitud = -33.4429, longitud = -70.6537, altitud = 545.0
        ),
        PuntoTuristicoEntity(
            id = 5, nombre = "Centro Cultural GAM",
            descripcion = "Espacio de artes escénicas, música y exposiciones en la Alameda.",
            latitud = -33.4375, longitud = -70.6399, altitud = 555.0
        ),
        PuntoTuristicoEntity(
            id = 6, nombre = "Mercado Central",
            descripcion = "Mercado tradicional de pescados y mariscos, de estructura de hierro.",
            latitud = -33.4331, longitud = -70.6517, altitud = 525.0
        ),
        PuntoTuristicoEntity(
            id = 7, nombre = "Museo Nacional de Bellas Artes",
            descripcion = "Edificio patrimonial del Parque Forestal dedicado al arte chileno.",
            latitud = -33.4358, longitud = -70.6437, altitud = 550.0
        )
    )

    val capsulas: List<CapsulaEntity> = listOf(
        CapsulaEntity(1, puntoId = 1, titulo = "¿Qué es la Inteligencia Artificial?", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-001", puntos = 10),
        CapsulaEntity(2, puntoId = 2, titulo = "Cómo aprende una máquina", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-002", puntos = 10),
        CapsulaEntity(3, puntoId = 3, titulo = "Reconocimiento de imágenes", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-003", puntos = 15),
        CapsulaEntity(4, puntoId = 4, titulo = "La IA y los datos abiertos", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-004", puntos = 10),
        CapsulaEntity(5, puntoId = 5, titulo = "Cómo entiende una IA la voz y el idioma", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-005", puntos = 15),
        CapsulaEntity(6, puntoId = 6, titulo = "La IA en la vida diaria", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-006", puntos = 10),
        CapsulaEntity(7, puntoId = 7, titulo = "Cuando la IA se equivoca: sesgos y errores", urlVideo = VIDEO_PRUEBA, codigoQr = "AIJS-007", puntos = 15)
    )

    val usuarios: List<UsuarioEntity> = listOf(
        UsuarioEntity(id = 1, nombreUsuario = USUARIO_DEMO, claveHash = Hash.sha256(CLAVE_DEMO), nombreVisible = "Usuario Demo", puntaje = 0),
        // Usuarios ficticios solo para el ranking (claveHash vacío: no pueden iniciar sesión).
        UsuarioEntity(id = 2, nombreUsuario = "camila", claveHash = "", nombreVisible = "Camila R.", puntaje = 320),
        UsuarioEntity(id = 3, nombreUsuario = "matias", claveHash = "", nombreVisible = "Matías P.", puntaje = 270),
        UsuarioEntity(id = 4, nombreUsuario = "valentina", claveHash = "", nombreVisible = "Valentina S.", puntaje = 210),
        UsuarioEntity(id = 5, nombreUsuario = "diego", claveHash = "", nombreVisible = "Diego T.", puntaje = 150),
        UsuarioEntity(id = 6, nombreUsuario = "sofia", claveHash = "", nombreVisible = "Sofía L.", puntaje = 90)
    )
}
