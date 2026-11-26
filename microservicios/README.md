# Microservicios (Spring Boot 3.3 · Java 17 · Maven)

| Servicio | Puerto | Base de datos | Responsabilidad |
| ----- | ----- | ----- | ----- |
| `usuarios-service` | 8081 | `usuarios` (H2 archivo / MySQL `usuarios_db`) | Registro, login, perfil, progreso y ranking |
| `contenido-service` | 8082 | `contenido` (H2 archivo / MySQL `contenido_db`) | CRUD de puntos turísticos y cápsulas QR |

## Cómo ejecutarlos
Requisitos: JDK 17+ y Maven (o el Maven de IntelliJ). En dos terminales:
```
cd microservicios/usuarios-service  && mvn spring-boot:run
cd microservicios/contenido-service && mvn spring-boot:run
```
- Por defecto usan **H2 en archivo** (carpeta `data/`), sin instalar nada. Consola: `http://localhost:8081/h2-console`.
- Con **MySQL**: `docker compose up -d` y luego `mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=mysql`.
- Pruebas: `mvn test` en cada carpeta.
- Desde el emulador Android el computador es `http://10.0.2.2:8081` y `http://10.0.2.2:8082`.

## Endpoints — usuarios-service (`/api`)
| Método | Ruta | Descripción | Respuestas |
| ----- | ----- | ----- | ----- |
| POST | `/usuarios` | Registrar jugador | 201, 400, 409 (alias repetido) |
| POST | `/auth/login` | Iniciar sesión | 200, 401 |
| GET | `/usuarios` · `/usuarios/{id}` | Listar / obtener | 200, 404 |
| PUT | `/usuarios/{id}` | Editar nombre, correo, teléfono | 200, 400, 404 |
| PUT | `/usuarios/{id}/clave` | Cambiar contraseña `{"claveActual","claveNueva"}` | 204, 401, 404 |
| DELETE | `/usuarios/{id}` | Eliminar usuario y su progreso | 204, 404 |
| GET | `/ranking` | Usuarios por puntaje | 200 |
| POST | `/usuarios/{id}/progreso/{capsulaId}` | Completar cápsula `{"puntos":10}` (idempotente) | 200, 404 |
| GET | `/usuarios/{id}/progreso` | Ids de cápsulas completadas | 200, 404 |

## Endpoints — contenido-service (`/api`)
| Método | Ruta | Descripción |
| ----- | ----- | ----- |
| GET / POST | `/puntos` | Listar / crear punto |
| GET / PUT / DELETE | `/puntos/{id}` | Obtener / editar / borrar (borra sus cápsulas) |
| GET | `/puntos/{id}/capsulas` | Cápsulas de un punto |
| GET / POST | `/capsulas` | Listar / crear cápsula (QR único → 409 si se repite) |
| GET / PUT / DELETE | `/capsulas/{id}` | Obtener / editar / borrar |
| GET | `/capsulas/qr/{codigo}` | Buscar cápsula por código QR |

Los errores devuelven `{"estado":404,"mensaje":"..."}`. Las claves se guardan con BCrypt y nunca se devuelven.
Cuentas sintéticas creadas al iniciar: `Admin/123`, `GenCont/123`, `demo/demo1234`.

## Docker (opcional)

- Solo MySQL: `docker compose up -d` y arrancar cada servicio con `--spring.profiles.active=mysql`.
- Todo en contenedores: `docker compose --profile completo up -d --build` (MySQL + usuarios en 8081 + contenido en 8082).
- Ver registros: `docker compose logs -f usuarios contenido` · Apagar: `docker compose --profile completo down` (con `-v` borra también los datos).
- Guía completa: `docs/PASO_A_PASO_DOCKER.md`.
