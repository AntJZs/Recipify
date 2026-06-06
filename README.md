# RECIPIFY MOBILE - DOCUMENTACIÓN TÉCNICA
## Cocina fácil, come increíble


## Descripción
> Actualmente vivimos en una sociedad sin tiempo para realizar actividades
> que son vitales para nosotros, una de ellas es cocinar. A veces llegamos cansados y cortos de tiempo
> para poder prepararnos algo que comer y adicionalmente con los ingredientes que
> tenemos en nuestra nevera no sabemos qué hacer.
>
> Las personas usan sitios web que dependen 100% de la conectividad a internet o
> recetarios físicos que a veces se guardan y olvidan.
>
> Recipify propone una forma más rápida y accesible de obtener esta información.
> En Recipify puedes ver recetas creadas por otras personas pero también compartir las propias.
> Hacemos que la información esté a la mano y disponible sin conexión.


## Integrantes
> ## Antonio De León Jiménez:
> Administrador de la base de datos y creación de prototipo
> ### Responsabilidades:
> Creación del prototipo móvil en Figma
> Crear la base de datos en Firebase
> Administrar la base de datos de Firestore
> Reportar bugs con Crashlytics
> Conectar la base de datos con la lógica del programa

>## Santiago Holguín Beltrán:
>Documentación y creación de diagramas
> ### Responsabilidades:
>Crear diagrama de casos de uso, navegación de pantallas, arquitectura móvil, modelo de datos
>Crear la documentación de SRS.md, arquitectura.md y uso-ia.md

>## Luis Fernando Morales Chavarría:
>Creación de pantallas y fragments
>### Responsabilidades:
>Crear una estructura base del proyecto
>Crear fragments de detalles de recetas
>Crear items de receta
>Configurar el Gradle
>Organizar flujos entre pantallas
>Gestionar uso de MVVM

>## Susana Solórzano Salazar
>Creación de pantallas y wireframe
>### Responsabilidades:
>Creación del wireframe de la app
>Creación del apartado de registro y login
>Creación del logo
>Creación del slogan
>Creación de fragment de detalle de la receta


## Stack Tecnológico

| Capa | Tecnología | Versión |
|:---|:---|:---|
| Plataforma | Android Nativo - Kotlin | API 30+ (Android 11) |
| UI | XML Views + ViewBinding | - |
| Arquitectura | MVVM + Repository Pattern | - |
| Persistencia local | Room | 2.7.1 |
| Persistencia remota | Firebase Firestore (offline persistence) | BOM 34.10.0 |
| Autenticación | Firebase Auth (Email + Google) | BOM 34.10.0 |
| Almacenamiento de imágenes | Firebase Storage | BOM 34.10.0 |
| API externa de recetas | TheMealDB API via Retrofit | Retrofit 2.9.0 |
| Carga de imágenes | Glide | 5.0.5 |
| Serialización | Gson | 2.10.1 |
| Asincronía | Kotlin Coroutines + Flow | 1.8.1 |
| Build system | Android Gradle Plugin (AGP) | 9.2.1 |
| Procesamiento de anotaciones | KSP | 2.1.20-1.0.32 |


## Funcionalidades Principales

### Para todos los usuarios
- Pantalla de bienvenida con recetas destacadas de muestra
- Registro con email/contraseña o cuenta Google
- Inicio de sesión

### Para usuarios autenticados
| Funcionalidad | Descripción |
|:---|:---|
| Explorar recetas | Feed principal con recetas de Firestore + TheMealDB API, con carga paginada por letra |
| Buscar recetas | Búsqueda en tiempo real con debounce de 500ms |
| Detalle de receta | Imagen, ingredientes, pasos con soporte Markdown, área geográfica |
| Favoritos / Bookmarks | Guardar recetas para acceso offline; sincroniza con Firestore automáticamente |
| Crear receta propia | Formulario con soporte de formato Markdown, selección de imagen, guardado de borrador automático |
| Editar / Eliminar receta | Edición y eliminación de recetas propias desde el perfil |
| Perfil de usuario | Avatar, contador de favoritos, publicaciones propias en grilla |
| Importar recetas JSON | Importación masiva desde archivo JSON |
| Modo offline | Escrituras encoladas localmente; sincronización automática al recuperar conectividad |


## Arquitectura

El proyecto sigue el patrón MVVM + Repository con tres capas bien definidas:

