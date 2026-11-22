# APK firmado (Guía 3.3.1)

## 1. Crear la llave (una sola vez)
Android Studio → **Build > Generate Signed Bundle / APK > APK > Next > Create new…**
- Key store path: `claves/aprendoia-release.jks` (carpeta `claves/` en la raíz; ya está ignorada por Git con `*.jks`)
- Contraseñas, Alias `aprendoia`, **Validity: 25 años o más**, y tus datos en Certificate.
- Guarda una copia del `.jks` y las contraseñas en un lugar seguro: si se pierde, no podrás actualizar la app.

Por terminal (alternativa):
```
keytool -genkeypair -v -keystore claves/aprendoia-release.jks -alias aprendoia -keyalg RSA -keysize 2048 -validity 9125
```

## 2. Conectar la llave al proyecto
1. Copia `keystore.properties.example` a `keystore.properties` y completa los 4 datos.
2. Sync. (`build.gradle.kts` lee ese archivo; si no existe, el proyecto igual compila, pero el release sale sin firmar.)

## 3. Generar el APK
- Terminal: `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`
- o Android Studio: **Build > Generate Signed Bundle / APK > APK > release > V1 + V2 > Create**.

## 4. Comprobar e instalar
- `./gradlew signingReport` muestra la firma de cada variante.
- Instalar en el emulador/teléfono: `adb install app/build/outputs/apk/release/app-release.apk`
- Probar a mano en el release: login, mapa, escáner, cápsula y Perfil > Ayuda > Novedades (API).

## Subir a Git / Trello
Commit sugerido: "Configuración de firma para APK release". **No subas** `keystore.properties` ni el `.jks`.
