# D03. Diagrama de arquitectura móvil

**Objetivo:** Explicar la organización técnica de la aplicación Recipify, detallando la interacción entre las capas de UI, Lógica de Negocio y Datos siguiendo el patrón MVVM.

## Diagrama de Arquitectura

```mermaid
flowchart TD
    subgraph UI_Layer [Capa de UI - View]
        A[Activities: Login, Register, Home, Detail]
        B[Fragments: Home, Search, Saved, Profile]
        C[Layouts XML / ViewBinding]
    end

    subgraph Logic_Layer [Capa de Negocio - ViewModel]
        D[HomeViewModel]
        E[SearchViewModel]
        F[LoginViewModel]
        G[AddRecipeViewModel]
    end

    subgraph Data_Layer [Capa de Datos - Repository]
        H[RecipeRepository]
        
        subgraph Local_Source [Local Data Source]
            I[(Room Database)]
            J[RecipeDao / BookmarkDao]
        end
        
        subgraph Remote_Source [Remote Data Source]
            K[Firebase Firestore]
            L[Firebase Storage]
            M[MealDB API - Retrofit]
        end
    end

    subgraph External_Services [Servicios Externos]
        N[Firebase Auth]
        O[Firebase Crashlytics]
    end

    %% Relaciones
    A & B --> C
    C <--> D & E & F & G
    D & E & G --> H
    H --> J --> I
    H --> K & L & M
    F & G --> N
    A & Logic_Layer --> O
```

## Explicación
- **Capa de UI:** Compuesta por Activities y Fragments que utilizan ViewBinding para interactuar con los layouts XML.
- **Capa de ViewModel:** Encargada de gestionar el estado de la UI y comunicarse con el Repositorio. Utiliza Corrutinas y Flow para el manejo asíncrono.
- **Capa de Datos (Repository):** El `RecipeRepository` actúa como mediador entre la fuente de datos local (Room) y las fuentes remotas (Firebase y API externa).
- **Servicios Externos:** Firebase Auth gestiona la seguridad y Crashlytics se encarga de la observabilidad y reporte de errores.
