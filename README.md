# Aprendo IA Jugando en Santiago

App Android (MVP) de aprendizaje gamificado de Inteligencia Artificial en la ciudad. El jugador recorre puntos turísticos de Santiago, **escanea el código QR** de cada punto, **mira una cápsula en video** sobre IA, suma puntos, sube en un ranking y deja **evidencia** (selfie y foto del entorno).

Proyecto del caso *Aprendo IA Jugando en Santiago* (Triskeledu SpA, DSY1105).
Funciona **sin servidor**: todo se guarda en el propio teléfono y los datos son **sintéticos** (inventados para la demostración).

---

## 1. Qué se necesita y cómo abrir el proyecto

| Requisito | Detalle |
|---|---|
| Android Studio | Versión reciente. Si avisa que el plugin de Gradle (AGP 9.4.1) no es compatible, actualiza Android Studio o baja `agp` a `9.3.0` en `gradle/libs.versions.toml` |
| JDK | El que trae Android Studio (no hace falta instalar otro) |
| Teléfono | Android 7.0 o superior (`minSdk 24`), con cámara y GPS |
| Internet | Solo para bajar las teselas del mapa y reproducir los videos |

**Pasos**
1. Descomprime el proyecto y en Android Studio elige **Open** y selecciona la carpeta `AprendoIASantiago`.
2. Espera el **Gradle Sync** (la primera vez descarga librerías).
3. En el teléfono activa **Opciones de desarrollador → Depuración USB**, conéctalo y selecciónalo arriba.
4. Pulsa **Run ▶**. No hay Play Store ni APK publicado: se instala por depuración.
5. Pruebas automáticas: `./gradlew test` (o clic derecho sobre `app/src/test` → *Run Tests*).

> Si la app falla justo después de actualizar una versión que cambia la base de datos, **desinstálala del teléfono** y vuelve a instalar (se explica en la sección de base de datos).

---

## 2. Cuentas y datos de acceso

La app abre en una portada de **Bienvenida** y luego en **¿Quién juega hoy?**, donde se elige un perfil o se escribe usuario y contraseña. También se puede **crear un perfil nuevo** (nombre, alias, contraseña, confirmar contraseña y avatar).

| Perfil | Usuario | Contraseña | Para qué sirve |
|---|---|---|---|
| Jugador de prueba | `demo` | `demo1234` | Probar el juego completo |
| Administrador | `Admin` | `123` | Cuenta del rol Administrador (ver *Estado*: aún sin funciones propias) |
| Generador de contenido | `GenCont` | `123` | Cuenta del rol Generador de contenido (ver *Estado*: aún sin funciones propias) |
| Jugadores creados en la app | el alias que elijas | la que elijas | Se guardan en el teléfono |

El usuario no distingue mayúsculas de minúsculas. Las contraseñas **no se guardan en texto**: se guarda su hash SHA-256.

**Usuarios solo para el ranking** (no pueden iniciar sesión, no tienen contraseña): Camila R. (320 pts), Matías P. (270), Valentina S. (210), Diego T. (150), Sofía L. (90).

### Puntos turísticos y códigos QR de prueba

| # | Punto | Código QR (texto) | Cápsula | Pts |
|---|---|---|---|---|
| 1 | Plaza de Armas | `AIJS-001` | ¿Qué es la Inteligencia Artificial? | 10 |
| 2 | Cerro Santa Lucía | `AIJS-002` | Cómo aprende una máquina | 10 |
| 3 | Cerro San Cristóbal | `AIJS-003` | Reconocimiento de imágenes | 15 |
| 4 | Palacio de La Moneda | `AIJS-004` | La IA y los datos abiertos | 10 |
| 5 | Centro Cultural GAM | `AIJS-005` | Cómo entiende una IA la voz y el idioma | 15 |
| 6 | Mercado Central | `AIJS-006` | La IA en la vida diaria | 10 |
| 7 | Museo Nacional de Bellas Artes | `AIJS-007` | Cuando la IA se equivoca: sesgos y errores | 15 |

