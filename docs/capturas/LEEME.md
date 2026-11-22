# Evidencias para entregar (capturas reales, sin editar)

Guarda aquí las imágenes con estos nombres; el README las enlaza.

| Archivo | Qué debe verse |
| ----- | ----- |
| `apk_firmado.png` | Carpeta `app/build/outputs/apk/release/` con `app-release.apk` (o Build > Generate Signed APK finalizado) |
| `jks.png` | El archivo `.jks` en su carpeta (nunca muestres las contraseñas) |
| `signing_report.png` | Salida de `./gradlew signingReport` en la variante release |
| `tests_app.png` | `./gradlew test` en verde o el panel de Android Studio |
| `cobertura.png` | Panel *Coverage* con el % de domain / data.repository / ui.viewmodels |
| `microservicios.png` | Los dos servicios corriendo y un `GET /api/ranking` o `/api/puntos` en el navegador o Postman |
| `app_crud.png` | Panel Admin creando/editando un punto y el cambio visible en el servicio |
| `trello.png` | Tablero con Por hacer / En curso / Finalizado y tarjetas asignadas |
| `tests_microservicios.png` | `mvn test` en verde en ambos servicios |
| `app_mapa.png`, `app_escaner.png`, `app_perfil.png` | Pantallas principales de la app |
| `github.png` | Pestaña *Contributors* o historial de commits con ambos integrantes |

Recuerda: el `.jks` y el APK firmado deben quedar en el repositorio (por ejemplo carpeta `entrega/`).
Si el `.jks` se sube, no pongas contraseñas reales en archivos del repo: usa claves de curso y avisa al docente.
