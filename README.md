# Aprendo IA Jugando en Santiago  _(versión con modo estricto activado por defecto)_

## 1. Nombre del proyecto

**Aprendo IA Jugando en Santiago** — app Android nativa (Kotlin + Jetpack Compose) de aprendizaje gamificado de
Inteligencia Artificial en entornos urbanos. El jugador recorre puntos turísticos de Santiago, **escanea códigos QR**,
mira **cápsulas en video** sobre IA, suma puntos, sube en el **ranking** y registra **evidencia fotográfica** validada
con ML Kit. Caso de estudio *Triskeledu SpA* · asignatura DSY1105 · Duoc UC. Todos los datos son sintéticos.

**Arquitectura:** MVVM. La app guarda todo en **Room/SQLite** (funciona sin internet) y se conecta con
**dos microservicios Spring Boot** (usuarios y contenido) y con **APIs externas** (clima y novedades) mediante **Retrofit**.

## 2. Integrantes

| Integrante | Responsabilidad principal |
| ----- | ----- |
| **Jordy** | Base de datos Room, repositorios, lógica de dominio, GPS/ML Kit, pruebas unitarias, integración con microservicios |
| **Alejandro** | Interfaz Compose, navegación por roles, paneles Admin y Creador de Contenido, multimedia (ExoPlayer), pruebas en emulador |

> Completar con apellidos, número de equipo y el enlace al tablero de Trello (ver sección 12).

## 3. Funcionalidades

- **Jugador:** registro e inicio de sesión (multi-perfil), mapa osmdroid con puntos y rutas, escáner QR (CameraX + ML Kit),
  video de la cápsula (Media3), puntos anti-duplicados, selfie validada con IA (+100 puntos de bonificación, una vez por punto; se guarda también en la galería del teléfono) y foto del entorno, colección, historial,
  progreso y ranking.
- **Perfil (estilo iOS):** editar datos, cambiar avatar/foto, cambiar contraseña, notificaciones, permisos (ubicación y cámara),
  ayuda y soporte, cerrar sesión.
- **Detalle del punto:** descripción, distancia, cápsulas, puntos y **clima actual** (API externa).
- **Administrador (`Admin`):** gestión de jugadores (**editar** datos y eliminar), y CRUD de puntos turísticos y cápsulas (crear, **editar**, eliminar).
- **Creador de contenido (`GenCont`):** CRUD de cápsulas.
- **Conexión con microservicios:** registro, login, perfil, progreso, ranking y contenido se envían y reciben del servidor;
  si no hay red la app avisa y sigue con los datos locales.
- **Novedades:** lista y CRUD de publicaciones desde una API REST externa (Guía 14).

## 4. Endpoints utilizados

### 4.1 Microservicios propios (Spring Boot 3.3, Java 17) — carpeta `microservicios/`

**usuarios-service** · `http://localhost:8081/api` (emulador: `10.0.2.2:8081`) · BD `usuarios`

| Método | Ruta | Uso en la app |
| ----- | ----- | ----- |
| POST | `/usuarios` | Registro de jugador (409 si el alias existe) |
| POST | `/auth/login` | Inicio de sesión |
| GET | `/usuarios`, `/usuarios/{id}` | Listar / obtener |
| PUT | `/usuarios/{id}` | Editar perfil |
| PUT | `/usuarios/{id}/clave` | Cambiar contraseña |
| DELETE | `/usuarios/{id}` | Eliminar jugador (panel Admin) |
| GET | `/ranking` | Ranking |
| POST | `/usuarios/{id}/progreso/{capsulaId}` | Registrar cápsula completada (idempotente) |
| GET | `/usuarios/{id}/progreso` | Cápsulas completadas |

**contenido-service** · `http://localhost:8082/api` (emulador: `10.0.2.2:8082`) · BD `contenido`

| Método | Ruta | Uso en la app |
| ----- | ----- | ----- |
| GET / POST | `/puntos` | Listar / crear punto |
| GET / PUT / DELETE | `/puntos/{id}` | Ver / editar / borrar punto |
| GET | `/puntos/{id}/capsulas` | Cápsulas de un punto |
| GET / POST | `/capsulas` | Listar / crear cápsula (QR único, 409 si se repite) |
| GET / PUT / DELETE | `/capsulas/{id}` | Ver / editar / borrar cápsula |
| GET | `/capsulas/qr/{codigo}` | Buscar por QR |

