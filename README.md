VexorGym es una aplicación móvil multiplataforma para registrar y consultar entrenamientos de gimnasio de forma simple y rápida.

Está pensada principalmente para personas que ya tienen experiencia entrenando y prefieren crear sus propias rutinas en lugar de utilizar rutinas guiadas. El objetivo es facilitar el seguimiento del rendimiento anterior de cada ejercicio para aplicar sobrecarga progresiva de forma sencilla.

## ✨ Funcionalidades

* Registro e inicio de sesión de usuarios.
* Creación y organización de ejercicios por día de la semana.
* Reordenamiento de ejercicios mediante drag & drop.
* Registro de sesiones de entrenamiento.
* Registro de series, peso y repeticiones.
* Visualización de la última serie realizada de cada ejercicio.
* Eliminación de ejercicios, sesiones y series con confirmación.
* Persistencia de rutinas y orden de ejercicios.
* Persistencia de sesiones y series.
* Autenticación mediante Supabase.
* Aislamiento de datos entre usuarios mediante Row Level Security (RLS).
* Manejo de errores de conexión.
* Adaptación de fechas y horarios a la zona horaria local del dispositivo.

## 🛠️ Stack tecnológico

* **Kotlin Multiplatform (KMP)**
* **Compose Multiplatform**
* **Kotlin Coroutines / Flow**
* **MVVM**
* **Repository Pattern**
* **Supabase**

  * Authentication
  * PostgreSQL
  * Row Level Security (RLS)
* **Git / GitHub**

La lógica compartida y la interfaz principal se encuentran en `commonMain`, permitiendo reutilizar el código entre Android e iOS.

## 🏗️ Arquitectura

El proyecto utiliza principalmente **MVVM + Repository Pattern**.

flowchart TD
    UI[Compose Multiplatform UI]
    VM[ViewModels]
    REPO[GymRepository]
    REMOTE[RemoteGymRepository]
    MOCK[MockGymRepository]
    SUPA[Supabase]

    UI --> VM
    VM --> REPO
    REPO --> REMOTE
    REPO --> MOCK
    REMOTE --> SUPA

### UI

Las pantallas están implementadas con Compose Multiplatform y trabajan a partir del estado expuesto por los ViewModels.

Principales pantallas:

* Login / Registro
* Rutina semanal
* Detalle de ejercicio y registro de sesiones/series

### ViewModels

Los ViewModels gestionan el estado necesario para la UI y procesan los eventos provenientes de las pantallas.

La UI no accede directamente a Supabase.

### Repository

La interfaz `GymRepository` abstrae el acceso a los datos.

Esto permite mantener separada la capa de presentación de la implementación concreta utilizada para obtener y guardar información.

Actualmente existen implementaciones remota y mock, lo que facilita el desarrollo y las pruebas.

### Supabase

`RemoteGymRepository` se encarga de interactuar con Supabase y mantener sincronizado el estado de la aplicación con los datos persistidos.

## 🌐 Backend y persistencia

VexorGym utiliza **Supabase** como backend.

### Supabase Auth

Se utiliza para:

* registro de usuarios;
* inicio de sesión;
* restauración de sesión.

### PostgreSQL

La base de datos almacena la información relacionada con:

* usuarios;
* ejercicios;
* sesiones;
* series;
* rutinas.

El orden y organización de las rutinas se persisten para que los cambios sobrevivan al cierre de sesión y a la recarga de la aplicación.

### Row Level Security

La aplicación utiliza políticas de **Row Level Security (RLS)** para garantizar que un usuario solamente pueda acceder y modificar sus propios datos.

El identificador del usuario autenticado se utiliza como parte del control de acceso a los registros.

## 🔄 Gestión del estado

La aplicación utiliza un flujo de estado unidireccional:

Usuario
   ↓
Evento de UI
   ↓
ViewModel
   ↓
Repository
   ↓
Supabase
   ↓
Nuevo estado
   ↓
UI

Se tuvo especial cuidado con la identidad de los elementos de las listas de Compose para evitar que los estados de ejercicios o series se mezclen durante recomposiciones, inserciones o eliminaciones.

Las listas utilizan claves estables para preservar correctamente la identidad de cada elemento.

## 📶 Manejo de errores y conexión

La aplicación contempla errores de red y operaciones que requieren comunicación con Supabase.

Cuando una operación no puede completarse por falta de conexión:

* la aplicación no debe cerrarse;
* se informa al usuario mediante un mensaje comprensible;
* se evita dejar la interfaz en un estado inconsistente.

Además, se diferencian los errores de conectividad de otros errores de aplicación para facilitar su diagnóstico.

## 🤖 Uso de Inteligencia Artificial

El uso de herramientas de Inteligencia Artificial fue una parte fundamental del proceso de desarrollo y estuvo alineado con el objetivo del challenge.

Se utilizaron **Gemini y ChatGPT** como herramientas de pair programming y asistencia técnica para acelerar diferentes etapas del desarrollo.

Entre las tareas en las que se utilizaron se encuentran:

