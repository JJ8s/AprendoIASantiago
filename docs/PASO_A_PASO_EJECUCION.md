# Paso a paso: encender los microservicios y hacer funcionar la app (Android Studio en una VM)

## 0. Entender las 3 piezas
1. **Microservicios** (usuarios 8081, contenido 8082): en Docker o con Maven.
2. **App Android**: en Android Studio, dentro de tu máquina virtual (VM), en un emulador o un teléfono.
3. **Red**: la app tiene que *llegar* a los microservicios. Esto es lo que cambia con una VM.

> `10.0.2.2` (valor por defecto de la app) significa "la máquina donde corre el emulador". Si el emulador corre **dentro de la VM**, `10.0.2.2` es la VM, no tu PC.

Elige tu escenario:

| Escenario | Dónde corren los microservicios | Qué dirección usa la app |
| ----- | ----- | ----- |
| **A** | Dentro de la misma VM que Android Studio | `10.0.2.2` (no hay que configurar nada) |
| **B** | En tu PC físico (Docker Desktop), Android Studio en la VM | La IP del PC visible desde la VM (se configura, ver 4B) |

---

## 1. Encender los microservicios

### Opción 1: Docker (en la máquina donde esté Docker)
1. Abre Docker Desktop y espera *Engine running*.
2. Terminal dentro de `microservicios/` (donde está `docker-compose.yml`):
   ```
   docker compose --profile completo up -d --build
   ```
   La primera vez tarda varios minutos.
3. `docker compose ps` → 3 contenedores corriendo: `aprendoia-mysql`, `aprendoia-usuarios`, `aprendoia-contenido`.
4. Apagar luego: `docker compose --profile completo down`.

### Opción 2: Maven (sin Docker; necesita JDK 17 y Maven)
Dos terminales:
```
cd microservicios\usuarios-service   &&  mvn spring-boot:run
cd microservicios\contenido-service  &&  mvn spring-boot:run
```
Dejar ambas abiertas. Para apagar: Ctrl+C en cada una.

### Comprobar (en el navegador de la máquina donde corren)
- http://localhost:8081/api/ranking → JSON con Admin, GenCont, demo
- http://localhost:8082/api/puntos → 7 puntos
Si esto no abre, la app tampoco va a conectar: arréglalo antes de seguir.

---

## 2. Escenario B: dejar que la VM vea los microservicios del PC
(Si estás en el escenario A, salta al paso 3.)

1. **Red de la VM**: en la configuración de la VM usa red **en puente / Bridged** (lo más simple). Con NAT también puede funcionar, pero la dirección del PC cambia.
2. **IP del PC** (en el PC físico, PowerShell): `ipconfig` → anota la "Dirección IPv4" del wifi o cable (ej. `192.168.1.50`).
3. **Abrir el firewall de Windows** (PowerShell como administrador, en el PC físico):
   ```
   New-NetFirewallRule -DisplayName "AprendoIA microservicios" -Direction Inbound -Protocol TCP -LocalPort 8081,8082 -Action Allow
   ```
4. **Probar desde la VM** (navegador de la VM): `http://192.168.1.50:8081/api/ranking` (usa tu IP). Si muestra el JSON, la conexión está lista. Si no abre: revisa que la VM esté en puente y el paso 3.

---

## 3. Abrir el proyecto en Android Studio (en la VM)
1. Descomprime el zip en una ruta **sin espacios ni tildes**, dentro de la VM (ej. `C:\proyectos\AprendoIASantiago`).
2. *File > Open* → carpeta del proyecto → esperar *Gradle Sync* (necesita internet en la VM).
3. *Build > Make Project* debe terminar sin errores. Si falla, copia el error completo.

## 4. Configurar a qué servidor y en qué modo conecta la app
Todo se configura en `gradle.properties` (en la raíz del proyecto). Después de editarlo: **Sync Now**.

### 4A. Escenario A (todo en la VM)
No agregues `urlUsuarios` ni `urlContenido`. Usa el emulador de la VM y listo.

### 4B. Escenario B (microservicios en el PC)
Agrega estas dos líneas con **la IP de tu PC**:
```
urlUsuarios=http://192.168.1.50:8081/api/
urlContenido=http://192.168.1.50:8082/api/
```

### 4C. Elegir la versión (normal o estricta)
- **Normal** (sigue funcionando sin servidor): no agregues nada, o `modoEstricto=false`.
- **Estricta** (sin el microservicio de usuarios no hay login ni registro): agrega
  ```
  modoEstricto=true
  ```
  (El zip `..._estricta` ya lo trae activado por defecto.)

Ejemplo de `gradle.properties` para B + estricta:
```
urlUsuarios=http://192.168.1.50:8081/api/
urlContenido=http://192.168.1.50:8082/api/
modoEstricto=true
```
Cada vez que cambies la IP o el modo: **Sync Now** y vuelve a ejecutar (*Run*), para que se reconstruya la app.

---

## 5. Ejecutar la app
1. **Emulador dentro de la VM**: necesita virtualización anidada activada en la VM (VT-x/AMD-V). Si el emulador no arranca o va extremadamente lento, usa un **teléfono real**.
2. **Teléfono real** (recomendado con VM): activa *Opciones de desarrollador > Depuración USB*, conéctalo y pásalo a la VM (en el menú de la VM: dispositivos USB). Con teléfono real usa siempre el escenario B (IP del PC) y el teléfono debe estar en el mismo wifi.
3. Elige el dispositivo arriba en Android Studio y pulsa **Run ▶**.

## 6. Probar que funciona
**Versión normal o estricta, con los microservicios encendidos**
1. Entra con `demo / demo1234` → debe entrar.
2. Mapa → toca un punto → debe verse el detalle con "Clima ahora".
3. Escáner: escanea `AIJS-001`; el puntaje sube.
4. `Admin / 123` → crea un punto → refresca `http://localhost:8082/api/puntos` (o con la IP del PC): debe aparecer.

**Probar la versión estricta (login depende del servidor)**
1. Cierra sesión en la app.
2. Apaga el servicio de usuarios:
   - Docker: `docker compose stop usuarios`
   - Maven: Ctrl+C en su terminal
3. Intenta entrar con `demo / demo1234` → debe salir *"No hay conexión con el servidor de usuarios..."* y no entrar.
4. Enciende de nuevo (`docker compose start usuarios` o `mvn spring-boot:run`) y reintenta → entra.

**Probar la versión normal sin servidor**: con los servicios apagados, el login con un usuario ya usado en ese teléfono entra igual (usa la base local) y los cambios del Admin avisan "se guardó solo en este teléfono".

---

## 7. Problemas frecuentes
| Síntoma | Causa y solución |
| ----- | ----- |
| La app dice sin conexión pero el navegador del PC sí abre | Escenario B sin configurar: faltan `urlUsuarios/urlContenido` con la IP del PC, o falta el Sync |
| Desde la VM no abre `http://IP_PC:8081/...` | VM no está en puente, firewall de Windows (paso 2.3) o IP equivocada |
| El emulador no inicia dentro de la VM | Falta virtualización anidada: usar teléfono real |
| `Cleartext HTTP traffic not permitted` | Ya está permitido en el Manifest; si ocurre, haz *Build > Clean Project* y vuelve a ejecutar |
| Solo aparece MySQL en Docker | Usaste `docker compose up -d` sin `--profile completo` |
| Cambié la IP o el modo y no se nota | Falta **Sync Now** y volver a ejecutar la app |
| Puerto 8081/8082 ocupado | Hay otro servicio (Maven o Docker anterior): ciérralo |