Errores con formato `{"estado":404,"mensaje":"..."}`. Detalle completo en `microservicios/README.md`.

### 4.2 APIs externas (Retrofit)

| API | Endpoint | Uso |
| ----- | ----- | ----- |
| Open-Meteo | `GET https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,apparent_temperature,wind_speed_10m,weather_code` | Sección "Clima ahora" en el Detalle del punto |
| JSONPlaceholder | `GET/POST/PUT/PATCH/DELETE https://jsonplaceholder.typicode.com/posts` | Pantalla Novedades (Perfil → Ayuda → Novedades) |

Cada API usa su propio cliente Retrofit; ninguna escribe en Room ni se mezcla con los microservicios.

## 5. Pasos para ejecutar

**Requisitos:** Android Studio reciente (AGP 9.x), JDK 17+, Maven (o el de IntelliJ), emulador Pixel 7 API 35 o teléfono Android 7+.

1. **Microservicios** (dos terminales):
   ```
   cd microservicios/usuarios-service  && mvn spring-boot:run     # puerto 8081
   cd microservicios/contenido-service && mvn spring-boot:run     # puerto 8082
   ```
   Usan H2 en archivo (sin instalar nada). Para MySQL: `docker compose up -d` en `microservicios/` y agregar
   `-Dspring-boot.run.arguments=--spring.profiles.active=mysql`.
2. **App:** abrir la carpeta en Android Studio → *Gradle Sync* → elegir el emulador → **Run ▶**.
   Teléfono real: `./gradlew assembleDebug -PurlUsuarios=http://IP_DEL_PC:8081/api/ -PurlContenido=http://IP_DEL_PC:8082/api/`
   (teléfono y PC en la misma red wifi).
3. **Ingresar** con `Admin/123`, `GenCont/123`, `demo/demo1234` o registrar un jugador nuevo (sección 7).
4. **Pruebas:** `./gradlew test` (app) y `mvn test` (cada microservicio). Cobertura: ver `docs/PRUEBAS_Y_COBERTURA.md`.
5. **APK firmado:** ver `docs/GUIA_APK_FIRMADO.md` (`keytool` → `keystore.properties` → `./gradlew assembleRelease`).
   El APK queda en `app/build/outputs/apk/release/app-release.apk`; cópialo a `entrega/` junto al `.jks`.

> Si cambia el esquema de Room o la URL del video de ejemplo, desinstala la app del emulador antes de volver a ejecutar.

## 6. Captura del APK firmado y del .jks

> Reemplazar con las capturas reales (ver lista en `docs/capturas/LEEME.md`).

| Evidencia | Archivo |
| ----- | ----- |
| APK firmado generado (`app-release.apk` en la carpeta de salida) | `docs/capturas/apk_firmado.png` |
| Llave `.jks` creada | `docs/capturas/jks.png` |
| `./gradlew signingReport` mostrando la firma release | `docs/capturas/signing_report.png` |

![APK firmado](docs/capturas/apk_firmado.png)
![Llave .jks](docs/capturas/jks.png)

---

## 7. Roles del sistema y cuentas de acceso

La aplicación inicia en la portada animada de **Bienvenida** y da paso al selector **¿Quién juega hoy?**, permitiendo alternar entre múltiples perfiles registrados en el mismo teléfono o iniciar sesión según el rol requerido por el caso de estudio:

| Rol | Usuario / Alias | Contraseña | Funcionalidades Implementadas | 
| ----- | ----- | ----- | ----- | 
| **Administrador** *(En duro)* | `Admin` | `123` | Acceso exclusivo al **Panel de Administración (3 pestañas)**: gestión en tiempo real sobre SQLite de **Jugadores** (crear/eliminar), **Puntos Turísticos** (crear puntos georreferenciados con coordenadas/eliminar) y **Cápsulas** (crear/eliminar). | 
| **Creador de Contenido** *(En duro)* | `GenCont` | `123` | Acceso exclusivo al **Panel de Creador de Contenido**: alta, clasificación y eliminación de cápsulas educativas en video asociadas a códigos QR, puntos turísticos y puntajes. | 
| **Jugador de Prueba** | `demo` | `demo1234` | Perfil de cliente pre-cargado para recorrer el flujo completo del juego. | 
| **Nuevos Jugadores (Clientes)** | *A elección* | *A elección* | Registro local con validación de formulario, confirmación de contraseña y selección de avatar. Cada perfil mantiene su propio historial, colección y puntaje aislado en SQLite. | 

