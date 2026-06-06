# SRS - Especificación de Requisitos de Software

## 1. Introducción

### 1.1 Propósito del documento
Este documento define los requisitos funcionales y no funcionales de la aplicación móvil **Recipify**. Su objetivo es proporcionar una descripción completa de las capacidades del sistema, sirviendo como guía para el desarrollo, pruebas y mantenimiento del software bajo estándares de ingeniería de software.

### 1.2 Alcance de la app móvil
Recipify es una plataforma móvil diseñada para facilitar la gestión de recetas de cocina. Permite a los usuarios descubrir nuevas preparaciones mediante una API externa, buscar recetas específicas, gestionar una lista de favoritos local y contribuir con sus propias creaciones sincronizadas en la nube. La aplicación garantiza el acceso a la información incluso en condiciones de baja o nula conectividad mediante persistencia local robusta.

### 1.3 Público objetivo del documento
Este documento está dirigido al equipo de desarrollo, docentes y evaluadores académicos, futuros mantenedores del sistema y cualquier parte interesada en comprender la arquitectura y funcionalidades de la solución.

### 1.4 Definiciones, siglas y abreviaturas
*   **SRS**: Software Requirements Specification.
*   **RF / RNF**: Requerimiento Funcional / Requerimiento No Funcional.
*   **UI / UX**: User Interface / User Experience.
*   **API**: Application Programming Interface.
*   **CRUD**: Create, Read, Update, Delete.
*   **Auth**: Autenticación y Autorización.
*   **MVVM**: Model-View-ViewModel (Arquitectura implementada).
*   **Room**: Biblioteca de persistencia de Android sobre SQLite para datos locales.
*   **Firebase**: Suite de servicios en la nube (Firestore, Auth, Storage, Crashlytics).
*   **Retrofit**: Cliente HTTP para el consumo de servicios web RESTful.

### 1.5 Referencias usadas
*   `README.md`: Contexto general e integrantes.
*   `app/build.gradle.kts`: Configuración de dependencias y tecnologías.
*   `AndroidManifest.xml`: Declaración de componentes y permisos del sistema.
*   Código fuente: Clases de persistencia (`RecipeEntity`), modelos (`Recipe`) y Repositorios.
*   `docs/arquitectura.md`: Justificación técnica del stack.

## 2. Descripción general

### 2.1 Contexto del problema
La falta de tiempo y la dependencia constante de conectividad limitan el uso de aplicaciones de recetas convencionales. Recipify resuelve la inaccesibilidad de la información culinaria mediante un sistema de caché local y sincronización remota eficiente.

### 2.2 Usuario objetivo
Personas interesadas en la cocina que busquen una herramienta digital ágil para organizar sus recetas personales y descubrir nuevas ideas de manera rápida y accesible desde su dispositivo móvil.

### 2.3 Descripción de la solución móvil
Aplicación nativa para Android que implementa búsqueda reactiva, gestión de favoritos mediante persistencia local (Room) y almacenamiento en la nube (Firestore), garantizando la integridad de los datos y la experiencia de usuario offline.

### 2.4 Plataforma elegida
*   **Plataforma**: Android Nativo.
*   **Lenguaje**: Kotlin.
*   **UI**: XML con ViewBinding (Migración a Jetpack Compose: Recomendado/Pendiente).
*   **Arquitectura**: MVVM (Model-View-ViewModel) con patrón Repository.

### 2.5 Supuestos, restricciones y dependencias
*   **Supuestos**: El usuario tiene acceso a servicios de Google Play para el funcionamiento de Firebase.
*   **Restricciones**: Nivel de API mínimo 30 (Android 11).
*   **Dependencias**: Firebase Auth/Firestore/Storage, Room, Retrofit, Glide, Corrutinas de Kotlin.

## 3. Requerimientos funcionales

