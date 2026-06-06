# D01. Diagrama de casos de uso

**Objetivo:** Representar de forma gráfica y estandarizada las interacciones entre los diferentes tipos de usuarios y las funcionalidades principales que ofrece el sistema Recipify, siguiendo el estándar clásico de diagramas UML de casos de uso (Actores Humanos -> Límite del Sistema -> Sistemas Externos).

![Diagrama de casos de uso](D01-casos-uso.jpg)

## Diagrama de Casos de Uso (PlantUML)

```plantuml
@startuml
' --- Configuración Estética (Estilo Profesional UML) ---
skinparam actorStyle stickman
skinparam packageStyle rectangle
skinparam shadowing false
skinparam usecase {
    BackgroundColor White
    BorderColor Black
    ArrowColor Black
}
left to right direction

' --- Actores Humanos (Lado Izquierdo) ---
actor "Usuario no\nautenticado" as Unauth
actor "Usuario\nautenticado" as Auth

' --- Límite del Sistema ---
rectangle "Recipify (App Móvil)" {
  (Registrarse) as UC1
  (Iniciar sesión) as UC2
  (Ver recetas destacadas) as UC3
  (Buscar recetas) as UC4
  (Ver listado de recetas) as UC5
  (Ver detalle de receta) as UC6
  (Crear receta propia) as UC7
  (Guardar receta en favoritos) as UC8
  (Ver favoritos) as UC9
  (Importar recetas desde JSON) as UC10
  (Configurar perfil) as UC11
  (Cerrar sesión) as UC12
}

' --- Actores de Sistema / Externos (Lado Derecho) ---
actor "Firebase Auth / Firestore" as Firebase <<Service>>
actor "TheMealDB API" as API <<Service>>
actor "Room Database" as Room <<Service>>
actor "Firebase Crashlytics" as Crashlytics <<Service>>

' --- Relaciones: Actores Humanos ---
Unauth -- UC1
Unauth -- UC2
Unauth -- UC3

Auth -- UC3
Auth -- UC4
Auth -- UC5
Auth -- UC6
Auth -- UC7
Auth -- UC8
Auth -- UC9
Auth -- UC10
Auth -- UC11
Auth -- UC12

' --- Relaciones: Sistemas Externos ---
UC1 -- Firebase
UC2 -- Firebase
UC7 -- Firebase
UC12 -- Firebase

UC3 -- API
UC4 -- API

UC8 -- Room
UC9 -- Room

UC4 -- Crashlytics
UC7 -- Crashlytics

@enduml
```

## Descripción de Actores y Casos de Uso

### Actores
*   **Usuario no autenticado (Izquierda):** Representa al usuario que interactúa con la aplicación de forma anónima o previa al acceso. Sus acciones incluyen el descubrimiento inicial y los flujos de autenticación.
*   **Usuario autenticado (Izquierda):** Representa al usuario con sesión activa. Posee control total sobre su recetario personal, creación de contenido y configuración de cuenta.
*   **Sistemas Externos (Derecha):**
    *   **Firebase:** Gestiona la identidad, la base de datos en la nube y el almacenamiento de medios.
    *   **TheMealDB:** API externa que provee la base de datos global de recetas.
    *   **Room:** Sistema de persistencia local que permite el uso de favoritos en modo offline.
    *   **Crashlytics:** Servicio encargado del reporte y monitoreo de estabilidad técnica.

### Casos de Uso Principales
*   **Acceso y Seguridad:** Procesos de registro e inicio de sesión integrados con servicios de nube.
*   **Gestión de Contenido:** Flujos para buscar en el catálogo externo y crear recetas personalizadas.
*   **Persistencia y Resiliencia:** Capacidad de guardar información localmente para consulta sin conexión a red.

> **Nota:** Este diagrama está embebido directamente en el archivo Markdown usando sintaxis PlantUML. Para visualizarlo en Android Studio, asegúrese de tener instalado el plugin de PlantUML y contar con **Graphviz** instalado en su sistema.