*Seguridad:* El inicio de sesión no distingue mayúsculas/minúsculas en el alias y las contraseñas jamás se almacenan en texto plano; se persiste su **hash SHA-256**.

**Perfiles sintéticos adicionales para el Ranking:** Camila R. (320 pts), Matías P. (270 pts), Valentina S. (210 pts), Diego T. (150 pts), Sofía L. (90 pts).

## 8. Puntos Georreferenciados y Códigos QR Oficiales

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

## 9. Flujo Completo de la Aplicación

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

## 10. Base de Datos Local (Room / SQLite)

Base de datos relacional local `aprendo_ia_santiago.db` gestionada con **Room**, **Corrutinas (`suspend`)** y **`Flow`** para actualización reactiva de la UI sin bloquear el hilo principal (*Main Thread*).

| Tabla (`@Entity`) | Descripción | Campos Principales | 
| ----- | ----- | ----- | 
| `usuarios` | Perfiles de jugadores y roles | `id`, `nombreUsuario` (UNIQUE), `claveHash` (SHA-256), `nombreVisible`, `puntaje` | 
| `puntos_turisticos` | Hitos georreferenciados de Santiago | `id`, `nombre`, `descripcion`, `latitud`, `longitud`, `altitud` | 
| `capsulas` | Videos cortos asociados a cada QR | `id`, `puntoId` (FK), `titulo`, `urlVideo`, `codigoQr` (UNIQUE), `puntos` | 
| `progreso` | Historial por jugador y evidencias | `usuarioId` + `capsulaId` (PK compuesta), `completada`, `fechaMillis`, `latitud`, `longitud`, `altitud`, `selfieUri`, `fotoEntornoUri` | 
| `rutas` / `ruta_puntos` | Recorridos turísticos sugeridos | `id`, `nombre`, `descripcion` / `rutaId` + `puntoId`, `orden` |
| `notificaciones` | Avisos por jugador | `id`, `usuarioId` (FK, borrado en cascada), mensaje, leída |

### Bases de datos de los microservicios (servidor)

| Servicio | Motor por defecto | Archivo | Motor alternativo | Consola |
| ----- | ----- | ----- | ----- | ----- |
| usuarios-service | H2 en archivo | `microservicios/usuarios-service/data/usuarios` | MySQL `usuarios_db` (perfil `mysql`) | `http://localhost:8081/h2-console` (JDBC `jdbc:h2:file:./data/usuarios`, usuario `sa`, sin clave) |
| contenido-service | H2 en archivo | `microservicios/contenido-service/data/contenido` | MySQL `contenido_db` (perfil `mysql`) | `http://localhost:8082/h2-console` (JDBC `jdbc:h2:file:./data/contenido`) |

Las tablas las crea JPA/Hibernate automáticamente (`ddl-auto: update`) a partir de las entidades; no hay scripts SQL manuales.
Al primer arranque se cargan datos sintéticos (`DatosIniciales`). Para ver la base local de Room en Android Studio: *View > Tool Windows > App Inspection > Database Inspector*.

## 11. Arquitectura del Proyecto (MVVM)

```
UI (Jetpack Compose)  ──observa──►  ViewModel (StateFlow)  ──consume──►  Repositorios  ──┬──►  DAO (Room / SQLite)  [base local, funciona sin red]
                                                                                    ├──►  Retrofit → microservicios Spring Boot (8081 / 8082)
                                                                                    └──►  Retrofit → APIs externas (Open-Meteo, JSONPlaceholder)

```