| ID | Requerimiento funcional | Prioridad | Criterios de aceptación |
| :--- | :--- | :--- | :--- |
| RF-01 | Registro de usuario | Alta | Dado que el usuario no tiene cuenta, cuando ingresa un email y password válidos, entonces se crea su perfil en Firebase Auth. |
| RF-02 | Inicio de sesión | Alta | Dado que el usuario tiene cuenta, cuando ingresa credenciales o usa Google, entonces accede a la pantalla principal. |
| RF-03 | Visualización de recetas destacadas | Alta | Dado que el usuario está en el Home, cuando la app carga, entonces se muestran recetas de la API y Firestore. |
| RF-04 | Búsqueda de recetas | Alta | Dado que el usuario busca un plato, cuando escribe en el buscador, entonces se filtran los resultados por nombre. |
| RF-05 | Visualización de detalles | Alta | Dado que el usuario selecciona una receta, cuando pulsa sobre ella, entonces se muestra la imagen, ingredientes y pasos. |
| RF-06 | Gestión de favoritos (Bookmarks) | Alta | Dado que el usuario ve una receta, cuando pulsa el botón de favorito, entonces se guarda en Room para acceso offline. |
| RF-07 | Creación de recetas propias | Media | Dado que el usuario completa el formulario de creación, cuando guarda, entonces la receta se sube a Firestore. |
| RF-08 | Importación desde JSON | Baja | Dado que el usuario tiene un archivo JSON compatible, cuando lo importa, entonces se procesa y añade a su recetario local. |
| RF-09 | Cierre de sesión | Media | Dado que el usuario pulsa "Log out", cuando confirma, entonces se cierra la sesión y vuelve al Login. |
| RF-10 | Configuración de perfil | Baja | Dado que el usuario accede a Profile Setup, cuando actualiza sus datos, entonces los cambios se guardan en su perfil. |
| RF-11 | Navegación entre secciones | Alta | Dado el menú inferior, cuando el usuario pulsa un icono, entonces cambia entre Home, Search, Saved y Profile. |
| RF-12 | Manejo de estados de carga | Media | Dado que una operación de red está en curso, cuando se inicia, entonces se muestra un indicador visual de progreso. |

## 4. Requerimientos no funcionales

| Categoría | Requerimiento no funcional | Criterio verificable |
| :--- | :--- | :--- |
| Usabilidad móvil | Interfaz intuitiva | El acceso a cualquier funcionalidad principal debe realizarse en un máximo de 3 clics. |
| Rendimiento | Tiempo de respuesta | Las consultas a la base de datos local Room deben resolverse en menos de 200ms. |
| Persistencia | Disponibilidad offline | El 100% de las recetas en la sección de favoritos deben ser legibles sin internet. |
| Disponibilidad parcial | Resiliencia de red | La app debe informar la falta de conexión sin cerrarse inesperadamente (Crash-free). |
| Seguridad | Autenticación delegada | El sistema no almacenará contraseñas localmente, delegando la seguridad a Firebase Auth. |
| Mantenibilidad | Arquitectura limpia | Uso estricto de ViewModels para desacoplar la lógica de negocio de las actividades. |
| Observabilidad | Trazabilidad de fallos | Los errores no controlados deben registrarse automáticamente en Firebase Crashlytics. |
| Accesibilidad | Soporte multilingüe | Uso de recursos `strings.xml` para soportar localización al español e inglés. |

## 5. Reglas de negocio

1.  **Validación**: No se permite el registro ni la creación de recetas con campos obligatorios vacíos.
2.  **Autoría**: Un usuario solo puede editar o eliminar recetas de las cuales es el autor original (validación vía UID).
3.  **Favoritos Únicos**: El sistema impide duplicar la misma receta en la tabla de `bookmarks` local.
4.  **Sesión Protegida**: Es obligatorio estar autenticado para añadir recetas, gestionar favoritos o editar el perfil.
5.  **Offline**: Las acciones que requieren internet deben informar al usuario si no hay conectividad mediante mensajes amigables.
6.  **Integridad**: Las recetas importadas vía JSON deben cumplir con la estructura del modelo `Recipe.kt`.

## 6. Modelo de datos

### 6.1 Entidades
*   **RecipeEntity** (Local): Entidad para Room que almacena ID, nombre, tiempo, categorías (JSON), ingredientes (JSON), área y pasos.
*   **BookmarkEntity** (Local): Tabla de relación que identifica las recetas favoritas del usuario.
*   **Recipe** (Remoto): Modelo de datos para intercambio con Firebase Firestore y la API externa.

