# 🧸 ReJuega — Guía de Instalación y Configuración para el Equipo

Aplicación Android para la gestión trazable de donaciones de juguetes infantiles,
con arquitectura **cliente-servidor** (Node.js + Express + MySQL), respaldo
**Firebase Firestore / Room** para modo offline, y **sincronización automática
Firestore → MySQL al arrancar el servidor**.

📌 **Lee este documento completo antes de intentar ejecutar el proyecto.**
La mayoría de errores comunes están en la sección [13. Solución de problemas](#13-solución-de-problemas).

> 📅 Última actualización: noviembre 2026 — estado del proyecto justo antes de la migración a Flutter.

## 📋 Contenido

1. [Requisitos previos](#1-requisitos-previos)
2. [Clonar el repositorio](#2-clonar-el-repositorio)
3. [Archivos ignorados esenciales (pedir por .zip)](#3-archivos-ignorados-esenciales-pedir-por-zip)
4. [Configurar la base de datos MySQL](#4-configurar-la-base-de-datos-mysql)
5. [Configurar el servidor Node.js](#5-configurar-el-servidor-nodejs)
6. [Permitir conexiones desde un celular físico](#6-permitir-conexiones-desde-un-celular-físico)
7. [Configurar la app Android](#7-configurar-la-app-android)
8. [Ejecutar todo el sistema](#8-ejecutar-todo-el-sistema)
9. [Estructura del proyecto](#9-estructura-del-proyecto)
10. [Endpoints disponibles](#10-endpoints-disponibles)
11. [Arquitectura y fuentes de datos](#11-arquitectura-y-fuentes-de-datos)
12. [Sistema de roles](#12-sistema-de-roles)
13. [Scripts de sincronización y respaldo](#13-scripts-de-sincronización-y-respaldo)
14. [Solución de problemas](#14-solución-de-problemas)
15. [Buenas prácticas Git](#15-buenas-prácticas-git)
16. [Comandos útiles](#16-comandos-útiles)
17. [Datos semilla incluidos](#17-datos-semilla-incluidos)

---

## 1. Requisitos previos

| Herramienta | Versión probada | Comando de verificación |
|---|---|---|
| **Android Studio** | 2024.x o superior | — |
| **JDK** | 17 o 21 (incluido en Android Studio) | Ver sección 7.2 |
| **Node.js** | v18+ (probado en v24.16.0) | `node --version` |
| **npm** | 9+ (probado en 11.13.0) | `npm --version` |
| **MySQL** | 8.0.x (probado en 8.0.46) | `mysql --version` |
| **MySQL Workbench** | 8.x (opcional pero recomendado) | — |
| **Git** | Cualquier versión reciente | `git --version` |

⚠️ **No necesitas instalar Express ni firebase-admin globalmente.** Se instalan por proyecto con `npm install`.

---

## 2. Clonar el repositorio

```bash
git clone https://github.com/JoseReyesR/ReJuega.git
cd ReJuega
```

Verifica que tengas estas carpetas principales:

```text
ReJuega/
├── app/          → Código de la app Android (Kotlin + Jetpack Compose)
├── server/       → Servidor Node.js + Express + scripts SQL y de sync
└── README.md     → Este documento
```

> 🌿 Ramas: el desarrollo estable vive en `main`/`develop_server`. La rama `flutter-migration` es experimental: **no la uses** salvo que estés en el equipo de Flutter.

---

## 3. Archivos ignorados esenciales (pedir por .zip)

El `.gitignore` protege secretos, por lo que **algunos archivos necesarios NO están en el repo**.
Pídeselos al líder del equipo en un **.zip por canal privado** (nunca por Git):

| Archivo | ¿Obligatorio? | Para qué sirve |
|---|---|---|
| `server/firebase-service-account.json` | ✅ **Sí, crítico** | Permite al servidor escribir en Firestore (scripts de sync y cambio de roles) |
| `server/.env` | 🔶 Recomendado | Tus variables locales (contraseña de MySQL). También puedes crearlo desde `.env.example` |
| `app/google-services.json` | ⚠️ Solo si tu clon no lo tiene | Configuración de Firebase de la app Android |

Verifica si te falta algo:

```bash
git check-ignore -v server/firebase-service-account.json   # debe aparecer ignorado
git ls-files app/google-services.json                      # si no imprime nada, pídelo en el zip
```

❌ **NO pidas ni compartas:** `node_modules/` (se regenera con `npm install`).

📥 **Al recibir el zip:** coloca `firebase-service-account.json` dentro de `server/` y `.env` dentro de `server/`.

---

## 4. Configurar la base de datos MySQL

### 4.1 Asegúrate de que MySQL esté corriendo

- Windows: abre `services.msc` y verifica que el servicio `MySQL80` esté **En ejecución**.
- O abre MySQL Workbench y conecta a tu instancia local.

### 4.2 Ejecutar el script de esquema y datos semilla

El script crea la base `rejuega_db`, las 9 tablas y los datos semilla base.

**Opción A — MySQL Workbench (recomendada):**
1. Abre `server/database/rejuega_schema.sql` en Workbench.
2. Clic en el ícono de **rayo (⚡ Execute)**.

**Opción B — Consola:**

```bash
cd server
mysql -u root -p < database/rejuega_schema.sql
```

### 4.3 Verificar

```sql
USE rejuega_db;
SHOW TABLES;
SELECT COUNT(*) FROM users;      -- esperado base: 8
SELECT COUNT(*) FROM campaigns;  -- esperado base: 5
SELECT COUNT(*) FROM materials;  -- esperado: 7
```

> 💡 **Importante:** estos son los conteos **base del script**. Cuando arranques el servidor por primera vez, la **auto-reconciliación** copiará desde Firestore los datos de prueba del equipo (campañas, donaciones, usuarios extra), por lo que los conteos finales serán mayores. Es el comportamiento esperado.

🔐 Si tu usuario root tiene contraseña distinta, solo ajústala en tu `.env` local (paso 5.2). **Nunca la subas a Git.**

---

## 5. Configurar el servidor Node.js

### 5.1 Instalar dependencias

```bash
cd server
npm install
```

Instala: `express`, `mysql2`, `cors`, `dotenv`, `uuid`, `firebase-admin` y `nodemon` (dev).

### 5.2 Crear tu archivo `.env`

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

🚫 **REGLA DE ORO:** el `.env` **NUNCA se sube a Git** (ya está en `.gitignore`).

### 5.3 Colocar la clave de servicio de Firebase

Copia el archivo `firebase-service-account.json` (del zip del líder) dentro de `server/`.

Sin este archivo fallarán: los scripts de sincronización y el cambio de roles desde el servidor.

### 5.4 Iniciar el servidor

```bash
npm run dev
```

Salida esperada:

```text
✅ Conectado a MySQL: rejuega_db
🚀 Servidor corriendo en http://localhost:3000
🔄 Auto-reconciliación Firestore → MySQL iniciada...
✅ Auto-reconciliación completada
```

> 💡 La **auto-reconciliación** integra a MySQL todo lo que exista en Firestore y falte en MySQL (útil tras una caída del servidor). Es idempotente: puede correr muchas veces sin duplicar datos.

### 5.5 Verificar en el navegador

- `http://localhost:3000` → JSON de estado de la API.
- `http://localhost:3000/api/materiales` → lista 7 materiales.

💡 El servidor escucha en `0.0.0.0` para permitir conexiones desde emulador y celulares en la misma red.

---

## 6. Permitir conexiones desde un celular físico

Solo necesario si pruebas en **celular físico** (el emulador no lo necesita).

### 6.1 Abrir el puerto 3000 en el Firewall de Windows

PowerShell **como administrador**:

```powershell
New-NetFirewallRule -DisplayName "Node.js ReJuega API" -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow
```

### 6.2 Conocer la IP de tu PC

```powershell
ipconfig
```

Copia la **Dirección IPv4** (ej. `192.168.1.100`).

### 6.3 Configurar la app para celular físico

Edita `app/src/main/java/pe/rejuega/app/data/remote/ApiConfig.kt`:

```kotlin
const val NODE_BASE_URL = "http://TU_IP_LOCAL:3000/"   // ej. http://192.168.1.100:3000/
```

📶 Tu PC y tu celular deben estar en la **misma red Wi-Fi**.

**Para emulador**, en cambio:

```kotlin
const val NODE_BASE_URL = "http://10.0.2.2:3000/"
```

---

## 7. Configurar la app Android

### 7.1 Abrir el proyecto

Android Studio → **Open** → selecciona la carpeta raíz `ReJuega`.
Espera el **Gradle Sync** inicial (puede tardar varios minutos).

### 7.2 ⚠️ FIX conocido: error de `jlink.exe`

Si al compilar ves:

```text
jlink executable ...\redhat.java...\jre\...\bin\jlink.exe does not exist
```

Solución:
1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**.
2. En **Gradle JVM criteria → Version**, cambia a **21** (o 17).
3. **Apply → OK** → **File → Sync Project with Gradle Files**.

Si persiste, agrega en `gradle.properties` (raíz):

```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

### 7.3 Firebase

- `app/google-services.json` debe existir (en el repo o desde el zip del líder).
- Si Android Studio lo pide, acepta actualizar los servicios de Google.

### 7.4 Cloudinary (subida de imágenes)

Las constantes deben coincidir con la cuenta del equipo:

- `app/.../data/remote/DonationRepository.kt`
- `app/.../data/repository/CampaignGalleryRepository.kt`

```kotlin
private const val CLOUD_NAME = "NOMBRE_DE_CLOUD_DEL_EQIPO"
private const val UPLOAD_PRESET = "rejuega_preset"
```

📣 Pídele al líder el `CLOUD_NAME` si ves `TU_CLOUD_NAME`.

### 7.5 Tráfico sin cifrar (HTTP local)

Verifica que `AndroidManifest.xml` contenga:

```xml
<application
    android:usesCleartextTraffic="true"
    ... >
```

---

## 8. Ejecutar todo el sistema

**Orden de encendido:**
1. **MySQL** corriendo (servicio activo).
2. **Servidor Node:** `cd server` → `npm run dev`.
3. **App Android:** botón **Run ▶** en Android Studio.

**Cuentas de prueba:**

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@rejuega.app | *(canal del equipo)* |
| Gestor | ana.torres@correo.com | *(canal del equipo)* |
| Donante | donante@rejuega.app | *(canal del equipo)* |

🔑 Por seguridad, las contraseñas **no se publican en este README**.
También puedes registrar una cuenta nueva desde la pantalla de Login (nace como Donante; un Admin puede promoverla).

**Prueba rápida de integración:**
Menú lateral (Admin/Gestor) → **Pruebas del Servidor** → **Ejecutar todas las pruebas**.
Debes ver tarjetas con códigos **200, 201, 400 y 404**.

---

## 9. Estructura del proyecto

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
│       │   ├── components/               # Menú lateral, tarjetas
│       │   └── navigation/               # Rutas y NavHost
│       ├── domain/model/                 # Modelos de dominio
│       ├── utils/                        # RefreshTrigger, fechas, imágenes
│       └── service/                      # Servicio FCM
│
└── server/                               # SERVIDOR NODE.JS (fuente primaria)
    ├── database/rejuega_schema.sql       # Esquema MySQL + datos semilla
    ├── scripts/
    │   ├── seed-to-firebase.js           # MySQL → Firestore (backup inicial)
    │   └── sync-from-firebase.js         # Firestore → MySQL (reconciliación)
    ├── src/
    │   ├── models/                       # Consultas SQL
    │   ├── controllers/                  # Lógica de negocio y códigos HTTP
    │   └── routes/                       # Endpoints REST
    ├── server.js                         # Entrada + auto-reconciliación al arrancar
    ├── db.js                             # Pool MySQL + Firestore (firebase-admin)
    ├── firebase-service-account.json     # ⚠️ IGNORADO: pedir por zip
    ├── .env.example                      # Plantilla de variables (sí se sube)
    └── .env                              # ⚠️ IGNORADO: tus variables locales
```

---

## 10. Endpoints disponibles

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
| DELETE | `/api/campaigns/:id` | Eliminar campaña (cascada) |
| GET | `/api/donations` | Lista donaciones (`?user_id=`, `?campaign_id=`) |
| GET | `/api/donations/:id` | Donación por ID + historial |
| POST | `/api/donations` | Registrar donación |
| PUT | `/api/donations/:id` | Editar donación (solo estado Pendiente) |
| PUT | `/api/donations/:id/estado` | Avanzar/rechazar estado (trazabilidad) |
| DELETE | `/api/donations/:id` | Eliminar donación (solo Pendiente) |
| GET | `/api/users` | Lista usuarios |
| GET | `/api/users/:uid` | Usuario por UID |
| POST | `/api/users/sync` | Sincronizar usuario desde Firebase Auth |
| PUT | `/api/users/:uid` | Actualizar perfil |
| PUT | `/api/users/:uid/rol` | Cambiar rol (**solo Administrador**) |
| GET | `/api/delivery-points` | Puntos fijos de recojo |
| POST | `/api/delivery-points` | Crear punto fijo |
| PUT | `/api/delivery-points/:id` | Editar punto fijo |
| GET | `/api/pickup-events` | Eventos de recojo (`?campaign_id=`) |
| POST | `/api/pickup-events` | Crear evento |
| PUT | `/api/pickup-events/:id` | Editar evento |
| GET | `/api/campaign-gallery/campaign/:id` | Evidencias de una campaña |
| POST | `/api/campaign-gallery` | Subir evidencia |
| DELETE | `/api/campaign-gallery/:id` | Eliminar evidencia |
| GET | `/api/notifications/user/:uid` | Notificaciones de un usuario |
| GET | `/api/notifications/user/:uid/unread-count` | Conteo de no leídas |
| POST | `/api/notifications` | Crear notificación |
| PUT | `/api/notifications/:id/read` | Marcar una como leída |
| PUT | `/api/notifications/user/:uid/read-all` | Marcar todas como leídas |

---

## 11. Arquitectura y fuentes de datos

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

- **Lectura:** Node primero; si falla (server caído), Firestore; si falla, Room.
- **Escritura:** Node (MySQL) + copia best-effort en Firestore.
- **Auto-reconciliación:** al arrancar el servidor, todo lo que exista solo en Firestore se integra a MySQL (`INSERT IGNORE`, sin duplicar).
- **Autenticación:** Firebase Auth (login, registro, cambio de contraseña).
- **Imágenes:** Cloudinary (la app sube y guarda la URL en el servidor).
- **Notificaciones push:** Firebase Cloud Messaging.
- **Mapas:** osmdroid / OpenStreetMap (sin API key).
- **Offline:** con el servidor caído, la app sigue leyendo y mostrando datos desde Firestore/Room.

---

## 12. Sistema de roles

| Capacidad | Donante | Gestor | Administrador |
|---|:---:|:---:|:---:|
| Ver campañas y donar juguetes | ✅ | ✅ | ✅ |
| Mis donaciones, huella ecológica, impacto | ✅ | ✅ | ✅ |
| Panel de gestión (campañas, donaciones, puntos, eventos, galería) | ❌ | ✅ | ✅ |
| Ver lista de usuarios | ❌ | ✅ | ✅ |
| **Cambiar rol de un usuario** | ❌ | ❌ | ✅ |

Reglas del cambio de rol:
- Solo un **Administrador** puede cambiar roles (el servidor valida y responde 403 si no).
- El cambio **solo funciona con el servidor encendido** (actualiza MySQL **y** Firestore a la vez).
- Un Admin **no puede quitarse el rol a sí mismo** (protección contra quedar sin admins).
- Con el servidor caído, la app muestra: *"Servidor no disponible. No puedes cambiar roles…"*.

---

## 13. Scripts de sincronización y respaldo

| Comando | Dirección | Cuándo usarlo |
|---|---|---|
| *(automático al arrancar)* | Firestore → MySQL | Siempre: el servidor se auto-reconcilia al iniciar |
| `npm run sync:from-firebase` | Firestore → MySQL | Manual, tras una caída larga o para forzar reconciliación |
| `npm run seed:to-firebase` | MySQL → Firestore | Una vez, para sembrar el backup inicial desde MySQL |

Notas:
- Todos son **idempotentes** (`INSERT IGNORE` / `set()` por ID): correrlos varias veces no duplica datos.
- Requieren `firebase-service-account.json` en `server/` (ver sección 3).

---

## 14. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `jlink.exe does not exist` al compilar | Gradle usa el JRE de VS Code | Sección 7.2 (JVM 21/17) |
| Pruebas del servidor salen **OFF** | Servidor apagado o URL mal | `npm run dev` y revisar `ApiConfig.kt` |
| OFF solo en emulador | URL con `localhost` | Usar `http://10.0.2.2:3000/` |
| OFF solo en celular | Firewall o distinta Wi-Fi | Sección 6 |
| `EADDRINUSE: port 3000` | Puerto ocupado | Cierra la otra instancia o cambia `PORT` |
| `Cannot GET /` | Falta ruta raíz o archivo equivocado | Verifica `server.js` y prueba `/api/materiales` |
| `Cannot read properties of undefined (reading 'cert')` | Falta `firebase-admin` o la clave de servicio | `npm install firebase-admin` + colocar `firebase-service-account.json` |
| Push bloqueado por GitHub (GH013, secret) | Se commiteó la clave de servicio | Sacarla del historial (rebase) y rotar la clave |
| Fecha "31/12/1969" en la app | `fecha_registro` nula o sin normalizar | Reiniciar el servidor (normalización `aMillis`); verificar MySQL |
| El rol vuelve a Donante al re-loguear | Cambio de rol hecho con server caído | Cambiar roles solo con server ON |
| Cleartext HTTP not permitted | Falta permiso en Manifest | Sección 7.5 |
| Gradle cache corrupto | Caché de configuración | `.\gradlew --stop` + `clean` + Invalidate Caches |
| No carga el mapa | Sin User-Agent osmdroid | Verificar `Configuration` en `ReJuegaApp.kt` |
| Login falla | Cuenta no existe en Firebase | Registrar cuenta nueva desde la app |

---

## 15. Buenas prácticas Git

- **Nunca subas:** `.env`, `firebase-service-account.json`, `node_modules`, contraseñas, claves privadas.
- **Archivos esenciales ignorados:** se comparten por **.zip privado** (sección 3), jamás por Git.
- **Ramas:** trabaja en `feature/nombre-corto` y haz merge con pull request.
- **Commits convencionales:** `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`.
- **Antes de hacer push:**
  - `npm run dev` y prueba 1 endpoint.
  - Build de Android Studio sin errores.
  - `git status` para confirmar que no vas a subir ignorados esenciales.

---

## 16. Comandos útiles

```bash
# Servidor
cd server
npm install                 # primera vez
npm run dev                 # desarrollo con auto-reinicio y auto-reconciliación
npm start                   # producción

# Sincronización
npm run sync:from-firebase  # Firestore → MySQL (manual)
npm run seed:to-firebase    # MySQL → Firestore (backup inicial)

# Base de datos
mysql -u root -p < database/rejuega_schema.sql   # recrear BD desde cero

# Android
.\gradlew clean
.\gradlew build

# Red
ipconfig
New-NetFirewallRule -DisplayName "Node.js ReJuega API" -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow
```

---

## 17. Datos semilla incluidos

El script `rejuega_schema.sql` carga la **semilla base**:

| Tabla | Registros base | Detalle |
|---|---|---|
| users | 8 | 2 administradores + 1 gestor + 5 donantes |
| materials | 7 | Con imágenes y kg de CO₂ |
| campaigns | 5 | 4 activas + 1 cerrada |
| donations | 10 | En todos los estados de trazabilidad |
| donation_history | 24 | Trazabilidad completa |
| delivery_points | 3 | Puntos fijos en Trujillo |
| pickup_events | 3 | Eventos ligados a campañas |
| campaign_gallery | 5 | Evidencias fotográficas |
| notifications | 5 | Leídas y no leídas |

> 💡 **Tras el primer arranque del servidor**, la auto-reconciliación agrega desde Firestore los datos de prueba del equipo (campañas, donaciones, puntos y usuarios adicionales creados en sesiones reales), por lo que los conteos finales serán mayores. Ambos conjuntos conviven sin duplicarse.

---

## 🤝 Responsables y contacto

| Área | Responsable | Contacto |
|---|---|---|
| Backend Node/MySQL | *(nombre)* | *(correo)* |
| App Android | *(nombre)* | *(correo)* |
| Firebase / Cloudinary | *(nombre)* | *(correo)* |
| Documentación | *(nombre)* | *(correo)* |

*Última actualización del documento: noviembre 2026 — Proyecto ReJuega, curso de Aplicaciones Móviles.*