- Un QR de prueba es cualquier código QR cuyo **texto** sea exactamente uno de esos (por ejemplo `AIJS-001`). Se puede generar con cualquier generador de QR gratuito.
- Si no se puede escanear (emulador, cámara dañada), la pantalla del escáner tiene un campo para **escribir el código a mano**.
- Los videos de todas las cápsulas apuntan a un video público de muestra (Big Buck Bunny). Las coordenadas y altitudes son aproximadas. **Hay que reemplazar** videos, coordenadas y QR por los oficiales del profesor/cliente (archivo `DatosEjemplo.kt`).

**Rutas de ejemplo**
- *Centro histórico*: La Moneda → Plaza de Armas → Mercado Central → Bellas Artes.
- *Cerros y cultura*: Santa Lucía → GAM → San Cristóbal.

---

## 3. Cómo se usa la app (flujo del jugador)

1. **Bienvenida → Login/Registro.** Elige un perfil, o crea uno nuevo (la contraseña se pide dos veces y deben coincidir).
2. **Inicio.** Resumen del jugador: puntaje y cápsulas completadas, con atajos al mapa, escáner y colección.
3. **Mapa.** Mapa de OpenStreetMap con un marcador por punto turístico y la ubicación del jugador. Los puntos **completados se ven en verde** y al tocar un marcador se ve su descripción, coordenadas y estado (*pendiente / completado, X de Y cápsulas*). Arriba hay botones de **rutas**: al elegir una se dibuja la línea, se numeran las paradas y se muestra el avance (por ejemplo `Centro histórico (1/4)`).
4. **Escáner QR.** Apunta la cámara al QR del punto:
   - Si el código es válido y es la **primera vez**: la cápsula queda *completada*, se suman los puntos, y se guarda **fecha y ubicación (latitud, longitud, altitud)** del jugador.
   - Si ya estaba completada: no se suman puntos otra vez.
   - Si el código no existe: aparece "Código no reconocido".
   - Desde el resultado se puede **ver la cápsula** (video) o **tomar evidencia**.
5. **Evidencia.** Dos pasos: (1) **selfie** con la cámara frontal, que se valida en el teléfono con ML Kit (debe haber **un solo rostro** y no verse muy lejos); (2) **foto del entorno** con la cámara trasera. Cada foto se guarda apenas es válida, así que se puede retomar donde se dejó.
6. **Colección.** Las cápsulas que el jugador ya completó, con su lugar y puntos, y acceso para volver a ver el video.
7. **Perfil.** Datos del jugador, progreso, **ranking** y cierre de sesión.

---

## 4. Base de datos

Se usa **Room (SQLite)** local. Archivo: `aprendo_ia_santiago.db`, dentro del almacenamiento privado de la app. No hay nube.

### Tablas

| Tabla | Qué guarda | Campos principales |
|---|---|---|
| `usuarios` | Jugadores y cuentas | `id`, `nombreUsuario` (único), `claveHash` (SHA-256; vacío = no puede iniciar sesión), `nombreVisible`, `puntaje` |
| `puntos_turisticos` | Lugares del mapa | `id`, `nombre`, `descripcion`, `latitud`, `longitud`, `altitud` |
| `capsulas` | Un video por punto | `id`, `puntoId` → punto, `titulo`, `urlVideo`, `codigoQr` (único), `puntos` |
| `progreso` | Avance de cada usuario por cápsula | `usuarioId` + `capsulaId` (clave compuesta), `completada`, `fechaMillis`, `latitud`, `longitud`, `altitud`, `selfieUri`, `fotoEntornoUri` |
| `rutas` | Recorridos temáticos | `id`, `nombre`, `descripcion` |
| `ruta_puntos` | Paradas de una ruta | `rutaId` + `puntoId`, `orden` |

**Estados:** una cápsula está *pendiente* si no existe fila en `progreso` para ese usuario, y *completada* si existe con `completada = 1`. Un **punto** está completado cuando el usuario completó todas sus cápsulas.

### Cómo se usa

