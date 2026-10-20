# Guía: qué se hizo, dónde y cómo (Actividad 2.3 – GPS y Mapas)

## 1. Qué pide la pauta y dónde se cumple
| Requisito de la pauta | Dónde está | Cómo se hizo |
|---|---|---|
| Proyecto GPSMapApp en Java | Todo el proyecto | `app/build.gradle` + código en Java |
| Permisos en AndroidManifest | `app/src/main/AndroidManifest.xml` | INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION |
| Dependencias de Google Maps | `app/build.gradle` | `play-services-maps` y `play-services-location` |
| API Key | `res/values/google_maps_api.xml` + `<meta-data>` del Manifest | La clave se guarda como string y el Manifest la lee |
| MapView en activity_main.xml | `res/layout/activity_main.xml` | `<MapView>` + botón + ImageView |
| Configurar el mapa | `MainActivity.java` → `onCreate` y `onMapReady` | `mapView.getMapAsync(this)` |
| Permisos en tiempo de ejecución | `MainActivity.java` → `habilitarUbicacion()` y `onRequestPermissionsResult()` | `checkSelfPermission` + `requestPermissions` |
| Ubicación actual | `habilitarUbicacion()` | `FusedLocationProviderClient.getLastLocation()` |
| Añadir marcadores | `agregarMarcador()` | `setOnMapClickListener` → `addMarker` |
| Threads | `agregarMarcador()` | `ExecutorService` busca la dirección en segundo plano; `Handler` vuelve a la UI |
| Capturar y mostrar imagen | botón `btnFoto` + `camaraLauncher` | `TakePicturePreview` abre la cámara y el resultado va al `ImageView` |
| GitHub + README | `README.md` | Repo `SimpleGPSMapApp` |

## 2. Explicación archivo por archivo
- **settings.gradle / build.gradle (raíz) / gradle.properties**: configuración general de Gradle (repositorios Google y Maven, plugin de Android, AndroidX activado).
- **app/build.gradle**: define `compileSdk 34`, `minSdk 24`, Java 8 y las librerías: appcompat, Maps y Location.
- **AndroidManifest.xml**: declara los permisos, la API Key (`com.google.android.geo.API_KEY`) y la `MainActivity` como pantalla inicial. No se pide permiso CAMERA porque la foto se toma con la app de cámara del teléfono.
- **google_maps_api.xml**: aquí pegas TU API Key.
- **activity_main.xml**: pantalla vertical: mapa (ocupa todo el espacio), botón "Tomar foto" y recuadro con la imagen.
- **MainActivity.java**: la lógica, explicada abajo.

## 3. Cómo funciona MainActivity.java (en orden)
1. **onCreate**: carga el layout, conecta el botón con la cámara, crea el cliente de ubicación y pide el mapa con `getMapAsync`.
2. **onMapReady**: se ejecuta cuando el mapa está listo. Activa los botones de zoom, deja escuchar los toques en el mapa y llama a `habilitarUbicacion()`.
3. **habilitarUbicacion**: si no hay permiso, lo pide. Si lo hay, activa el punto azul, lee la última ubicación, pone un marcador "Estás aquí" y mueve la cámara con zoom 15.
4. **onRequestPermissionsResult**: recibe la respuesta del usuario; si aceptó, repite el paso 3.
5. **agregarMarcador**: al tocar el mapa, un hilo secundario consulta el `Geocoder` (convierte coordenadas en dirección). Esto puede ser lento, por eso no se hace en el hilo principal. Luego `mainHandler.post(...)` dibuja el marcador en la pantalla, porque solo el hilo principal puede tocar la UI.
6. **camaraLauncher**: abre la cámara; al volver, muestra la foto en el `ImageView`.
7. **Métodos onStart/onResume/...**: un `MapView` exige reenviarle el ciclo de vida de la Activity; si no, falla o se queda en blanco.

## 4. Cómo abrirlo y ejecutarlo
1. Descomprime el ZIP y en Android Studio: **File → Open** y elige la carpeta `GPSMapApp`. Espera el Sync de Gradle.
   - Si Android Studio da problemas al abrirlo, crea un proyecto nuevo "GPSMapApp" (Empty Views Activity, Java) y copia estos archivos en las mismas rutas.
2. Consigue tu API Key: console.cloud.google.com → proyecto → activar **Maps SDK for Android** → Credenciales → Crear API Key.
3. Pégala en `google_maps_api.xml` reemplazando `TU_API_KEY_AQUI`.
4. Ejecuta (▶) en un teléfono real o en un emulador con Google Play. En emulador fija la ubicación en `⋮ → Location`.

## 5. Subir a GitHub
1. Crea en github.com el repo vacío **SimpleGPSMapApp**.
2. En la terminal de Android Studio:
```
git init
git add .
git commit -m "App GPS y mapas con marcadores y captura de imagen"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/SimpleGPSMapApp.git
git push -u origin main
```
3. Importante: sube el repo con `TU_API_KEY_AQUI` (sin tu clave real) para que nadie use tu cuenta de Google.

## 6. Preguntas de cierre (borrador, adáptalo con tus palabras)
1. **Conocimientos nuevos**: aprendí a usar hilos (ExecutorService y Handler) para tareas lentas como el Geocoder sin congelar la app, y a integrar el GPS con FusedLocationProviderClient, incluyendo la solicitud de permisos en tiempo de ejecución.
2. **Mejorar otras apps**: apps de delivery o transporte con seguimiento en vivo, apps turísticas con puntos de interés cercanos, apps de seguridad que comparten la ubicación, o registro de rutas deportivas.
3. **Logros y desafíos**: logré mostrar la ubicación y agregar marcadores. Los desafíos fueron configurar la API Key y activar el SDK correcto, manejar el permiso de ubicación, y probar el GPS en el emulador; los resolví fijando una ubicación manual y revisando la configuración en Google Cloud.
