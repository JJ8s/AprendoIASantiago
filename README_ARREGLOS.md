# AprendoIA Santiago · Registro de revisión y arreglos

Documento de la revisión hecha al repositorio `JJ8s/AprendoIASantiago` (rama `main`,
último commit: "Pantallas puntaje progreso y catalogo finalizado").

> **Importante:** en esta revisión **no se modificó ningún archivo del código**.
> Lo que sigue separa lo que se comprobó, lo que se corrigió en la documentación
> y lo que está **propuesto pero aún no aplicado**.

---

## 1. Revisión de errores que impidan ejecutar (análisis estático)

No se pudo compilar (no había Android SDK ni acceso a los servidores de Gradle/Google),
por lo que esta revisión **no garantiza** que la app compile.

| Comprobación | Resultado |
|---|---|
| Imports internos (`com.example.aprendoiasantiago...`) | Todos apuntan a clases existentes. `R` lo genera Android al compilar |
| Textos (`R.string.*`) | Los 75 usados están definidos en `res/values/strings.xml` |
| Recursos `R.drawable` / `R.raw` / `R.mipmap` en el código | No se usan; los iconos del manifest existen |
| Room: entidades en `@Database` | Las 6 entidades de `data/model/Entidades.kt` están registradas |
| Room: versión y migraciones | Versión 3, con migraciones 1→2 y 2→3 |
| Funciones DAO llamadas desde los repositorios | Todas existen en `Daos.kt` |
| Manifest | Permisos de internet, ubicación y cámara presentes |
| Mapa (osmdroid) | `MapaScreen` configura el `userAgent`, necesario para cargar mosaicos |

### Riesgos de entorno (no son errores de código)
- Versiones muy nuevas: AGP 9.4.1, Gradle 9.6.0, compileSdk/targetSdk 37, Kotlin 2.2.10, KSP 2.3.10.
  Requiere un Android Studio reciente y el SDK 37 instalado; de lo contrario falla la sincronización.
- Los videos de las cápsulas y los mosaicos del mapa necesitan conexión a internet.
- `exportSchema = false` en Room: no se guarda el esquema, por lo que no hay verificación automática de migraciones.

---

## 2. Correcciones a información dada antes en la conversación

| Dije | Realidad al clonar el repo |
|---|---|
| "No hay carpeta `model/`" | **Sí existe**: `data/model/Entidades.kt` con `UsuarioEntity`, `PuntoTuristicoEntity`, `CapsulaEntity`, `ProgresoEntity`, `RutaEntity`, `RutaPuntoEntity` |
| Entidades en `data/local/` | Se movieron a `data/model/` (commit "reorganizar entidades en carpeta model") |
| Estructura de `ui/` sin `gencont`, `screens`, `components` | Existen `ui/gencont/GenContScreen.kt`, `ui/screens/` (`CatalogoScreen`, `MainScreen`, `ProgresoScreen`) y `ui/components/EncabezadoPantalla.kt` |
| Solo la rama `main` | Hay otras ramas: `Brain-jordy`, `Brain_Alejandro`, `Testing` |

---

## 3. Mejoras propuestas (NO aplicadas)

### 3.1 Vista de mapa (`ui/mapa/MapaScreen.kt`)
Propuesta de rediseño para que el mapa sea más legible:

1. Marcadores de color: verde = visitado, naranja = pendiente.
2. Tarjeta inferior al tocar un punto, con nombre, descripción y distancia al jugador.
3. Ventanita de video **bloqueada** (solo vista previa con candado, sin reproducción).
4. Botón **Escanear QR** que solo navega a la pantalla del lector.
5. Punto azul con la ubicación del jugador y botón para recentrar el mapa.
6. Chips de filtro: Todos / Pendientes / Visitados.
7. Cabecera de progreso ("3 de 8 puntos").
8. Opcional: habilitar el escaneo solo a menos de ~50 m del punto.

Estado: el código se entregó en la conversación **escrito sobre nombres supuestos**
(`PuntoUi`, `R.drawable.pin_*`, `RUTA_ESCANER`), sin haber leído el `MapaScreen.kt` real.
Hay que adaptarlo antes de integrarlo.

Pendiente para aplicarlo:
- Crear `pin_visitado`, `pin_pendiente` y `pin_yo` en `res/drawable/`.
- Conectar `onEscanear` con la ruta real del lector en `navigation/Destino.kt`.
- Pasar la ubicación desde `ubicacion/ProveedorUbicacion.kt`.

### 3.2 Modelos de dominio separados de las entidades
Hoy las entidades de Room (`data/model/Entidades.kt`) llegan hasta la UI.
Propuesta: crear `domain/model/` con clases sin anotaciones de Room y un archivo
`data/mapper/Mapeadores.kt` con funciones `Entity.toModelo()`.
Ventaja: la UI no depende de Room y no expone datos como `claveHash`.

---

## 4. Otros puntos a revisar
- Las cuentas de prueba (`Admin`, `GenCont`, `demo`) están definidas en `DatosEjemplo.kt` con
  contraseñas simples; conviene cambiarlas o quitarlas antes de una entrega pública.
- El zip entregado contiene la rama `main` completa, sin `.git`.
- Falta comparar las pantallas con el enunciado/PDF del proyecto, que no se pudo leer.