- **Primera vez que se abre la app** (`SembradoInicial`): se cargan puntos, cápsulas, rutas y usuarios de ejemplo.
- **En cada inicio**: puntos, cápsulas y rutas se *actualizan* (`@Upsert`), así que si cambias `DatosEjemplo.kt` la app toma los cambios **sin perder el progreso** de nadie. Los usuarios existentes **no** se reinician.
- **Completar una cápsula** es una **transacción**: se inserta el progreso (`INSERT IGNORE`) y, solo si era nuevo, se suman los puntos. Así nunca se suman dos veces ni queda a medias.
- **Las fotos** no van dentro de la base: se guardan como archivos JPG en la carpeta privada `evidencias/` y en `progreso` solo queda su ruta.
- La pantalla lee con **Flows**: cuando algo cambia en la base (por ejemplo, completas una cápsula), el mapa, el perfil y la colección se actualizan solos.

### Versiones y migraciones

| Versión | Cambio |
|---|---|
| 1 | Usuarios, puntos, cápsulas y progreso básico |
| 2 | `progreso` gana fecha, ubicación y evidencias (migración con `ALTER TABLE`, conserva datos) |
| 3 | Tablas `rutas` y `ruta_puntos` (migración que solo crea tablas) |

Si cambias el esquema, sube `version` en `AppDatabase.kt` y escribe una `Migration`. Para empezar de cero basta **desinstalar la app** (o *Ajustes → Apps → Almacenamiento → Borrar datos*).

### Ver la base en Android Studio
**View → Tool Windows → App Inspection → Database Inspector**, con la app corriendo en el teléfono.

---

## 5. Cómo está hecho (arquitectura)

Kotlin + **Jetpack Compose**, con capas separadas e **inyección manual** de dependencias (sin librerías extra):

```
UI (Compose)  →  ViewModel (StateFlow)  →  Repositorio  →  DAO (Room)  →  SQLite
```

```
app/src/main/java/com/example/aprendoiasantiago/
├── AprendoApp.kt / AppContainer.kt     Crea la base y los repositorios (una sola vez)
├── MainActivity.kt                     Punto de entrada
├── data/local/                         Room: entidades, DAOs, base, datos de ejemplo, sembrado, almacén de fotos
├── data/repository/                    Auth, Juego (puntos, QR, rutas, evidencia), Sesión
├── domain/                             Reglas puras y fáciles de probar: validador de login, avance por punto, rutas, validador de rostro
├── ml/                                 Detector de rostros (ML Kit)
├── ubicacion/                          Obtiene la ubicación del GPS
├── navigation/                         Rutas y barra inferior (5 pestañas)
├── ui/                                 Pantallas: bienvenida, login, inicio, mapa, escáner, evidencia, cápsula, colección, perfil, tema
├── ui/viewmodels/                      Estado de cada pantalla
└── utils/Hash.kt                       SHA-256
```

**Librerías** (todas gratuitas): Compose + Material 3, Navigation Compose, Room + KSP, **osmdroid** (mapa OpenStreetMap, sin API key), **CameraX**, **ML Kit** (lectura de QR y detección de rostros, en el teléfono), **Media3/ExoPlayer** (video).

**Detalles útiles**
- El mapa cuesta **$0**. OpenStreetMap exige un *User-Agent* propio (definido en `MapaScreen.kt`) y mostrar "© OpenStreetMap contributors" (ya aparece en el mapa). No usar para descargas masivas.
- Se pide permiso de **cámara** (escáner y evidencia) y de **ubicación** (mapa y registro de dónde se completó cada cápsula). Si el jugador rechaza la ubicación, la cápsula igual se completa, sin ubicación guardada.
- Interfaz pensada para **uso al aire libre**: colores de alto contraste, botones grandes y el estado siempre escrito (no solo con color).

---

## 6. Pruebas

`./gradlew test` ejecuta pruebas unitarias (sin teléfono) sobre: hash de contraseñas, validación de login y registro (incluida la confirmación de contraseña), coherencia de los datos de ejemplo, puntos y evidencia al escanear (con DAOs falsos en memoria), avance por punto, rutas y validación de rostro.