```
app/src/main/java/com/example/aprendoiasantiago/
├── AprendoApp.kt / AppContainer.kt     Inyección manual de dependencias e instancia única de BD y Repositorios
├── MainActivity.kt                     Punto de entrada y permisos de tiempo de ejecución
├── data/local/                         Room: Entidades, DAOs, AppDatabase, Migraciones, DatosEjemplo y SembradoInicial
├── data/repository/                    Auth, Perfil, Juego, Notificaciones, Sesion + ServidorRepository (microservicios), ClimaRepository y PostRepository (APIs externas)
├── data/remote/                        Retrofit: MicroserviciosApi (usuarios y contenido), ClimaApi (Open-Meteo), ApiService (JSONPlaceholder)
├── domain/                             Reglas de negocio puras (validadores de login, rostros, avance y rutas)
├── ml/                                 Detector de rostros con Google ML Kit
├── ubicacion/                          Proveedor de coordenadas GPS
├── navigation/                         AppNavigation.kt (NavHost, rutas protegidas por rol y barra inferior)
├── ui/
│   ├── admin/ & gencont/               Paneles CRUD de Administrador y Creador de Contenido (con diálogos de edición)
│   ├── punto/ & screens/               Detalle del punto (con clima), catálogo, historial, progreso y novedades
│   ├── bienvenida/ & login/            Portada animada, selección de jugador y registro
│   ├── inicio/ & mapa/                 Dashboard del jugador y mapa osmdroid
│   ├── escaner/, capsula/ & evidencia/ Lector QR CameraX, Reproductor ExoPlayer y captura de Selfie IA
│   ├── coleccion/ & perfil/            Galería de descubrimientos, estadísticas y ranking
│   └── viewmodels/                     ViewModels reactivos (Inicio, Mapa, Escaner, Capsula, Admin, GenCont, etc.)
└── utils/Hash.kt                       Encriptación SHA-256 para contraseñas

```

### Estructura del repositorio

```
├── app/                 App Android (Kotlin + Compose)
│   └── src/test, src/androidTest   Pruebas unitarias y de interfaz
├── microservicios/      usuarios-service (8081) y contenido-service (8082), Spring Boot
├── docs/                Plan de la evaluación, pruebas/cobertura, guía de APK firmado, paso a paso de entrega
│   ├── capturas/        Evidencias (APK, .jks, tests, Trello, GitHub)
│   ├── media/           Videos del proyecto
│   └── historico/       README antiguo (solo referencia)
├── entrega/             APK firmado y .jks
└── keystore.properties.example
```

## 12. Cumplimiento de la Evaluación Parcial 4

| Requisito | Estado | Dónde está |
| ----- | ----- | ----- |
| Interfaz completa y funcional | ✅ | `ui/` · navegación en `navigation/AppNavigation.kt` |
| Microservicios Spring Boot con BD y endpoints | ✅ código · ⏳ ejecutar y capturar | `microservicios/` |
| App integrada con CRUD en tiempo real | ✅ código · ⏳ probar con los servicios arriba | `data/remote/MicroserviciosApi.kt`, `ServidorRepository`, Admin/GenCont |
| API externa con Retrofit visible en la UI | ✅ | Clima (Detalle del punto) y Novedades |
| Pruebas unitarias ≥ 80 % de la lógica | ✅ escritas · ⏳ medir % | `app/src/test`, `docs/PRUEBAS_Y_COBERTURA.md` |
| APK firmado + `.jks` en el repositorio | 🔶 configuración lista · ⏳ generar | `docs/GUIA_APK_FIRMADO.md`, `app/build.gradle.kts` |
| GitHub + Trello con trabajo por integrante | 👤 equipo | `docs/capturas/LEEME.md` |

**Trello:** tablero con listas *Por hacer / En curso / Finalizado*, tarjetas por tarea asignadas a cada integrante y enlace aquí: `(pegar enlace)`.
**GitHub:** repositorio público con acceso al docente; commits de cada integrante tanto en la app como en `microservicios/`.


## Modo estricto (sin microservicio de usuarios no hay login)

Por defecto la app sigue funcionando sin conexión (usa Room como respaldo). Con el **modo estricto** el inicio de sesión
y el registro dependen del microservicio de usuarios: si no responde, la app muestra
*"No hay conexión con el servidor de usuarios. Enciende el microservicio e inténtalo de nuevo."* y no deja entrar.

- Activarlo: `./gradlew assembleDebug -PmodoEstricto=true` (en Android Studio: agregar `modoEstricto=true` en `gradle.properties`).
- Desactivarlo: quitar la línea o usar `-PmodoEstricto=false`.
- Una clave incorrecta sigue mostrando "Usuario o contraseña incorrectos"; el aviso de servidor solo sale si el servicio no responde.
- Con el servicio de usuarios arriba y sin red hacia el resto, el contenido (puntos y cápsulas) sigue leyéndose de la base local.
