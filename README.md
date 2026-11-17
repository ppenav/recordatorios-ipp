# Recordatorios

Aplicación Android de recordatorios hecha con Kotlin, Jetpack Compose y Material 3.

La versión 3 guarda los recordatorios en Room, las preferencias en DataStore y se sincroniza con un servicio REST mediante Retrofit y OkHttp. Funciona sin conexión: todo cambio se guarda primero en el teléfono y se envía al servicio cuando hay red.

## Requisitos

- Android Studio con el SDK 36 instalado.
- JDK 17 o superior (el que trae Android Studio sirve).
- Python 3.10 o superior para el servicio simulado.
- Un emulador o un teléfono con Android 7.0 (API 24) o superior.

## Configuración

La URL del servicio se lee del archivo `api.properties` en la raíz del proyecto. Si no existe, se usa `api.properties.example`.

```
cp api.properties.example api.properties
```

| Clave | Valor de ejemplo | Uso |
|---|---|---|
| `API_BASE_URL` | `http://10.0.2.2:8080/` | Dirección del servicio. `10.0.2.2` es el computador visto desde el emulador. |

El proyecto no usa credenciales ni tokens. `api.properties` está en `.gitignore` para que cada persona use su propia dirección.

## Servicio simulado

En `mock-server/server.py` hay un servicio REST sin dependencias externas. Guarda los datos en `mock-server/data.json`.

```
python mock-server/server.py 8080
```

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/reminders` | Lista de recordatorios |
| PUT | `/reminders/{id}` | Crea o actualiza el recordatorio con ese id (201 o 200) |
| DELETE | `/reminders/{id}` | Elimina el recordatorio (204, o 404 si no existe) |
| PUT | `/_mode/{modo}` | Simula fallas: `ok`, `error500`, `badrequest`, `timeout` |

Ejemplo para simular un error del servidor y volver a la normalidad:

```
curl -X PUT http://localhost:8080/_mode/error500
curl -X PUT http://localhost:8080/_mode/ok
```

## Compilar y ejecutar

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

También se puede abrir la carpeta en Android Studio y presionar Run con el emulador encendido.

## Probar el modo sin conexión

1. Con la app abierta, activar el modo avión del emulador.
2. Crear o editar un recordatorio: queda marcado como "Pendiente de sincronizar".
3. Desactivar el modo avión: la app sincroniza sola y el recordatorio pasa a "Sincronizado".
4. El registro del servicio muestra un solo PUT por recordatorio, aunque la sincronización se dispare varias veces.