Lo que **no** cubren (se prueba a mano en el teléfono): cámara, GPS, mapa, video y reconocimiento real de rostros.

---

## 7. Estado frente al caso

| Requisito del caso | Estado |
|---|---|
| App autónoma, BD local SQLite, datos sintéticos | Hecho |
| Mapa interactivo gratuito con puntos turísticos | Hecho |
| Escaneo de QR con cámara | Hecho |
| Cápsulas en video | Hecho (video de muestra) |
| Estados pendiente / completado | Hecho (mapa, perfil, colección) |
| Puntos y ranking | Hecho |
| Selfie validada + foto del entorno | Hecho |
| Georreferenciación (lat, lon, alt) al completar | Hecho |
| Rutas | Hecho (2 rutas de ejemplo) |
| Uso de GPS en segundo plano | **Pendiente** (servicio en primer plano + permiso de ubicación en segundo plano) |
| Perfiles Generador de contenido y Administrador | **Parcial**: existen las cuentas y el login, pero todavía no tienen funciones propias (crear/editar cápsulas, puntos y QR) |
| Videos, coordenadas y QR oficiales | **Pendiente**: hay que pedirlos al profesor/cliente |
| Cierre: pulido de diseño, manual de pruebas, checklist | **Pendiente** |

---

## 8. Reparto del trabajo: Jordy y Alejandro

> **Nota importante:** casi todo el proyecto se desarrolló de forma **secuencial**, sin dividir tareas entre las dos personas: se fue avanzando etapa por etapa sobre el mismo código. El reparto de abajo **no describe quién escribió cada línea**, sino cómo se **dividen las responsabilidades** del proyecto para explicarlo, mantenerlo y terminar lo que falta.

### Jordy: datos, lógica y servicios
- **Base de datos:** entidades, DAOs, versiones y migraciones, datos de ejemplo y sembrado (`data/local`).
- **Repositorios y reglas:** `data/repository`, `domain` (login y registro, avance por punto, rutas, validador de rostro).
- **Servicios del teléfono:** ubicación (`ubicacion`), detección de rostros (`ml`), almacén de fotos.
- **Pruebas automáticas** (`src/test`).
- **Pendiente a su cargo:** GPS en segundo plano; datos de las cuentas Administrador y Generador de contenido y la lógica de crear/editar contenido.

### Alejandro: interfaz y experiencia
- **Pantallas y tema:** bienvenida, login/registro, inicio, colección, perfil, colores, tipografía (`ui/*`, `ui/theme`).
- **Mapa, escáner y evidencia (parte visual):** marcadores, botones de rutas, cámara, pasos de selfie y foto.
- **Navegación, textos y textos de error** (`navigation`, `strings.xml`).
- **Pruebas en teléfono** (cámara, GPS, mapa, video) y registro de fallos.
- **Pendiente a su cargo:** pantallas de los perfiles Administrador y Generador de contenido; pulido de diseño; capturas de pantalla.

### En conjunto
- Pedir al profesor/cliente los videos, QR y coordenadas oficiales y cargarlos en `DatosEjemplo.kt`.
- Manual de pruebas, checklist contra el caso, informe y presentación.

---

## 9. Problemas conocidos y consejos

- **El mapa muestra "Access blocked":** OpenStreetMap bloqueó el *User-Agent*. Cambia `USER_AGENT` y `CARPETA_CACHE` en `MapaScreen.kt`.
- **Un QR no se detecta:** acércate, evita reflejos, o usa el campo para escribir el código.
- **La selfie es rechazada:** debe verse **solo un rostro**, de frente y con buena luz.
- **El video no carga:** revisa la conexión; las cápsulas usan un video de muestra en internet.
- **Error de base de datos tras actualizar:** desinstala la app del teléfono y vuelve a instalarla.
- Código sin usar: `ui/screens/MainScreen.kt` no se utiliza en ningún lado y puede borrarse.
