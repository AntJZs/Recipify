# D06. Diagramas de secuencia

**Objetivo:** Mostrar la interacción dinámica entre el usuario, la interfaz, los ViewModels y los servicios externos para flujos críticos de la aplicación.

## 1. Inicio de Sesión (Login)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant V as LoginActivity (View)
    participant VM as LoginViewModel
    participant A as Firebase Auth
    participant H as HomePage

    U->>V: Ingresa credenciales y pulsa "Ingresar"
    V->>VM: login(email, password)
    VM->>A: signInWithEmailAndPassword()
    alt Éxito
        A-->>VM: AuthResult
        VM-->>V: Update loginState (Success)
        V->>H: startActivity(Intent)
    else Error
        A-->>VM: FirebaseAuthException
        VM-->>V: Update loginState (Error message)
        V->>U: Muestra Toast/Error en campos
    end
```

## 2. Crear Receta Propia

```mermaid
sequenceDiagram
    participant U as Usuario
    participant V as AddRecipe (View)
    participant VM as AddRecipeViewModel
    participant R as RecipeRepository
    participant F as Firebase Firestore
    participant S as Firebase Storage

    U->>V: Completa formulario y selecciona imagen
    V->>VM: addRecipe(recipe, imageUri)
    VM->>R: createRecipe(recipe, imageUri)
    
    rect rgb(200, 220, 240)
        Note right of R: Subida de imagen
        R->>S: uploadImage(uri)
        S-->>R: downloadUrl
    end

    R->>F: collection("recipes").add(recipeData)
    F-->>R: DocumentReference
    R-->>VM: Result.Success
    VM-->>V: Notificar éxito
    V->>U: Muestra mensaje y cierra pantalla
```

## 3. Manejo de Error de Red y Crashlytics

```mermaid
sequenceDiagram
    participant V as UI (Fragment/Activity)
    participant VM as ViewModel
    participant R as RecipeRepository
    participant API as TheMealDB API
    participant C as Firebase Crashlytics

    V->>VM: Cargar recetas
    VM->>R: getLatestRecipes()
    R->>API: Retrofit call (GET)
    
    alt Error de Red (IOException)
        API--xR: Failure
        R->>C: recordException(throwable)
        R-->>VM: Result.Error("No connection")
        VM-->>V: Update state (Show offline msg)
        V->>V: Cargar datos de Room (Caché)
    end
```

## Explicación
- **Login:** Representa la validación de credenciales contra Firebase Auth y la transición a la pantalla principal.
- **Creación de Receta:** Detalla el flujo complejo que incluye la subida de una imagen a Storage y la persistencia del documento en Firestore.
- **Manejo de Errores:** Ilustra cómo la aplicación captura fallos de red, los reporta a Crashlytics para monitoreo y ofrece una alternativa al usuario basada en datos locales.
