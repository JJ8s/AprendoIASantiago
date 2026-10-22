
# Aprendo IA Jugando en Santiago

Aplicación Android nativa de aprendizaje gamificado de Inteligencia Artificial en entornos urbanos. El jugador recorre puntos turísticos emblemáticos de Santiago, **escanea códigos QR físicos**, **visualiza cápsulas educativas en video (Shorts)** sobre IA, suma puntajes en tiempo real, sube en el ranking local y registra **evidencia fotográfica** validada con Inteligencia Artificial en el dispositivo (detección facial con ML Kit).

Proyecto desarrollado para el caso de estudio *Aprendo IA Jugando en Santiago* (**Triskeledu SpA**, asignatura DSY1105 - Duoc UC).

Arquitectura **100% autónoma y persistente en SQLite (Room)**, con soporte multi-usuario en un mismo dispositivo y control de acceso por roles (**Cliente/Jugador**, **Administrador** y **Creador de Contenido**).

## 1. Requisitos del Entorno y Ejecución

| Requisito | Detalle Recomendado | 
| ----- | ----- | 
| **Android Studio** | Versión reciente compatible con AGP 9.x (o ajustar `agp = "9.3.0"` en `gradle/libs.versions.toml`). | 
| **JDK** | JDK embebido de Android Studio (JBR 17/21). | 
| **Dispositivo Físico / AVD** | Android 7.0+ (`minSdk 24`). **Emulador validado y recomendado: Pixel 7 con API 35 (Android 15)** para garantizar salida de red limpia en `ExoPlayer` y uso de webcam. | 
| **Cámara en Emulador** | En *Device Manager → Edit → Show Advanced Settings*, configurar cámara **Rear = Webcam0** para escanear códigos QR reales desde el notebook. | 
| **Internet** | Requerido para la descarga de teselas de OpenStreetMap y streaming de las cápsulas `.mp4` mediante `Media3 ExoPlayer`. | 

### Pasos de Instalación

1. Clonar el repositorio o descomprimir el proyecto y abrir la carpeta en **Android Studio**.

2. Esperar a que finalice el **Gradle Sync**.

3. Seleccionar un teléfono físico por USB o el emulador **Pixel 7 (API 35)** y pulsar **Run ▶**.

4. Para ejecutar la suite de pruebas unitarias locales: `./gradlew test` (o clic derecho sobre `app/src/test` → *Run Tests*).

> **Nota importante sobre SQLite:** Si se modifica la URL del video en `DatosEjemplo.kt` o se altera el esquema inicial, **desinstala la aplicación del emulador/teléfono** antes de volver a darle *Run* para forzar la recreación limpia de la base de datos local.

## 2. Roles del Sistema y Cuentas de Acceso

La aplicación inicia en la portada animada de **Bienvenida** y da paso al selector **¿Quién juega hoy?**, permitiendo alternar entre múltiples perfiles registrados en el mismo teléfono o iniciar sesión según el rol requerido por el caso de estudio:

| Rol | Usuario / Alias | Contraseña | Funcionalidades Implementadas | 
| ----- | ----- | ----- | ----- | 
| **Administrador** *(En duro)* | `Admin` | `123` | Acceso exclusivo al **Panel de Administración (3 pestañas)**: gestión en tiempo real sobre SQLite de **Jugadores** (crear/eliminar), **Puntos Turísticos** (crear puntos georreferenciados con coordenadas/eliminar) y **Cápsulas** (crear/eliminar). | 
| **Creador de Contenido** *(En duro)* | `GenCont` | `123` | Acceso exclusivo al **Panel de Creador de Contenido**: alta, clasificación y eliminación de cápsulas educativas en video asociadas a códigos QR, puntos turísticos y puntajes. | 
| **Jugador de Prueba** | `demo` | `demo1234` | Perfil de cliente pre-cargado para recorrer el flujo completo del juego. | 
| **Nuevos Jugadores (Clientes)** | *A elección* | *A elección* | Registro local con validación de formulario, confirmación de contraseña y selección de avatar. Cada perfil mantiene su propio historial, colección y puntaje aislado en SQLite. | 

*Seguridad:* El inicio de sesión no distingue mayúsculas/minúsculas en el alias y las contraseñas jamás se almacenan en texto plano; se persiste su **hash SHA-256**.

**Perfiles sintéticos adicionales para el Ranking:** Camila R. (320 pts), Matías P. (270 pts), Valentina S. (210 pts), Diego T. (150 pts), Sofía L. (90 pts).

## 3. Puntos Georreferenciados y Códigos QR Oficiales

Los códigos QR utilizan identificadores limpios en **texto plano** que la aplicación cruza mediante consultas asíncronas contra la base de datos local SQLite:

