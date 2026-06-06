# D05. Diagrama de sincronización local-remoto

**Objetivo:** Explicar el flujo de datos entre la persistencia local (Room) y la nube (Firebase/API) en Recipify.

## Diagrama de Sincronización

```mermaid
sequenceDiagram
    participant UI as Interfaz de Usuario
    participant Repos as RecipeRepository
    participant Local as Room (Local DB)
    participant Remote as Firestore / API (Nube)

    Note over UI, Remote: Flujo de Lectura (Online)
    UI->>Repos: Solicitar recetas
    Repos->>Remote: Fetch data
    Remote-->>Repos: Success (List<Recipe>)
    Repos-->>UI: Mostrar recetas

    Note over UI, Remote: Flujo de Guardado Favorito (Offline Support)
    UI->>Repos: Marcar como favorito
    Repos->>Local: Insert RecipeEntity & BookmarkEntity
    Local-->>Repos: Confirmación
    Repos-->>UI: Estado actualizado (visto offline)

    Note over UI, Remote: Flujo de Creación (Online)
    UI->>Repos: Guardar receta propia
    Repos->>Remote: Push document to Firestore
    Remote-->>Repos: Document ID
    Repos-->>UI: Éxito (Sync completed)

    Note over UI, Remote: Manejo de Error de Red
    UI->>Repos: Acción requerida de red
    Repos->>Remote: Intento de conexión
    Remote--xRepos: Error (No connection)
    Repos->>UI: Mostrar mensaje error / Cargar caché local
```

## Explicación
- **Lectura:** Cuando hay conexión, la app prioriza los datos frescos de la API externa y Firestore.
- **Favoritos:** El guardado de favoritos se realiza principalmente de forma local en **Room**, lo que permite que el usuario consulte sus recetas guardadas incluso sin internet.
- **Escritura:** La creación de recetas propias se envía directamente a **Firestore**. Si no hay conexión, el sistema informa al usuario (Sincronización manual o reintento automático marcado como *Recomendado*).
- **Resiliencia:** Ante errores de red, la aplicación está diseñada para no cerrarse y ofrecer una alternativa basada en los datos persistidos localmente.
