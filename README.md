# 🎯 Extreme Focus

**La mayoría de apps de bienestar digital se desactivan con un toque. Esta no.**

*Most digital wellbeing apps can be switched off in one tap. This one can't.*

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-24%2B-3DDC84?logo=android&logoColor=white)
![Room](https://img.shields.io/badge/Room-local%20only-FF6F00?logo=sqlite&logoColor=white)

[**🇪🇸 Español**](#-español) · [**🇬🇧 English**](#-english)

---

## 🇪🇸 Español

### El problema

Los bloqueadores de apps convencionales fallan por el mismo motivo: el usuario que instala el bloqueo y el usuario que quiere saltárselo son la misma persona, y el segundo siempre gana porque desactivarlo cuesta un toque.

**Extreme Focus** parte de otra idea: si quieres volver a abrir la app, tienes que *ganártelo*.

### ⚙️ Cómo funciona

| Mecanismo | Qué hace |
|---|---|
| ⏱️ **Límites diarios** | Minutos por app, leídos del uso real del sistema (`UsageStatsManager`) |
| 🚫 **Intercepción instantánea** | Detecta la apertura de una app bloqueada y te devuelve al inicio antes de que veas el feed |
| 🧗 **Retos de fricción** | Para desbloquear temporalmente hay que superar una prueba deliberadamente tediosa |
| 🔕 **Filtro de notificaciones** | Cancela las notificaciones de las apps bloqueadas: sin gancho, sin recaída |
| 🛡️ **Anti-sabotaje** | Si te pillan en Ajustes intentando desinstalarla o forzar su detención, te manda al inicio |
| 🌐 **Bypass por navegador** | Detecta intentos de abrir las mismas redes desde Chrome/Firefox y los corta |

### 🧗 Los retos de fricción

Superar uno concede una ventana temporal de acceso (3–15 min), nunca permanente.

- **Transcripción del manifiesto** — copiar un texto a mano, sin pegar ni errores
- **Toque del abismo** — mantener el dedo pulsado 60 segundos sin soltar
- **Rejilla monótona** — encontrar secuencias en una cuadrícula deliberadamente aburrida
- **Tarea de monotonía** — introducir 50 elementos a mano o resolver un puzzle lógico tedioso

La lógica es simple: si el impulso no sobrevive a dos minutos de aburrimiento, no era una necesidad real.

### 🏗️ Arquitectura

```
app/src/main/java/com/example/
├── data/local/       Room: apps monitorizadas + registro de bloqueos
├── domain/           Motor de recomendaciones por plataforma + motor de mensajes
├── service/          Accesibilidad · Monitor en primer plano · Overlay · Notificaciones
├── receiver/         Arranque/reinicio diario · Device Admin (anti-desinstalación)
└── ui/screens/       Compose: panel, selector, bloqueo, retos, auditoría, permisos
```

- **UI** — 100 % Jetpack Compose + Material 3, navegación por estado sellado (`AppScreen`)
- **Datos** — Room, **todo en local**: la app no envía nada a ningún servidor
- **Detección** — `UsageStatsManager` para el tiempo real de uso + servicio de accesibilidad para la intercepción inmediata
- **Persistencia del bloqueo** — servicio en primer plano con comprobación cada 3 s, más overlay de sistema (`TYPE_APPLICATION_OVERLAY`) que se dibuja sobre la app bloqueada

### 🔐 Permisos y por qué

Esta app pide permisos sensibles. Es deliberado y aquí está el motivo de cada uno:

| Permiso | Para qué | ¿Opcional? |
|---|---|---|
| Acceso al uso (`PACKAGE_USAGE_STATS`) | Saber cuántos minutos llevas realmente en cada app | ❌ Imprescindible |
| Servicio de accesibilidad | Detectar la apertura de una app en el instante en que ocurre | ⚠️ Sin él, la detección es más lenta |
| Superposición (`SYSTEM_ALERT_WINDOW`) | Dibujar el reto de fricción encima de la app bloqueada | ✅ Opcional |
| Acceso a notificaciones | Cancelar las notificaciones de apps bloqueadas | ✅ Opcional |
| Administrador de dispositivo | Dificultar la desinstalación impulsiva | ✅ Opcional |

> 🔒 **Privacidad:** todo se procesa y almacena en el dispositivo. No hay backend, no hay telemetría, no hay cuenta de usuario.

### 🎚️ Perfiles de restricción

Tres niveles preconfigurados que ajustan los límites de todas las apps a la vez, con minutos calibrados por plataforma:

| Perfil | Rango | Para quién |
|---|---|---|
| **Estricto** | 5–15 min | Corte radical contra el scroll infinito |
| **Equilibrado** | 15–30 min | Consumo consciente |
| **Flexible** | 30–60 min | Margen amplio antes de perder el foco |

### 🚧 Estado

Proyecto personal en desarrollo activo. Funcional en dispositivo, aún no publicado en Play Store.

---

## 🇬🇧 English

### The problem

Conventional app blockers fail for one reason: the person installing the block and the person trying to bypass it are the same person — and the second one always wins, because turning it off takes a single tap.

**Extreme Focus** takes a different approach: if you want back in, you have to *earn* it.

### ⚙️ How it works

| Mechanism | What it does |
|---|---|
| ⏱️ **Daily limits** | Per-app minute budgets, based on real system usage (`UsageStatsManager`) |
| 🚫 **Instant interception** | Detects a blocked app opening and sends you home before the feed loads |
| 🧗 **Friction challenges** | Temporary unlocks must be earned through a deliberately tedious task |
| 🔕 **Notification filter** | Cancels notifications from blocked apps — no hook, no relapse |
| 🛡️ **Anti-tamper** | Catches attempts to uninstall or force-stop it from Settings and bounces you home |
| 🌐 **Browser bypass** | Detects the same platforms being opened in Chrome/Firefox and cuts them off |

### 🧗 The friction challenges

Passing one grants a temporary access window (3–15 min), never a permanent one.

- **Manifesto transcription** — retype a text by hand, no pasting, no mistakes
- **Abyss touch** — hold your finger down for 60 seconds without releasing
- **Monotony grid** — find sequences in a deliberately boring grid
- **Monotony task** — enter 50 items manually or solve a tedious logic puzzle

The logic is simple: if the urge doesn't survive two minutes of boredom, it was never a real need.

### 🏗️ Architecture

```
app/src/main/java/com/example/
├── data/local/       Room: monitored apps + block event log
├── domain/           Per-platform recommendation engine + message engine
├── service/          Accessibility · Foreground monitor · Overlay · Notifications
├── receiver/         Boot & daily reset · Device Admin (anti-uninstall)
└── ui/screens/       Compose: dashboard, selector, block, challenges, audit, permissions
```

- **UI** — 100% Jetpack Compose + Material 3, sealed-state navigation (`AppScreen`)
- **Data** — Room, **fully local**: the app sends nothing to any server
- **Detection** — `UsageStatsManager` for real usage time + an accessibility service for instant interception
- **Block persistence** — foreground service polling every 3s, plus a system overlay (`TYPE_APPLICATION_OVERLAY`) drawn on top of the blocked app

### 🔐 Permissions and why

This app requests sensitive permissions. That's deliberate — here's the reason for each:

| Permission | Why | Optional? |
|---|---|---|
| Usage access (`PACKAGE_USAGE_STATS`) | Know how many minutes you've actually spent in each app | ❌ Required |
| Accessibility service | Detect an app opening the instant it happens | ⚠️ Without it, detection is slower |
| Overlay (`SYSTEM_ALERT_WINDOW`) | Draw the friction challenge on top of the blocked app | ✅ Optional |
| Notification access | Cancel notifications from blocked apps | ✅ Optional |
| Device admin | Make impulsive uninstalls harder | ✅ Optional |

> 🔒 **Privacy:** everything is processed and stored on-device. No backend, no telemetry, no user account.

### 🎚️ Restriction profiles

Three presets that adjust every app's limit at once, with per-platform calibrated minutes:

| Profile | Range | For whom |
|---|---|---|
| **Strict** | 5–15 min | Hard cut against infinite scroll |
| **Balanced** | 15–30 min | Mindful consumption |
| **Lenient** | 30–60 min | Wide margin before focus is gone |

### 🚧 Status

Personal project under active development. Working on-device, not yet published on the Play Store.

---

<sub>Built by [Bernabé Muñoz Peñas](https://www.linkedin.com/in/bernabemunozpenas/) · Kotlin · Jetpack Compose · Room</sub>