| \# | Punto Turístico (Santiago) | Código QR (Texto) | Cápsula Educativa Asociada | Valor | 
| ----- | ----- | ----- | ----- | ----- | 
| 1 | Plaza de Armas | `AIJS-001` | ¿Qué es la Inteligencia Artificial? | 10 pts | 
| 2 | Cerro Santa Lucía | `AIJS-002` | Cómo aprende una máquina | 10 pts | 
| 3 | Cerro San Cristóbal | `AIJS-003` | Reconocimiento de imágenes | 15 pts | 
| 4 | Palacio de La Moneda | `AIJS-004` | La IA y los datos abiertos | 10 pts | 
| 5 | Centro Cultural GAM | `AIJS-005` | Cómo entiende una IA la voz y el idioma | 15 pts | 
| 6 | Mercado Central | `AIJS-006` | La IA en la vida diaria | 10 pts | 
| 7 | Museo Nacional de Bellas Artes | `AIJS-007` | Cuando la IA se equivoca: sesgos y errores | 15 pts | 

* **Generación para pruebas en vivo:** Puedes generar cualquiera de estos códigos en formato **Texto** (ej. `AIJS-002`) en plataformas como [qr.io](https://qr.io) y mostrarlo frente a la cámara del teléfono o webcam del computador.

* **Ingreso manual de respaldo:** La pantalla de escáner incluye un campo de texto para digitar el código manualmente en caso de pruebas rápidas sin cámara.

* **Rutas temáticas predefinidas:**

  * *Centro histórico*: La Moneda → Plaza de Armas → Mercado Central → Bellas Artes.

  * *Cerros y cultura*: Santa Lucía → GAM → San Cristóbal.

## 4. Flujo Completo de la Aplicación

### A. Flujo del Cliente (Jugador)

1. **Bienvenida y Selección Multi-Perfil:** Portada interactiva, creación de nuevos jugadores o selección rápida de perfiles registrados en el dispositivo.

2. **Dashboard Principal (`InicioScreen`):** Saludo personalizado, tarjeta de puntaje acumulado, barra de progreso hacia la meta, indicadores de cápsulas descubiertas y accesos directos.

3. **Mapa Interactivo (`MapaScreen`):** Mapa basado en OpenStreetMap (`osmdroid`) con los marcadores georreferenciados cargados desde SQLite. Los hitos completados cambian a color verde, permite activar rutas temáticas trazadas en pantalla y consultar el detalle de cada lugar.

4. **Escáner QR (`EscanerScreen`):** Lectura mediante **CameraX + Google ML Kit**.

   * Si el QR es válido y nuevo para ese jugador: registra la captura en una transacción atómica, suma el puntaje, almacena timestamp y coordenadas GPS (`latitud`, `longitud`, `altitud`).

   * Si ya fue escaneado por el mismo jugador: informa que ya está descubierto y **evita duplicar puntajes**.

   * Si no pertenece al sistema: rechaza la lectura con alerta visual.

5. **Reproductor de Cápsula (`CapsulaScreen` y `ReproductorVideo`):** Reproducción nativa de video `.mp4` mediante **Media3 (ExoPlayer)** consumiendo el recurso alojado en el repositorio, con manejo automático de ciclo de vida (`ON_PAUSE` y liberación de memoria con `DisposableEffect`).

6. **Selfie Turística y Evidencia (`EvidenciaScreen`):** Flujo de cámara en 2 pasos:

   * *Paso 1 (Selfie):* Validación en el dispositivo mediante **ML Kit Face Detection** (exige un único rostro visible, encuadrado y con buena iluminación).

   * *Paso 2 (Entorno):* Captura con cámara trasera del punto turístico.

7. **Colección y Perfil (`ColeccionScreen` / `PerfilScreen`):** Galería histórica de cápsulas desbloqueadas con acceso de repetición al video, desglose de progreso, tabla de **Ranking** y cambio de sesión.

### B. Flujo de Administración y Creación de Contenido

* **Panel de Administrador (`AdminScreen`):** Al autenticarse como `Admin`, el `NavHost` redirige a una vista de gestión con `TabRow` de 3 secciones (*Jugadores*, *Puntos* y *Cápsulas*) y botón flotante (`FAB`) que despliega diálogos reactivos para insertar o eliminar registros directamente en SQLite.

* **Panel de Creador (`GenContScreen`):** Al autenticarse como `GenCont`, accede al mantenedor dedicado de cápsulas educativas, permitiendo asociar título, URL de video `.mp4`, código QR único, punto turístico y puntaje.

## 5. Base de Datos Local (Room / SQLite)

Base de datos relacional local `aprendo_ia_santiago.db` gestionada con **Room**, **Corrutinas (`suspend`)** y **`Flow`** para actualización reactiva de la UI sin bloquear el hilo principal (*Main Thread*).

| Tabla (`@Entity`) | Descripción | Campos Principales | 
| ----- | ----- | ----- | 
| `usuarios` | Perfiles de jugadores y roles | `id`, `nombreUsuario` (UNIQUE), `claveHash` (SHA-256), `nombreVisible`, `puntaje` | 
| `puntos_turisticos` | Hitos georreferenciados de Santiago | `id`, `nombre`, `descripcion`, `latitud`, `longitud`, `altitud` | 
| `capsulas` | Videos cortos asociados a cada QR | `id`, `puntoId` (FK), `titulo`, `urlVideo`, `codigoQr` (UNIQUE), `puntos` | 
| `progreso` | Historial por jugador y evidencias | `usuarioId` + `capsulaId` (PK compuesta), `completada`, `fechaMillis`, `latitud`, `longitud`, `altitud`, `selfieUri`, `fotoEntornoUri` | 
| `rutas` / `ruta_puntos` | Recorridos turísticos sugeridos | `id`, `nombre`, `descripcion` / `rutaId` + `puntoId`, `orden` | 

## 6. Arquitectura del Proyecto (MVVM)

```
UI (Jetpack Compose)  ──observa──►  ViewModel (StateFlow)  ──consume──►  Repositorios  ──consulta──►  DAO (Room / SQLite)

```

```
app/src/main/java/com/example/aprendoiasantiago/
├── AprendoApp.kt / AppContainer.kt     Inyección manual de dependencias e instancia única de BD y Repositorios
├── MainActivity.kt                     Punto de entrada y permisos de tiempo de ejecución
├── data/local/                         Room: Entidades, DAOs, AppDatabase, Migraciones, DatosEjemplo y SembradoInicial
├── data/repository/                    AuthRepository, JuegoRepository y SesionRepository
├── domain/                             Reglas de negocio puras (validadores de login, rostros, avance y rutas)
├── ml/                                 Detector de rostros con Google ML Kit
├── ubicacion/                          Proveedor de coordenadas GPS
├── navigation/                         AppNavigation.kt (NavHost, rutas protegidas por rol y barra inferior)
├── ui/
│   ├── admin/                          AdminScreen.kt y GenContScreen.kt (Paneles de gestión CRUD en SQLite)
│   ├── bienvenida/ & login/            Portada animada, selección de jugador y registro
│   ├── inicio/ & mapa/                 Dashboard del jugador y mapa osmdroid
│   ├── escaner/, capsula/ & evidencia/ Lector QR CameraX, Reproductor ExoPlayer y captura de Selfie IA
│   ├── coleccion/ & perfil/            Galería de descubrimientos, estadísticas y ranking
│   └── viewmodels/                     ViewModels reactivos (Inicio, Mapa, Escaner, Capsula, Admin, GenCont, etc.)
└── utils/Hash.kt                       Encriptación SHA-256 para contraseñas

```

## 7. Cumplimiento de Requerimientos (Estado Final)

| Requerimiento de Evaluación | Estado | Implementación | 
| ----- | ----- | ----- | 
| Base de datos local SQLite (Room) con persistencia real | ✅ **Completado** | 6 tablas relacionales, DAOs asíncronos, migraciones y sembrado inicial. | 
| Multi-perfil de clientes en el mismo teléfono | ✅ **Completado** | Selector de perfiles e historial independiente por `usuarioId`. | 
| Cuentas en duro: `Admin` (`123`) y `GenCont` (`123`) | ✅ **Completado** | Sembradas automáticamente y enrutadas a sus paneles exclusivos. | 
| Panel de Administrador y Panel de Creador de Contenido | ✅ **Completado** | `AdminScreen` (3 pestañas CRUD) y `GenContScreen` conectados a Room. | 
| Mapa de Santiago georreferenciado y rutas | ✅ **Completado** | `osmdroid` con marcadores dinámicos desde SQLite y cálculo de avance. | 
| Escaneo de QR con cámara y validación anti-duplicados | ✅ **Completado** | CameraX + ML Kit Barcode Scanning + entrada manual de respaldo. | 
| Reproducción nativa de cápsulas de video (`.mp4`) | ✅ **Completado** | `Media3 ExoPlayer` con video alojado en el repositorio y permisos de red. | 
| Evidencia fotográfica (Selfie validada con IA + Entorno) | ✅ **Completado** | Detección de rostro único en tiempo real con ML Kit Face Detection. | 
| Colección de descubrimientos, progreso y ranking | ✅ **Completado** | Pantallas reactivas sincronizadas mediante `Flow` y `StateFlow`. | 
| Notificaciones Push y Backend Centralizado (Spring Boot) | ⏭️ **Etapa 3** | Postergado oficialmente por indicación docente para la Evaluación 3. | 

## 8. Equipo de Desarrollo

* **Jordy:** Diseño e implementación de base de datos Room (entidades, DAOs, migraciones), repositorios base, lógica de dominio, servicios de GPS/ML Kit y pruebas unitarias.

* **Alejandro:** Diseño de interfaz en Jetpack Compose, flujos de navegación por roles, implementación completa de los paneles de **Administrador (`AdminScreen`)** y **Creador de Contenido (`GenContScreen`)**, integración multimedia (`ExoPlayer` + video `.mp4`), configuración y pruebas de hardware (cámara QR, selfie y despliegue en emulador API 35).

## 9. Video del proyecto

Mira la demostración de **Aprendo IA Jugando en Santiago**, donde se presentan las principales funcionalidades de la aplicación: navegación por el mapa, escaneo de códigos QR, reproducción de cápsulas educativas, registro de evidencias y sistema de puntuación.

[**▶ Ver video de demostración (Proyecto.mp4)**](./Proyecto.mp4)
