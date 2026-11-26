# Paso a paso para cerrar la entrega (todo lo que va fuera del código)

Marca cada casilla al terminar y sigue el orden 1 → 10. Si un paso falla, no sigas: copia el error completo y pídele ayuda a Claude.
Cuentas de prueba: `Admin/123`, `GenCont/123`, `demo/demo1234`.
📸 = captura de pantalla que debes guardar en `docs/capturas/` con ese nombre exacto (sin editar, sin mostrar contraseñas).

---

## 1. Preparar el computador (30 min)
- [ ] Instalar/verificar: **Android Studio** reciente, **JDK 17** (`java -version`), **Maven 3.9+** (`mvn -v`), **Git** (`git --version`).
- [ ] Crear un emulador: *Device Manager > Create device > Pixel 7 > API 35*.
- [ ] Descomprimir `AprendoIASantiago_ep4_final.zip` en una ruta **sin espacios ni tildes** (ej. `C:\proyectos\AprendoIASantiago`).
- [ ] Cada integrante debe tener su propia copia clonada desde GitHub más adelante (paso 8); por ahora basta una.

## 2. Compilar la app (nada se ha compilado todavía)
1. Android Studio → *Open* → carpeta del proyecto → esperar *Gradle Sync*.
2. *Build > Make Project*. Debe terminar sin errores.
3. Si falla: copiar el mensaje completo (pestaña *Build*) y enviarlo. Es lo más probable que necesite arreglos menores.
4. Si la app se cierra al abrir tras cambios en la base de datos: desinstalarla del emulador y volver a ejecutar.

## 3. Levantar los microservicios
1. Terminal 1: `cd microservicios/usuarios-service` → `mvn spring-boot:run` → puerto **8081**.
2. Terminal 2: `cd microservicios/contenido-service` → `mvn spring-boot:run` → puerto **8082**.
3. Comprobar en el navegador:
   - `http://localhost:8081/api/ranking` → JSON con jugadores.
   - `http://localhost:8082/api/puntos` → JSON con 7 puntos.
4. Ver las bases: `http://localhost:8081/h2-console` (JDBC `jdbc:h2:file:./data/usuarios`, usuario `sa`, clave vacía); igual en 8082 con `./data/contenido`.
5. 📸 `microservicios.png`: las dos terminales corriendo + el JSON en el navegador.
6. Dejar ambas terminales abiertas durante las pruebas del paso 4.
7. (Opcional) Con Docker: ver `docs/PASO_A_PASO_DOCKER.md`.

## 4. Probar la app conectada (checklist de la demostración)
Con los servicios arriba, ejecutar la app en el emulador (usa `10.0.2.2` automáticamente).

**Jugador**
- [ ] Bienvenida → Comenzar → Selección de jugador. Registrar un jugador nuevo → verificar que aparece en `http://localhost:8081/api/usuarios`.
- [ ] Entrar con `demo/demo1234` (pide contraseña al seleccionar el perfil).
- [ ] Mapa: pines **azules** (pendientes), tu ubicación (punto azul); tocar un pin abre el Detalle.
- [ ] Detalle del punto: aparece "Clima ahora" (API externa; el emulador necesita internet).
- [ ] Escáner QR: leer un código `AIJS-001`…`AIJS-007` (puedes mostrar el texto en un generador de QR en la pantalla del PC). Suma puntos; escanear el mismo código otra vez **no** suma.
- [ ] Video de la cápsula se reproduce.
- [ ] **Selfie** (Detalle > evidencia): tomar selfie válida → suma **+100 puntos** (una sola vez por punto); revisar que la foto aparece en la galería del emulador (carpeta *AprendoIA*).
- [ ] Colección, Historial y Progreso muestran los datos solo del jugador activo.
- [ ] Perfil: editar datos, cambiar contraseña, notificaciones, permisos, cerrar sesión → los cambios aparecen en el servidor.
- [ ] Perfil → Ayuda → Novedades: listar, crear, editar y borrar publicaciones (API externa).
- [ ] Cambiar de jugador: toda la información cambia al nuevo perfil.

