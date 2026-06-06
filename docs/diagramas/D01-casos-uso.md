# D01. Diagrama de casos de uso

**Objetivo:** Mostrar los actores del sistema y las funcionalidades principales que ofrece la aplicación Recipify.

## Diagrama de Casos de Uso

```mermaid
graph TD
    UserNonAuth((Usuario No Autenticado))
    UserAuth((Usuario Autenticado))
    FirebaseService[Firebase Auth / Firestore]
    MealAPI[TheMealDB API]

    UserNonAuth --> UC1[Registrarse]
    UserNonAuth --> UC2[Iniciar Sesión]
    
    UserAuth --> UC3[Ver Recetas Destacadas]
    UserAuth --> UC4[Buscar Recetas]
    UserAuth --> UC5[Ver Detalles de Receta]
    UserAuth --> UC6[Guardar en Favoritos]
    UserAuth --> UC7[Crear Receta Propia]
    UserAuth --> UC8[Importar Recetas JSON]
    UserAuth --> UC9[Configurar Perfil]
    UserAuth --> UC10[Cerrar Sesión]

    UC1 -.-> FirebaseService
    UC2 -.-> FirebaseService
    UC3 -.-> MealAPI
    UC3 -.-> FirebaseService
    UC7 -.-> FirebaseService
    UC6 -.-> LocalDB[(Room Database)]
```

## Explicación
- **Actores:** El usuario se divide en dos estados (No Autenticado y Autenticado). Los servicios de Firebase y la API externa actúan como actores de soporte.
- **Funcionalidades:** El flujo principal permite al usuario autenticado interactuar con recetas (ver, buscar, guardar y crear) y gestionar su perfil. La persistencia se divide entre Firebase (nube) y Room (local para favoritos).
