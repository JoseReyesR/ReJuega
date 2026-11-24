# 🧸 ReJuega — Guía de Instalación y Configuración para el Equipo

Aplicación Android para la gestión trazable de donaciones de juguetes infantiles,
con arquitectura **cliente-servidor** (Node.js + Express + MySQL) y respaldo
**Firebase / Room** para modo offline.

> 📌 **Lee este documento completo antes de intentar ejecutar el proyecto.**
> La mayoría de errores comunes están en la sección [11. Solución de problemas](#11-solución-de-problemas).

---

## 📋 Contenido

1. [Requisitos previos](#1-requisitos-previos)
2. [Clonar el repositorio](#2-clonar-el-repositorio)
3. [Configurar la base de datos MySQL](#3-configurar-la-base-de-datos-mysql)
4. [Configurar el servidor Node.js](#4-configurar-el-servidor-nodejs)
5. [Permitir conexiones desde un celular físico](#5-permitir-conexiones-desde-un-celular-físico)
6. [Configurar la app Android](#6-configurar-la-app-android)
7. [Ejecutar todo el sistema](#7-ejecutar-todo-el-sistema)
8. [Estructura del proyecto](#8-estructura-del-proyecto)
9. [Endpoints disponibles](#9-endpoints-disponibles)
10. [Arquitectura y fuentes de datos](#10-arquitectura-y-fuentes-de-datos)
11. [Solución de problemas](#11-solución-de-problemas)
12. [Buenas prácticas Git](#12-buenas-prácticas-git)
13. [Comandos útiles](#13-comandos-útiles)
14. [Datos semilla incluidos](#14-datos-semilla-incluidos)

---

## 1. Requisitos previos

Instala y verifica estas herramientas **antes de continuar**:

| Herramienta | Versión probada | Comando de verificación |
|---|---|---|
| **Android Studio** | 2024.x o superior | — |
| **JDK** | 17 o 21 (incluido en Android Studio) | Ver sección 6.2 |
| **Node.js** | v18+ (probado en v24.16.0) | `node --version` |
| **npm** | 9+ (probado en 11.13.0) | `npm --version` |
| **MySQL** | 8.0.x (probado en 8.0.46) | `mysql --version` |
| **MySQL Workbench** | 8.x (opcional pero recomendado) | — |
| **Git** | Cualquier versión reciente | `git --version` |

> ⚠️ **No necesitas instalar Express globalmente.** Se instala por proyecto con `npm install`.

---

## 2. Clonar el repositorio

```bash
git clone <https://github.com/JoseReyesR/ReJuega.git>
cd ReJuegaaa
```

Verifica que tengas estas carpetas principales:

```text
ReJuega/
├── app/          → Código de la app Android (Kotlin + Jetpack Compose)
├── server/       → Servidor Node.js + Express + scripts SQL
└── README.md     → Este documento
```

---

## 3. Configurar la base de datos MySQL

### 3.1 Asegúrate de que MySQL esté corriendo

- Windows: abre `services.msc` y verifica que el servicio `MySQL80` esté **En ejecución**.
- O abre MySQL Workbench y conecta a tu instancia local.

### 3.2 Ejecutar el script de esquema y datos semilla

El script crea la base de datos `rejuega_db`, las 9 tablas y los datos de prueba.

**Opción A — MySQL Workbench (recomendada):**
1. Abre el archivo `server/database/rejuega_schema.sql` en Workbench.
2. Haz clic en el ícono de **rayo (⚡ Execute)**.

**Opción B — Consola:**

```bash
cd server
mysql -u root -p < database/rejuega_schema.sql
```

### 3.3 Verificar

```sql
USE rejuega_db;
SHOW TABLES;
SELECT COUNT(*) FROM users;      -- esperado: 8
SELECT COUNT(*) FROM campaigns;  -- esperado: 5
SELECT COUNT(*) FROM materials;  -- esperado: 7
```

> 🔐 Si tu usuario root tiene contraseña distinta a la del líder del equipo,
> solo debes ajustarla en tu archivo `.env` local (paso 4.2). **Nunca la subas a Git.**

---

## 4. Configurar el servidor Node.js

### 4.1 Instalar dependencias

```bash
cd server
npm install
```

Esto instala: `express`, `mysql2`, `cors`, `dotenv`, `uuid` y `nodemon` (dev).

### 4.2 Crear tu archivo `.env`

Copia el archivo de ejemplo y completa tus datos locales:

```bash
copy .env.example .env        (Windows)
cp .env.example .env          (Mac/Linux)
```

Edita `server/.env`:

```env
PORT=3000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=TU_CONTRASENA_MYSQL
DB_NAME=rejuega_db
DB_PORT=3306
ALLOWED_ORIGINS=*
```

> 🚫 **REGLA DE ORO:** el archivo `.env` **NUNCA se sube a Git** (ya está en `.gitignore`).
> Cada integrante tiene su propio `.env` con su contraseña local.

### 4.3 Iniciar el servidor

```bash
npm run dev
```

Salida esperada:

```text
✅ Conectado a MySQL: rejuega_db
🚀 Servidor corriendo en http://localhost:3000
```

### 4.4 Verificar en el navegador

Abre `http://localhost:3000` y deberías ver:

```json
{ "app": "ReJuega API", "version": "1.0.0", "status": "OK", ... }
```

Y `http://localhost:3000/api/materiales` debe listar 7 materiales.

> 💡 El servidor escucha en `0.0.0.0` (todas las interfaces) para permitir
> conexiones desde el emulador y desde celulares físicos en la misma red.

---

## 5. Permitir conexiones desde un celular físico

Solo necesario si pruebas en **celular físico** (el emulador no lo necesita).

### 5.1 Abrir el puerto 3000 en el Firewall de Windows

PowerShell **como administrador**:

```powershell
New-NetFirewallRule -DisplayName "Node.js ReJuega API" -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow
```

### 5.2 Conocer la IP de tu PC

```powershell
ipconfig
```

Copia la **Dirección IPv4** (ej. `192.168.1.100`).

### 5.3 Configurar la app para celular físico

Edita `app/src/main/java/pe/rejuega/app/data/remote/ApiConfig.kt`:

```kotlin
const val NODE_BASE_URL = "http://TU_IP_LOCAL:3000/"   // ej. http://192.168.1.100:3000/
```

> 📶 Tu PC y tu celular deben estar en la **misma red Wi-Fi**.
> Si cambias de red, tu IP cambia y debes actualizar esta línea.

**Para emulador**, en cambio, se usa:

```kotlin
const val NODE_BASE_URL = "http://10.0.2.2:3000/"
```

---

## 6. Configurar la app Android

### 6.1 Abrir el proyecto

Android Studio → **Open** → selecciona la carpeta raíz `ReJuega`.

Espera a que termine el **Gradle Sync** inicial (puede tardar varios minutos).

### 6.2 ⚠️ FIX OBLIGATORIO: error de `jlink.exe`

Si al compilar ves este error:

```text
jlink executable ...\redhat.java...\jre\...\bin\jlink.exe does not exist
```

Significa que Gradle tomó el JRE incompleto de la extensión de VS Code. Solución:

1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**.
2. En **Gradle JVM criteria → Version**, cambia `25` por **`21`** (o `17`).
3. **Apply → OK**.
4. **File → Sync Project with Gradle Files**.

Si persiste, agrega en `gradle.properties` (raíz del proyecto):

```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

### 6.3 Firebase

- El archivo `app/google-services.json` **ya está en el repositorio** (es compartido por el equipo).
- Si Android Studio lo pide, acepta actualizar los servicios de Google.
- Las reglas de Firestore vigentes están documentadas en el informe del proyecto.

### 6.4 Cloudinary (subida de imágenes)

Las constantes de Cloudinary están en estos archivos y deben coincidir con la cuenta del equipo:

- `app/.../data/remote/DonationRepository.kt`
- `app/.../data/repository/CampaignGalleryRepository.kt`

```kotlin
private const val CLOUD_NAME = "NOMBRE_DE_CLOUD_DEL_EQIPO"
private const val UPLOAD_PRESET = "rejuega_preset"
```

> 📣 Pídele al líder del equipo el `CLOUD_NAME` correcto si ves `TU_CLOUD_NAME`.

### 6.4 Tráfico sin cifrar (HTTP local)

Verifica que `AndroidManifest.xml` contenga:

```xml
<application
    android:usesCleartextTraffic="true"
    ... >
```

(Sin esto, el celular/emulador bloquea las peticiones a `http://`.)

---

## 7. Ejecutar todo el sistema

**Orden de encendido:**

1. **MySQL** corriendo (servicio activo).
2. **Servidor Node:** `cd server` → `npm run dev`.
3. **App Android:** botón **Run ▶** en Android Studio (emulador o celular).

**Cuentas de prueba:**

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@rejuega.app` | *(compartida en el canal del equipo)* |
| Donante | `donante@rejuega.app` | *(compartida en el canal del equipo)* |

> 🔑 Por seguridad, las contraseñas **no se publican en este README**.
> También puedes registrar una cuenta nueva desde la pantalla de Login.

**Prueba rápida de integración:**
Menú lateral (Admin) → **Pruebas del Servidor** → botón **Ejecutar todas las pruebas**.
Debes ver tarjetas con códigos **200, 201, 400 y 404** en pantalla.

---

## 8. Estructura del proyecto

```text
ReJuega/
├── app/                                  # APP ANDROID (Kotlin + Compose)
│   └── src/main/java/pe/rejuega/app/
│       ├── data/
│       │   ├── local/                    # Room (caché offline / respaldo)
│       │   ├── remote/                   # Firebase, Cloudinary, Retrofit, DTOs
│       │   └── repository/               # Repositorios (fuentes de datos)
│       ├── viewmodel/                    # ViewModels (MVVM)
│       ├── ui/
│       │   ├── screens/                  # Pantallas Compose
│       │   ├── components/               # Componentes reutilizables
│       │   └── navigation/               # Rutas y NavHost
│       ├── domain/model/                 # Modelos de dominio
│       ├── utils/                        # Utilidades (fechas, imágenes, estados)
│       └── service/                      # Servicio FCM
│
└── server/                               # SERVIDOR NODE.JS (fuente primaria)
    ├── database/rejuega_schema.sql       # Esquema MySQL + datos semilla
    ├── src/
    │   ├── models/                       # Consultas SQL
    │   ├── controllers/                  # Lógica de negocio y códigos HTTP
    │   ├── routes/                       # Endpoints REST
    │   └── middleware/                   # (extensible)
    ├── server.js                         # Punto de entrada (escucha en 0.0.0.0)
    ├── db.js                             # Pool de conexiones MySQL
    ├── .env.example                      # Plantilla de variables (sí se sube)
    └── .env                              # Tus variables locales (NO se sube)
```

---

## 9. Endpoints disponibles

Base: `http://localhost:3000` (o tu IP local).

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/materiales` | Lista materiales (huella ecológica) |
| GET | `/api/materiales/:id` | Material por ID (404 si no existe) |
| POST | `/api/materiales` | Crear material (400 si faltan campos) |
| GET | `/api/campaigns` | Lista campañas (`?estado=Activa`) |
| GET | `/api/campaigns/:id` | Campaña por ID |
| POST | `/api/campaigns` | Crear campaña |
| PUT | `/api/campaigns/:id` | Editar campaña |
| PUT | `/api/campaigns/:id/estado` | Cerrar/activar campaña |
| GET | `/api/donations` | Lista donaciones (`?user_id=`, `?campaign_id=`) |
| POST | `/api/donations` | Registrar donación |
| PUT | `/api/donations/:id/estado` | Avanzar/rechazar estado (trazabilidad) |
| GET | `/api/users` | Lista usuarios |
| PUT | `/api/users/:uid` | Actualizar perfil |
| GET | `/api/delivery-points` | Puntos fijos de recojo |
| POST | `/api/delivery-points` | Crear punto fijo |
| GET | `/api/pickup-events` | Eventos de recojo (`?campaign_id=`) |
| POST | `/api/pickup-events` | Crear evento |
| GET | `/api/campaign-gallery/campaign/:id` | Evidencias de una campaña |
| POST | `/api/campaign-gallery` | Subir evidencia |
| GET | `/api/notifications/user/:uid` | Notificaciones de un usuario |
| POST | `/api/notifications` | Crear notificación |

---

## 10. Arquitectura y fuentes de datos

```text
[ UI Jetpack Compose ]
        ↓
[ ViewModel (StateFlow) ]
        ↓
[ Repository ]
   ├─ 1º) SERVIDOR NODE + MySQL   → fuente primaria (cliente-servidor)
   ├─ 2º) Firebase Firestore      → respaldo en la nube
   └─ 3º) Room (SQLite local)     → caché offline
```

- **Autenticación:** Firebase Auth (login, registro, cambio de contraseña).
- **Imágenes:** Cloudinary (la app sube y guarda la URL en el servidor).
- **Notificaciones push:** Firebase Cloud Messaging.
- **Mapas:** osmdroid / OpenStreetMap (sin API key).
- **Offline:** si el servidor no responde, la app lee de Room/Firestore y sigue funcionando.

---

## 11. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `jlink.exe does not exist` al compilar | Gradle usa el JRE de VS Code | Sección 6.2 (cambiar JVM a 21/17) |
| Pruebas del servidor salen **OFF** | Servidor apagado o URL mal | `npm run dev` y revisar `ApiConfig.kt` |
| `OFF` solo en emulador | URL con `localhost` | Usar `http://10.0.2.2:3000/` |
| `OFF` solo en celular | Firewall o distinta Wi-Fi | Sección 5 (regla de firewall + misma red) |
| `EADDRINUSE: port 3000` | Puerto ocupado | Cierra la otra instancia o cambia `PORT` en `.env` |
| Error CORS en navegador | Origen no permitido | `ALLOWED_ORIGINS=*` en `.env` y reiniciar |
| `Cleartext HTTP not permitted` | Falta permiso en Manifest | Sección 6.4 |
| Gradle cache corrupto | Caché de configuración | `.\gradlew --stop` + `.\gradlew clean` + Invalidate Caches |
| No carga el mapa | Sin User-Agent osmdroid | Verificar `Configuration` en `ReJuegaApp.kt` |
| Login falla | Cuenta no existe en Firebase | Registrar cuenta nueva desde la app |

---

## 12. Buenas prácticas Git

- **Nunca subas:** `.env`, `server/node_modules`, contraseñas, claves privadas.
- **Ramas:** trabaja en `feature/nombre-corto` y haz merge a `main` con pull request.
- **Commits convencionales:**
    - `feat: ...` nueva funcionalidad
    - `fix: ...` corrección de error
    - `docs: ...` documentación
    - `refactor: ...` cambio sin cambiar comportamiento
- **Antes de hacer push:**
    1. `npm run dev` y prueba 1 endpoint.
    2. Build de Android Studio sin errores.
- **Mensajes claros en español o inglés, pero consistentes.**

---

## 13. Comandos útiles

```bash
# Servidor
cd server
npm install          # primera vez
npm run dev          # desarrollo con auto-reinicio
npm start            # producción

# Base de datos
mysql -u root -p < database/rejuega_schema.sql   # recrear BD desde cero

# Android
.\gradlew clean
.\gradlew build

# Red
ipconfig                                  # IP local de tu PC
New-NetFirewallRule -DisplayName "Node.js ReJuega API" -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow
```

---

## 14. Datos semilla incluidos

El script `rejuega_schema.sql` carga automáticamente:

| Tabla | Registros | Detalle |
|---|---|---|
| users | 8 | 2 administradores + 6 donantes |
| materials | 7 | Con imágenes y kg de CO₂ |
| campaigns | 5 | 4 activas + 1 cerrada |
| donations | 10 | En todos los estados de trazabilidad |
| donation_history | 24 | Trazabilidad completa |
| delivery_points | 3 | Puntos fijos en Trujillo |
| pickup_events | 3 | Eventos ligados a campañas |
| campaign_gallery | 5 | Evidencias fotográficas |
| notifications | 5 | Leídas y no leídas |

---

## 🤝 Responsables y contacto

| Área | Responsable | Contacto |
|---|---|---|
| Backend Node/MySQL | *(nombre)* | *(correo)* |
| App Android | *(nombre)* | *(correo)* |
| Firebase / Cloudinary | *(nombre)* | *(correo)* |
| Documentación | *(nombre)* | *(correo)* |

---

*Última actualización del documento: noviembre 2026 — Proyecto ReJuega, curso de Aplicaciones Móviles.*