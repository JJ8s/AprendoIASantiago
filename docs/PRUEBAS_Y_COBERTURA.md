# Pruebas unitarias y cobertura (IE 3.2.1)

## Herramientas
JUnit 5 (`useJUnitPlatform`) + Kotest (aserciones) + MockK (simulacros) + `kotlinx-coroutines-test`.
Las pruebas antiguas de JUnit 4 siguen corriendo con `junit-vintage-engine`.

## Cómo ejecutarlas
- Terminal: `./gradlew test` (informe HTML en `app/build/reports/tests/testDebugUnitTest/index.html`).
- Android Studio: clic derecho en `app/src/test` → **Run Tests in …**.
- Microservicios: `mvn test` dentro de `microservicios/usuarios-service` y `microservicios/contenido-service`.
- Pruebas de interfaz (emulador): `./gradlew connectedDebugAndroidTest`.

## Cómo medir el 80 % de cobertura (para la evidencia)
1. Android Studio → clic derecho en `app/src/test` → **Run Tests in … with Coverage**.
2. En el panel *Coverage* abre `com.example.aprendoiasantiago` y mira el porcentaje de las carpetas
   `domain`, `data.repository`, `ui.viewmodels`, `utils` (la "lógica"; las pantallas Compose, Room generado y
   Activity no cuentan como lógica y se prueban en el emulador).
3. Saca captura del panel con los porcentajes y súbela al repositorio / Trello.
4. Si alguna clase queda bajo 80 %, haz doble clic: las líneas rojas son las que faltan probar.

## Qué clase de lógica tiene prueba (revisión estática)
| Capa | Clases | Pruebas |
| ----- | ----- | ----- |
| ViewModel | Login, Perfil, PuntoDetalle, Post, Clima, Mapa, Escáner, Cápsula, Admin, GenCont, Evidencia, AjustesPerfil | `viewmodel/*Test` (12 de 12) |
| Repository | Auth, Perfil, Notificaciones, Juego, Servidor, Post, Clima, Sesión | `repository/*Test`, `JuegoRepositoryTest` |
| Dominio | ValidadorLogin/Perfil/Rostro, Rutas, ProgresoPorPunto, Geo, DetallePunto, Clima, Notificaciones | `domain/*Test` y tests de raíz |
| Utilidades | Hash | `HashTest` |
| Microservicios | Servicio y controlador de cada uno | `src/test` de cada servicio |

Casos cubiertos: flujo correcto, validaciones, errores de red/base de datos y casos borde.
