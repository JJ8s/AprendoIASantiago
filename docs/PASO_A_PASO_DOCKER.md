# Paso a paso: comprobar los microservicios con Docker

Docker es **opcional** para la entrega (los servicios también corren con H2 y Maven). Sirve para verlos sobre MySQL real y "empaquetados".

## Qué hay y qué no hay (importante)
- `docker compose up -d` (sin nada más) levanta **solo MySQL**. Si ves solo `aprendoia-mysql`, es lo esperado con ese comando.
- Para levantar también los dos servicios hay que usar el **perfil `completo`** (paso 2). Para eso tu carpeta `microservicios/` debe tener:
  - `docker-compose.yml` con las secciones `usuarios` y `contenido`
  - `usuarios-service/Dockerfile` y `contenido-service/Dockerfile`

  Si no los tienes, copia esos archivos desde el paquete `docker_microservicios.zip` (respetando las carpetas) y **hazlo en la rama que vas a entregar**.

## 0. Requisitos
- Docker Desktop instalado y **abierto** (abajo a la izquierda debe decir *Engine running*).
- Libres los puertos **3306, 8081 y 8082**. Cierra los servicios de Maven (Ctrl+C) y cualquier MySQL local.
- Una terminal abierta **dentro de la carpeta `microservicios/`** (donde está `docker-compose.yml`). Comprobar: `dir` (Windows) o `ls` (Mac/Linux) debe mostrar `docker-compose.yml`, `usuarios-service` y `contenido-service`.

## 1. (Si ya levantaste solo MySQL) no hace falta borrar nada
Compose reutiliza `aprendoia-mysql` y agrega los otros dos.

## 2. Levantar todo
```
docker compose --profile completo up -d --build
```
- La primera vez tarda **varios minutos** (descarga imágenes y compila con Maven dentro de Docker). Es normal que parezca detenido.
- Termina con tres líneas tipo `Container ... Started` (MySQL en `Healthy`).
- Si falla compilando, copia el error completo (las últimas ~30 líneas).

## 3. Verificar que estén los 3 contenedores
```
docker compose ps
```
Deben verse `aprendoia-mysql` (healthy), `aprendoia-usuarios` (0.0.0.0:8081->8081) y `aprendoia-contenido` (0.0.0.0:8082->8082), todos *running*.
En Docker Desktop: pestaña *Containers* → flecha `>` del grupo **microservicios** para expandirlo.
📸 `docker_ps.png`

## 4. Ver el arranque de cada servicio
```
docker compose logs usuarios
docker compose logs contenido
```
Busca `Started UsuariosServiceApplication in ... seconds` (o el equivalente de contenido). Si hay `Exception`, cópiala. (En Docker Desktop: clic en el contenedor → pestaña *Logs*.)

## 5. Probar los endpoints en el navegador
| Prueba | Dirección | Debe mostrar |
| ----- | ----- | ----- |
| Ranking | http://localhost:8081/api/ranking | JSON con Admin, GenCont y demo |
| Usuarios | http://localhost:8081/api/usuarios | Lista de usuarios |
| Puntos | http://localhost:8082/api/puntos | 7 puntos turísticos |
| Cápsulas | http://localhost:8082/api/capsulas | 7 cápsulas (AIJS-001…007) |
| Cápsula por QR | http://localhost:8082/api/capsulas/qr/AIJS-001 | 1 cápsula |

📸 `docker_endpoints.png`

## 6. Probar un login y una escritura (PowerShell)
Login:
```
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/login -ContentType "application/json" -Body '{"nombreUsuario":"demo","clave":"demo1234"}'
```
Debe devolver los datos de `demo`. Con una clave incorrecta debe dar error 401.

Crear un punto y comprobarlo:
```
Invoke-RestMethod -Method Post -Uri http://localhost:8082/api/puntos -ContentType "application/json" -Body '{"nombre":"Punto de prueba","descripcion":"Creado desde PowerShell","latitud":-33.44,"longitud":-70.65,"altitud":540}'
```
Luego abre http://localhost:8082/api/puntos: debe aparecer (8 puntos). Bórralo con `Invoke-RestMethod -Method Delete -Uri http://localhost:8082/api/puntos/ID` (usa el `id` que devolvió).
En Mac/Linux usa `curl -X POST ... -H "Content-Type: application/json" -d '{...}'`.

## 7. Confirmar que los datos están en MySQL
```
docker exec -it aprendoia-mysql mysql -uroot -proot -e "SHOW DATABASES;"
docker exec -it aprendoia-mysql mysql -uroot -proot -e "SELECT id, nombre_usuario, puntaje FROM usuarios_db.usuarios;"
docker exec -it aprendoia-mysql mysql -uroot -proot -e "SELECT id, codigo_qr, titulo FROM contenido_db.capsulas;"
```
Deben existir `usuarios_db` y `contenido_db` con datos. 📸 `docker_mysql.png`

Persistencia: `docker compose --profile completo restart` y repite el paso 5: los datos siguen.

## 8. Probar con la app
- Emulador: no cambia nada (usa `10.0.2.2:8081` y `10.0.2.2:8082`).
- Teléfono real: `./gradlew assembleDebug -PurlUsuarios=http://IP_DEL_PC:8081/api/ -PurlContenido=http://IP_DEL_PC:8082/api/`
- En la app, entra con `Admin/123`, crea un punto y revisa http://localhost:8082/api/puntos.

## 9. Apagar
- Detener y quitar contenedores (conserva los datos): `docker compose --profile completo down`
- Borrar también la base: `docker compose --profile completo down -v`
- Después de cambiar código de un servicio: `docker compose --profile completo up -d --build`

## Problemas frecuentes
| Síntoma | Causa / solución |
| ----- | ----- |
| Solo aparece `aprendoia-mysql` | Se usó `docker compose up -d` sin `--profile completo`, o falta el compose/Dockerfile nuevos (ver "Qué hay y qué no hay") |
| `no configuration file provided` / `no such service` | La terminal no está en `microservicios/`, o el compose es el viejo |
| `Cannot connect to the Docker daemon` | Abrir Docker Desktop y esperar a *Engine running* |
| `port is already allocated` | Hay otra cosa en 3306/8081/8082: cerrar Maven/MySQL local, o cambiar el puerto de la izquierda (ej. `"8083:8081"`) |
| Un servicio se reinicia (*Restarting*) | `docker compose logs usuarios`; suele ser MySQL aún iniciando: espera 30 s |
| Build falla descargando dependencias | Revisar internet; reintentar con `docker compose --profile completo build --no-cache` |
| `Public Key Retrieval is not allowed` | Falta `allowPublicKeyRetrieval=true` en la URL MySQL de `application.yml` (ya viene en el paquete) |
| La app no conecta | Probar primero el paso 5 en el navegador del PC; en teléfono real usar la IP del PC y abrir el firewall |