**Administrador (`Admin/123`)**
- [ ] Pestaña Jugadores: **editar** (nombre/correo/teléfono) y eliminar un jugador de prueba.
- [ ] Pestaña Puntos: crear, **editar** y eliminar un punto → verificar en `http://localhost:8082/api/puntos`.
- [ ] Pestaña Cápsulas: crear (QR único), editar y eliminar → verificar en `http://localhost:8082/api/capsulas`.
- [ ] 📸 `app_crud.png`: panel Admin con el cambio + el dato visible en el servicio.

**Creador de contenido (`GenCont/123`)**
- [ ] Crear, editar y eliminar una cápsula.

**Sin conexión**
- [ ] Apagar los microservicios (Ctrl+C) y repetir una acción: la app avisa (mensaje inferior) y sigue funcionando con los datos locales.

📸 Además, 3 capturas de pantallas de la app (mapa, escáner/resultado, perfil) → `app_mapa.png`, `app_escaner.png`, `app_perfil.png`.

### 4b. Teléfono real (opcional)
1. PC y teléfono en el mismo wifi; permitir los puertos 8081 y 8082 en el firewall.
2. Averiguar la IP del PC (`ipconfig` en Windows, `ifconfig` en Mac/Linux).
3. `./gradlew assembleDebug -PurlUsuarios=http://IP_DEL_PC:8081/api/ -PurlContenido=http://IP_DEL_PC:8082/api/` e instalar el APK.

## 5. Ejecutar pruebas y medir la cobertura (requisito: ≥ 80 %)
1. App: `./gradlew test` → debe terminar en verde. Informe: `app/build/reports/tests/testDebugUnitTest/index.html`. 📸 `tests_app.png`.
2. Microservicios: `mvn test` en `usuarios-service` y en `contenido-service`. 📸 `tests_microservicios.png`.
3. Pruebas de interfaz (emulador encendido): `./gradlew connectedDebugAndroidTest`.
4. Cobertura: clic derecho en `app/src/test` → *Run Tests in … with Coverage*. En el panel *Coverage* revisar `domain`, `data.repository`, `ui.viewmodels`, `utils`. 📸 `cobertura.png`.
5. Si algo queda bajo 80 %: anotar los nombres de las clases y pedirle a Claude que agregue pruebas.

## 6. APK firmado y llave `.jks`
1. Crear la carpeta `claves/` en la raíz del proyecto.
2. Generar la llave (una sola vez; **anota las contraseñas**):
   ```
   keytool -genkeypair -v -keystore claves/aprendoia-release.jks -alias aprendoia -keyalg RSA -keysize 2048 -validity 9125
   ```
   (`keytool` viene con el JDK. Alternativa gráfica: *Build > Generate Signed Bundle/APK > APK > Create new*.)
3. Copiar `keystore.properties.example` como `keystore.properties` y completar `storePassword` y `keyPassword` (el archivo no se sube a Git).
4. Compilar el release: `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`.
5. Verificar la firma: `./gradlew signingReport` → 📸 `signing_report.png`.
6. Probarlo: `adb install -r app/build/outputs/apk/release/app-release.apk`, abrirlo y recorrer login, mapa y escáner. Si se cierra solo, avisar (puede faltar una regla de optimización).
7. Copiar **`app-release.apk`** y **`aprendoia-release.jks`** a la carpeta `entrega/`.
8. 📸 `apk_firmado.png` (carpeta con el APK) y `jks.png` (el `.jks` en su carpeta, sin contraseñas).
9. Usar claves propias del curso; no reutilizar contraseñas personales. `keystore.properties` NO se sube; el `.jks` de `entrega/` SÍ (ya hay excepción en `.gitignore`).

## 7. Completar el README (antes de subir a GitHub)
- [ ] Sección 2: apellidos de ambos y número de equipo.
- [ ] Sección 6: confirmar que existen `docs/capturas/apk_firmado.png`, `jks.png`, `signing_report.png`.
- [ ] Sección 12: pegar enlaces de Trello y GitHub (después de los pasos 8 y 9).