### 6.2 Atributos

| Entidad | Atributo | Tipo de dato | Descripción | Obligatorio |
| :--- | :--- | :--- | :--- | :--- |
| Recipe | id | String | Identificador único de la receta | Sí |
| Recipe | name | String | Título o nombre del plato | Sí |
| Recipe | totalTimeMinutes| Long | Tiempo estimado de preparación | Sí |
| Recipe | description | String | Resumen informativo | Sí |
| Recipe | ingredients | List<Any> | Lista de ingredientes y cantidades | Sí |
| Recipe | body | String | Instrucciones detalladas de preparación | Sí |
| Recipe | userId | String | UID del autor en Firebase | Sí |
| Recipe | imageURL | String | Enlace a la imagen en la nube | No |

### 6.3 Relaciones
*   **Usuario - Receta (1:N)**: Un usuario puede crear múltiples recetas en Firestore.
*   **Receta - Favorito (1:1)**: Marcado local en Room mediante persistencia de ID en la tabla de marcadores.

### 6.4 Datos locales
Gestionados por **Room Database** (`AppDatabase.kt`), permitiendo que el catálogo de favoritos sea persistente y funcional sin conexión.

### 6.5 Datos remotos
Gestionados por **Firebase Firestore** para sincronización global y **Firebase Storage** para almacenamiento de imágenes.

### 6.6 Estructura en servicio remoto
*   Colección `recipes`: Documentos NoSQL que siguen el esquema de la clase `Recipe`.
*   Colección `users`: Perfiles de usuario gestionados tras el registro inicial.

## 7. Interfaces externas

### 7.1 Autenticación
*   **Firebase Auth**: Soporte para Email/Password y Google Sign-In.

### 7.2 Persistencia remota
*   **Cloud Firestore**: Base de datos remota NoSQL en tiempo real.

### 7.3 Reporte de fallos
*   **Firebase Crashlytics**: Implementado para monitorear la estabilidad del sistema.

### 7.4 APIs externas
*   **TheMealDB API**: Consumida vía Retrofit para poblar el catálogo de recetas internacionales.

### 7.5 Permisos del dispositivo
*   `android.permission.INTERNET`: Acceso a servicios en la nube.
*   `android.permission.ACCESS_NETWORK_STATE`: Detección de conectividad para manejo de estados offline.

## 8. Criterios de aceptación del proyecto

*   La aplicación se ejecuta sin fallos críticos en Android 11+.
*   El flujo de autenticación es funcional y seguro.
*   Las recetas se listan y buscan correctamente desde fuentes remotas.
*   El modo offline permite consultar los favoritos guardados en Room.
*   Se pueden crear recetas y visualizarlas en el feed tras la sincronización.
*   La arquitectura MVVM se mantiene consistente en todo el proyecto.

## 9. Matriz de trazabilidad

| RF | Pantalla relacionada | Capa/clase relacionada | Persistencia/API relacionada | Cómo se demuestra |
| :--- | :--- | :--- | :--- | :--- |
| RF-01/02 | Login / Register | Login/Register ViewModels | Firebase Auth | Registro e inicio de sesión con éxito |
| RF-03 | Home | HomeViewModel / RecipeAdapter | Firestore / MealDB | Feed principal con recetas dinámicas |
| RF-04 | Search | SearchViewModel | MealDB API | Búsqueda por nombre con resultados reales |
| RF-05 | Recipe Detail | RecipeDetailActivity | Repository / Firestore | Visualización de pasos e ingredientes |
| RF-06 | Saved | HomeViewModel / BookmarkDao | Room (Local) | Marcar favorito y verlo en modo avión |
| RF-07 | Add Recipe | AddRecipeViewModel | Firestore / Storage | Crear receta y verla aparecer en la nube |
| RF-08 | JSON Import | JsonRecipeImportActivity | Local Storage | Carga exitosa de receta desde archivo JSON |
| RF-09 | Profile | ProfileFragment | Firebase Auth | Cierre de sesión y retorno al inicio |
| RF-11 | HomePage | MainActivity / BottomNav | N/A | Navegación fluida entre las 4 secciones |
