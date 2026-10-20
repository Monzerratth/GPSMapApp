# SimpleGPSMapApp

Aplicación Android (Java) que muestra un mapa de Google Maps con la ubicación
actual del usuario, permite agregar marcadores tocando el mapa (la dirección de
cada marcador se obtiene en un Thread en segundo plano) y capturar una imagen
con la cámara.

## Funcionalidades
- Mapa de Google Maps con la ubicación actual (GPS)
- Marcadores al tocar cualquier punto del mapa, con su dirección
- Captura y visualización de una imagen con la cámara
- Uso de Threads (ExecutorService + Handler)
- Solicitud de permisos en tiempo de ejecución

## Tecnologías
Java, Android Studio, Google Maps SDK for Android, Fused Location Provider.

## Cómo ejecutarla
1. Clonar el repositorio y abrirlo en Android Studio.
2. Obtener una API Key en Google Cloud Console (activar **Maps SDK for Android**).
3. Pegarla en `app/src/main/res/values/google_maps_api.xml` (reemplazar `TU_API_KEY_AQUI`).
4. Ejecutar en un dispositivo o emulador con Google Play y GPS activo.
5. Aceptar el permiso de ubicación y tocar el mapa para agregar marcadores.