## 8. GitHub: commits por integrante (requisito explícito)
1. Jordy crea el repositorio **público** (ej. `AprendoIASantiago`) y agrega a Alejandro como colaborador (y al docente, según indique).
2. Cada integrante configura su identidad (el correo debe estar asociado a su cuenta de GitHub, si no sus commits no cuentan como suyos):
   ```
   git config user.name "Nombre Apellido"
   git config user.email "correo-de-su-cuenta@..."
   ```
3. Subir **por partes y desde el computador de cada uno**, con mensajes claros. Sugerencia de reparto:
   - **Jordy:** `microservicios/` (usuarios y contenido), `data/remote`, repositorios, pruebas unitarias, firma del APK.
   - **Alejandro:** pantallas Compose, navegación, paneles Admin/GenCont, mapa e iconos, README y capturas.
4. Ejemplo de commits: `feat: microservicio de usuarios`, `feat: integración Retrofit con contenido`, `test: pruebas de ViewModels`, `docs: README final`.
5. Verificar en GitHub: *Insights > Contributors* muestra a ambos. 📸 `github.png`.
6. Revisar que **no** se subieron `keystore.properties`, `local.properties`, `build/` ni `target/`, y que **sí** está `entrega/` con APK y `.jks`.
7. Muy importante: **no hacer cambios al repositorio después de la fecha límite** (la pauta asigna nota 1.0 si hay cambios posteriores).

## 9. Trello: planificación distribuida
1. Crear un tablero con listas **Por hacer / En curso / Finalizado**.
2. Una tarjeta por tarea, asignada a Jordy o a Alejandro, con checklist y fecha. Ejemplos: Microservicio usuarios · Microservicio contenido · Integración Retrofit · API del clima · CRUD Admin · Pruebas ViewModel · Pruebas Repository · APK firmado · README y evidencias.
3. Mover las tarjetas a su lista real y adjuntar enlaces a commits o capturas.
4. Dar acceso al docente o dejar el tablero visible por enlace.
5. 📸 `trello.png` y pegar el enlace en el README (sección 12).

## 10. Entrega en AVA
1. Un solo integrante sube el material al enlace grupal de AVA: **código fuente (app + microservicios), APK firmado y llave `.jks`**, y el enlace al repositorio y a Trello. Confirmar el formato exacto en el enunciado.
2. **Cada** integrante activa el enlace individual de AVA escribiendo nombre y apellido, número de equipo y nombre de la aplicación ("Aprendo IA Jugando en Santiago"). Sin esto no hay defensa.
3. Preparar la defensa: tener los microservicios corriendo y la app instalada. **Si el proyecto no ejecuta durante la defensa, los ítems siguientes al error se califican con 0.**

---

## Checklist final de archivos
`README.md` completo · `app/` · `microservicios/` · `entrega/app-release.apk` · `entrega/aprendoia-release.jks` · `docs/capturas/*.png` (11 imágenes) · enlace a Trello · repositorio público con commits de ambos.

## Capturas esperadas en `docs/capturas/`
`apk_firmado` · `jks` · `signing_report` · `tests_app` · `tests_microservicios` · `cobertura` · `microservicios` · `app_crud` · `app_mapa` · `app_escaner` · `app_perfil` · `trello` · `github` (todas `.png`).

## Si algo falla
| Síntoma | Qué hacer |
| ----- | ----- |
| Error en Gradle Sync o Make Project | Enviar el mensaje completo de la pestaña *Build* |
| `mvn` no encuentra dependencias | Revisar internet y que `java -version` sea 17 |
| La app no se conecta al servidor | Servicios arriba, emulador (usa `10.0.2.2`); en teléfono real usar `-PurlUsuarios`/`-PurlContenido` |
| Puerto 8081/8082 ocupado | Cerrar el proceso anterior o cambiar `server.port` en el `application.yml` |
| La app se cierra al abrir | Desinstalarla del emulador y volver a ejecutar |
| El APK release se cierra al abrir | Probar el APK debug; si funciona, avisar para ajustar reglas de optimización |