* generación y revisión de componentes de Compose Multiplatform;
* implementación y depuración de estados de UI;
* integración con Supabase;
* diseño y revisión de consultas SQL;
* configuración y revisión de políticas RLS;
* resolución de problemas de persistencia;
* implementación y depuración del drag & drop;
* análisis de errores de recomposición;
* manejo de errores de red;
* revisión de arquitectura y separación de responsabilidades.

### Proceso de trabajo con IA

La IA no se utilizó como mecanismo de generación automática sin revisión.

El proceso fue iterativo:

Problema
   ↓
Definición del comportamiento esperado
   ↓
Asistencia de IA
   ↓
Revisión del código generado
   ↓
Compilación
   ↓
Pruebas funcionales
   ↓
Detección de errores
   ↓
Nueva iteración / corrección


Un ejemplo concreto fue el reordenamiento de ejercicios. Las primeras implementaciones mantenían el nuevo orden únicamente en memoria. Después de detectar el problema durante las pruebas, se revisó el flujo completo entre UI, ViewModel, Repository y Supabase y se implementó la persistencia del orden.

También se utilizaron asistentes de IA para investigar y resolver problemas relacionados con la identidad de elementos en listas de Compose, evitando que los valores de diferentes series se mezclaran durante las recomposiciones.

La responsabilidad de validar el resultado, detectar errores y decidir qué soluciones adoptar permaneció del lado del desarrollador.

## 🧪 QA

Durante el desarrollo se probaron distintos escenarios funcionales, entre ellos:

* creación y eliminación de ejercicios;
* creación y eliminación de sesiones;
* creación y eliminación de series;
* reordenamiento de ejercicios;
* persistencia del orden de las rutinas;
* cierre y restauración de sesión;
* múltiples series dentro de una sesión;
* selección correcta de la última serie;
* comportamiento sin conexión;
* errores de red;
* aislamiento de datos entre usuarios.

Las pruebas manuales se utilizaron especialmente para validar los casos donde una implementación generada inicialmente por IA no se comportaba como se esperaba.

## 📱 Plataformas

El proyecto está desarrollado con Kotlin Multiplatform y Compose Multiplatform.

shared/
├── commonMain/
│   └── Kotlin compartido
└── iosMain/
    └── integración iOS

androidApp/
└── integración Android

iosApp/
└── proyecto Xcode

La mayor parte de la lógica y de la UI se mantiene en código compartido.

## 🚀 Cómo ejecutar el proyecto

### Requisitos

* Android Studio
* JDK compatible con la versión de Gradle/Kotlin utilizada por el proyecto
* Xcode para ejecutar la versión iOS
* Cuenta/proyecto de Supabase correctamente configurado

### Clonar el repositorio

git clone https://github.com/TU_USUARIO/VexorGym.git
cd VexorGym

### Configuración de Supabase

El proyecto requiere la configuración correspondiente al proyecto de Supabase utilizado por la aplicación.

No se deben incluir claves privadas ni secretos dentro del repositorio.

La configuración necesaria debe proporcionarse mediante el mecanismo de configuración utilizado por el proyecto.

Además, la base de datos debe contener las tablas y políticas RLS necesarias para ejecutar correctamente la aplicación.

### Android

Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar la configuración Android correspondiente.

### iOS

Abrir `iosApp` mediante Xcode, resolver las dependencias y ejecutar el proyecto en un simulador o dispositivo compatible.

## 📂 Estructura general

VexorGym/
├── shared/
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/
│       │       ├── ui/
│       │       ├── viewmodel/
│       │       ├── repository/
│       │       └── model/
│       └── iosMain/
│
├── androidApp/
│
├── iosApp/
│
├── gradle/
│
└── README.md

## 📸 Capturas

Se recomienda incluir capturas de las principales pantallas:

* Login
  
  <img width="350" height="792" alt="image" src="https://github.com/user-attachments/assets/dc87763a-0db7-4719-9be7-efb93340389b" />
  
* Rutina semanal
  
  <img width="353" height="791" alt="image" src="https://github.com/user-attachments/assets/57d8c7ff-61bb-44b4-b9ae-9f016dbb4a63" />
  
* Detalle del ejercicio
  
  <img width="351" height="788" alt="image" src="https://github.com/user-attachments/assets/e91924a6-23c5-4789-bb66-6e8157eeadf2" />

* Eliminar ejercicio
  
  <img width="353" height="790" alt="image" src="https://github.com/user-attachments/assets/32af75b3-5879-4ec9-a278-dda2c4e880ba" />

## 📦 APK

La versión Android instalable se encuentra disponible en la sección **Releases** de este repositorio.

[Descargar última versión](https://github.com/TU_USUARIO/VexorGym/releases/latest)

## 🔮 Posibles mejoras futuras

Entre las posibles mejoras futuras se encuentran:

* Poder utilizar la aplicación de manera offline

## 👨‍💻 Autor

**Samuel Gallardo**

Proyecto desarrollado como parte del challenge técnico de **AranguriApps**.
