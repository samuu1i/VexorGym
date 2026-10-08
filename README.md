# VexorGym 🏋️

VexorGym es una aplicación móvil multiplataforma orientada a personas que ya tienen experiencia entrenando y prefieren una herramienta simple para registrar y consultar sus entrenamientos.

La aplicación está pensada alrededor de un concepto sencillo: en lugar de enseñar al usuario cómo entrenar, le permite consultar rápidamente cómo fue su entrenamiento anterior y registrar nuevas sesiones, series y ejercicios para aplicar sobrecarga progresiva.

El proyecto fue desarrollado como parte del **Challenge Técnico - Software Engineer Mobile de AranguriApps 2026**.

---

## 🎯 Objetivo

VexorGym busca resolver una necesidad concreta:

> Registrar entrenamientos y consultar rápidamente el historial reciente para poder progresar en cargas y repeticiones sin una interfaz innecesariamente compleja.

La aplicación permite gestionar:

* Ejercicios.
* Grupos musculares.
* Rutina semanal.
* Sesiones de entrenamiento.
* Series con peso y repeticiones.
* Historial de entrenamientos.
* Persistencia local para utilizar parte de la aplicación sin conexión.
* Sincronización de datos con Supabase al recuperar Internet.

---

## ✨ Funcionalidades

### Gestión de ejercicios

* Crear ejercicios.
* Asignar un grupo muscular.
* Agregar ejercicios a un día de la rutina semanal.
* Consultar ejercicios por grupo muscular.
* Eliminar ejercicios.
* Iconos personalizados para los grupos musculares.

Los grupos musculares no requieren una tabla independiente en Supabase: cada ejercicio mantiene su `muscle_group` y la aplicación utiliza ese valor para clasificarlo visualmente.

### Rutina semanal

La rutina permite organizar los ejercicios por día de la semana y consultar rápidamente el entrenamiento planificado.

La información de la rutina se mantiene asociada al usuario mediante Supabase.

### Sesiones y series

Cada ejercicio puede tener múltiples sesiones, y cada sesión puede contener múltiples series.

La relación conceptual es:

```text
Ejercicio
    │
    ├── Sesión
    │     ├── Serie
    │     ├── Serie
    │     └── Serie
    │
    └── Sesión
          ├── Serie
          └── Serie
```

Una serie registra principalmente peso y repeticiones.

### Funcionamiento offline

La aplicación utiliza **SQLDelight + SQLite** como almacenamiento local.

Actualmente se soporta:

* Lectura offline de la rutina.
* Lectura offline de ejercicios.
* Lectura offline de sesiones y series.
* Creación de sesiones sin Internet.
* Creación de series sin Internet.
* Eliminación offline de series.
* Eliminación offline de sesiones.
* Eliminación offline de ejercicios.
* Persistencia local después de cerrar y volver a abrir la aplicación.
* Sincronización con Supabase al recuperar conexión.

El flujo general es:

```text
                    ┌──────────────┐
                    │   Supabase   │
                    └──────┬───────┘
                           │
                    sincronización
                           │
┌──────┐            ┌──────▼───────┐
│  UI  │ ◄───────── │  Repository  │
└──┬───┘             └──────┬───────┘
   │                        │
   │                  ┌─────▼─────┐
   └─────────────────►│ SQLDelight│
                      │  / SQLite │
                      └───────────┘
```

La lectura local permite que la aplicación siga mostrando información previamente almacenada aunque Supabase no esté disponible.

Cuando vuelve Internet, las operaciones pendientes se sincronizan utilizando los mismos identificadores locales para evitar duplicados.

---

## 🧱 Arquitectura

El proyecto utiliza **Kotlin Multiplatform + Compose Multiplatform**, compartiendo la mayor parte de la lógica entre plataformas.

La aplicación se estructura aproximadamente de la siguiente manera:

```text
UI
│
├── WeeklyRoutineScreen
└── ExerciseDetailScreen
        │
        ▼
ViewModel
        │
        ▼
Repository
        │
        ├───────────────► Supabase
        │
        └───────────────► LocalGymDataSource
                                │
                                ▼
                            SQLDelight
                                │
                                ▼
                              SQLite
```

### Presentación

La interfaz está implementada con **Compose Multiplatform**.

Los ViewModels se encargan de mantener el estado que consume la UI y de coordinar las operaciones de negocio sin que las pantallas tengan que conocer directamente Supabase o SQLDelight.

### Repository

`RemoteGymRepository` centraliza la comunicación con el backend y coordina:

* consultas remotas;
* persistencia local;
* manejo de datos offline;
* sincronización;
* creación y eliminación de entidades.

Esto evita que la UI dependa directamente de la infraestructura de persistencia.

### Persistencia local

`LocalGymDataSource` encapsula el acceso a SQLDelight.

La base local incluye entidades para:

* ejercicios;
* sesiones;
* series;
* rutina del usuario.

Las consultas SQL están definidas mediante archivos `.sq` y SQLDelight genera las interfaces Kotlin correspondientes.

---

## 🗄️ Backend y datos

VexorGym utiliza **Supabase** como backend.

Supabase se utiliza para:

* autenticación;
* usuarios;
* ejercicios;
* sesiones;
* series;
* rutina semanal;
* persistencia remota;
* sincronización.

### Relaciones principales

```text
User
 │
 ├── Exercises
 │      │
 │      └── Sessions
 │             │
 │             └── Sets
 │
 └── Weekly Routine
          │
          └── Exercise IDs
```

Los registros locales se asocian al `user_id` correspondiente para evitar mezclar información entre diferentes cuentas en el mismo dispositivo.

---

## 🔐 Autenticación

La autenticación se realiza utilizando **Supabase Auth**.

El flujo incluye:

* Registro.
* Inicio de sesión.
* Persistencia de sesión.
* Recuperación de contraseña.
* Cambio de contraseña.
* Deep links asociados al proceso de recuperación.

Los enlaces inválidos o expirados se manejan sin provocar un cierre inesperado de la aplicación.

---

## 🎨 UI / UX

La interfaz está diseñada buscando simplicidad y velocidad de uso.

Se utilizaron recursos gráficos personalizados para los grupos musculares, por ejemplo:

```text
ic_pecho.png
ic_pierna.png
ic_bicep.png
ic_tricep.png
ic_espalda.png
ic_hombro.png
ic_gemelos.png
ic_femoral.png
ic_gluteo.png
```

Estos recursos se utilizan de forma consistente en las distintas vistas donde se representa visualmente el grupo muscular.

La aplicación utiliza una navegación clara entre:

* autenticación;
* rutina semanal;
* selección/consulta de ejercicios;
* detalle del ejercicio;
* sesiones y series.

---

## 🤖 Uso de Inteligencia Artificial

El uso de IA fue una parte central del desarrollo y siguió el enfoque solicitado por el challenge.

### Herramientas utilizadas

**Android Studio Gemini Agent**

Se utilizó principalmente para:

* generar y modificar código;
* implementar funcionalidades;
* realizar refactors;
* trabajar sobre Compose Multiplatform;
* integrar Supabase;
* implementar SQLDelight;
* analizar errores de compilación;
* diagnosticar crashes;
* realizar cambios de UI.

**ChatGPT**

Se utilizó como herramienta de apoyo para:

* diseñar la arquitectura;
* dividir funcionalidades en etapas;
* elaborar prompts específicos para el agente;
* revisar decisiones de implementación;
* analizar errores y logs;
* identificar posibles problemas de sincronización;
* definir estrategias de persistencia offline;
* realizar QA sobre los cambios generados.

### Proceso de trabajo con IA

La IA no fue utilizada como mecanismo de copy-paste directo.

El flujo de trabajo fue:

```text
Definir funcionalidad
       ↓
Diseñar estrategia
       ↓
Crear prompt específico
       ↓
Agente implementa
       ↓
Revisar cambios
       ↓
Compilar
       ↓
Probar funcionalidad
       ↓
Detectar errores
       ↓
Corregir
       ↓
Commit
```

Una parte importante del trabajo consistió en detectar problemas introducidos durante la generación automática, especialmente relacionados con:

* recomposición de Compose;
* navegación;
* persistencia local;
* esquemas de SQLDelight;
* sincronización Supabase/SQLite;
* aislamiento entre usuarios;
* operaciones offline.

---

## 🧪 QA y pruebas

La aplicación fue probada manualmente durante el desarrollo utilizando diferentes escenarios.

### Conectividad

Se probaron escenarios con:

* Internet disponible.
* Modo avión.
* Cierre y reapertura de la aplicación sin conexión.
* Recuperación de conexión.
* Sincronización posterior.

### Persistencia offline

Se verificó que los datos creados offline permanecieran después de:

```text
Crear información
    ↓
Cerrar aplicación
    ↓
Abrir nuevamente sin Internet
    ↓
Consultar información
```

### Sincronización

Se verificó que:

* sesiones creadas offline llegaran posteriormente a Supabase;
* series creadas offline llegaran posteriormente a Supabase;
* eliminaciones realizadas offline se sincronizaran;
* no se generaran duplicados;
* los datos locales pendientes no se perdieran cuando una operación de red fallaba.

### Cuentas

Se verificó el aislamiento de datos locales mediante `user_id`, de manera que los datos pertenecientes a una cuenta no sean utilizados por otra cuenta en el mismo dispositivo.

### Git

El desarrollo se realizó utilizando commits frecuentes y separados por funcionalidades, por ejemplo:

```text
Integracion SQLDelight para uso offline
Lecturas locales
Visualizacion local
escritura offline de series
escritura offline de sesiones
Sincronizar offline con supabase
eliminar offline sincronizado
eliminar ejercicio offline sincronizado
```

Esto permite identificar y recuperar puntos funcionales del desarrollo en lugar de depender de un único commit final.

---

## 📱 Plataformas

El proyecto utiliza:

* Kotlin Multiplatform.
* Compose Multiplatform.
* Android.
* iOS.

La lógica compartida se encuentra principalmente dentro del módulo `shared`.

La entrega del challenge incluye el **APK instalable de Android** solicitado.

La validación funcional para la entrega se realizó principalmente sobre Android.

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos

* Android Studio.
* JDK compatible con la configuración actual del proyecto.
* Android SDK.
* Git.
* Cuenta/proyecto de Supabase configurado.

### Clonar el proyecto

```bash
git clone <URL_DEL_REPOSITORIO>
cd VexorGym
```

### Configurar Supabase

Crear/configurar un proyecto de Supabase y proporcionar a la aplicación las credenciales utilizadas por el proyecto.

**No incluir credenciales privadas en el repositorio.**

La configuración debe contener como mínimo los datos necesarios para conectarse al proyecto Supabase utilizado por VexorGym.

### Ejecutar Android

Desde Android Studio:

```text
Seleccionar configuración Android
        ↓
Seleccionar dispositivo/emulador
        ↓
Run
```

También puede generarse el APK Debug mediante Gradle:

```powershell
.\gradlew :androidApp:assembleDebug
```

El APK generado puede instalarse en un dispositivo Android para realizar las pruebas funcionales.

---

## ⚠️ Limitaciones conocidas

### Creación de ejercicios sin Internet

La versión entregada permite utilizar gran parte de la aplicación offline, pero **la creación de ejercicios sin conexión no forma parte de la versión final entregada**.

La creación de ejercicios requiere conexión para persistirse en Supabase.

### Migraciones históricas de SQLDelight

La persistencia local utiliza SQLDelight/SQLite.

La infraestructura de migraciones históricas del esquema local se encuentra como una mejora pendiente de completar para cubrir de forma integral todas las actualizaciones futuras del esquema.

Por este motivo, durante el desarrollo puede ser necesario reinstalar la aplicación si una modificación del esquema local no está contemplada por una migración existente.

La versión entregada fue validada mediante instalación limpia del APK.

### Tests automatizados / CI

La versión entregada priorizó la implementación funcional y el QA manual dentro del tiempo disponible.

No se incluyó una suite amplia de tests unitarios ni un pipeline de CI completo.

---

## 🔮 Mejoras futuras

Algunas mejoras que podrían incorporarse posteriormente:

* Finalizar y ampliar las migraciones de SQLDelight para todas las evoluciones futuras del esquema.
* Soporte completo para creación de ejercicios offline.
* Tests unitarios y de integración para el Repository.
* Tests automatizados de sincronización offline.
* CI para compilación y validación automática.
* Mejoras adicionales en el manejo de conflictos entre dispositivos.
* Observabilidad y logging estructurado para operaciones de sincronización.
* Mejoras adicionales de accesibilidad.

---

## 📸 Capturas

### Login

`<img width="354" height="790" alt="image" src="https://github.com/user-attachments/assets/0106ea57-a394-4828-84d5-7ce742e03f66" />
`

### Rutina semanal

`<img width="323" height="719" alt="image" src="https://github.com/user-attachments/assets/fb6b77fd-bda9-4d61-93a1-69c67bfc1ff1" />
`

### Detalle del ejercicio

`<img width="324" height="721" alt="image" src="https://github.com/user-attachments/assets/f0f88b9b-a859-4353-a32c-fc7b0faf1374" />
`

---

## 📄 Licencia

Proyecto desarrollado con fines educativos y como parte del proceso de selección de AranguriApps.