```
+---------------------------------------------------------+
|  CAPA UI  (Activities / Fragments + ViewBinding)        |
|  LoadingActivity - Destacados - HomePage                |
|  HomeFragment - SearchFragment - SavedFragment          |
|  ProfileFragment - RecipeDetailActivity - AddRecipe     |
+----------------------+----------------------------------+
                       | observa LiveData / StateFlow
+----------------------v----------------------------------+
|  CAPA VIEWMODEL  (Logica de presentacion)               |
|  HomeViewModel - SearchViewModel - AddRecipeViewModel   |
|  LoginViewModel - DestacadosViewModel - LoadingViewModel|
+----------------------+----------------------------------+
                       | solicita datos
+----------------------v----------------------------------+
|  CAPA REPOSITORY  (RecipeRepository - singleton)        |
|                                                         |
|  LOCAL (Room)              REMOTO                       |
|  RecipeDao                 Firebase Firestore (offline) |
|  BookmarkDao               Firebase Storage             |
|  AppDatabase               TheMealDB API (Retrofit)     |
+---------------------------------------------------------+
```

### Estrategia offline-first
- Firestore usa `PersistentCacheSettings` con caché ilimitado — todas las escrituras se encolan localmente y sincronizan cuando hay red.
- Room persiste el catálogo de recetas y bookmarks para acceso instantáneo sin internet.
- Las operaciones de UI (guardar receta, marcar favorito) usan el patrón fire-and-forget: la escritura se realiza localmente de inmediato y el servidor confirma en background.

Documentación detallada: [docs/arquitectura.md](docs/arquitectura.md)


## Diagramas

Todos los diagramas están en formato Mermaid en la carpeta [docs/diagramas/](docs/diagramas/):

| Diagrama | Archivo |
|:---|:---|
| Casos de uso | [D01-casos-uso.md](docs/diagramas/D01-casos-uso.md) |
| Navegación entre pantallas | [D02-navegacion.md](docs/diagramas/D02-navegacion.md) |
| Arquitectura móvil (MVVM) | [D03-arquitectura-movil.md](docs/diagramas/D03-arquitectura-movil.md) |
| Modelo de datos | [D04-modelo-datos.md](docs/diagramas/D04-modelo-datos.md) |
| Sincronización offline | [D05-sincronizacion.md](docs/diagramas/D05-sincronizacion.md) |
| Diagrama de secuencia | [D06-secuencia.md](docs/diagramas/D06-secuencia.md) |
| Estructura de carpetas | [D07-estructura-carpetas.md](docs/diagramas/D07-estructura-carpetas.md) |
| Despliegue y servicios | [D08-despliegue-servicios.md](docs/diagramas/D08-despliegue-servicios.md) |


## Mockups / Prototipo

Diseño visual disponible en Figma:

[Recetapp - Recipify en Figma](https://www.figma.com/design/KboEIBfMj81PlG4NqCjWbD/Recetapp---Recipify?node-id=0-1&t=DQa2qcPZepkuql81-1)


## Servicios Externos

| Servicio | Uso en la app | Requiere clave |
|:---|:---|:---|
| Firebase Auth | Autenticación con Email y Google Sign-In | Sí - google-services.json |
| Firebase Firestore | Base de datos principal de recetas y perfiles de usuario | Sí - google-services.json |
| Firebase Storage | Almacenamiento de imágenes de recetas y avatares | Sí - google-services.json |
| TheMealDB API | Catálogo público de recetas (themealdb.com/api/json/v1/1/) | No - API pública gratuita |


## Cómo Ejecutar Localmente

### Prerequisitos
- Android Studio Narwhal (2025.1.1) o superior
- JDK 11+
- Dispositivo o emulador con Android 11 (API 30) o superior
- Cuenta de Firebase con proyecto configurado

### Pasos

**1. Clonar el repositorio**
```bash
git clone https://github.com/<org>/Recipify.git
cd Recipify
```

**2. Configurar Firebase**

Descarga el archivo `google-services.json` desde la consola de Firebase del proyecto y colócalo en:
```
app/google-services.json
```

**3. Configurar reglas de Firestore**

En Firebase Console > Firestore > Reglas, asegúrate de tener reglas que permitan lectura/escritura autenticada:
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**4. Sincronizar Gradle y ejecutar**

En Android Studio:
- File > Sync Project with Gradle Files
- Seleccionar dispositivo/emulador > Run 'app'

O desde terminal:
```bash
./gradlew installDebug
```

### Notas
- El archivo `google-services.json` no está en el repositorio por seguridad — debe solicitarse al administrador del proyecto.
- La API de TheMealDB es pública; no requiere ninguna clave adicional.
- Para modo offline completo, abre la app al menos una vez con conexión para poblar el caché de Firestore.


## Documentación Adicional

- SRS (Especificación de Requisitos): [docs/SRS.md](docs/SRS.md)
- Arquitectura y decisiones técnicas: [docs/arquitectura.md](docs/arquitectura.md)
- Uso de IA en el desarrollo: [docs/uso-ia.md](docs/uso-ia.md)
