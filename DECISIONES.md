# Entregable - Uso del patrón MVVM y StateFlow dentro del proyecto

> Para esta sección, se contextualiza que el único flujo en nuestro programa que hasta el momento tiene unos requerimientos similares a los que pide esta actividad, por lo tanto, se va a aplicar este ejemplo a los archivos de nuestro proyecto [RegisterViewModel.kt](https://github.com/AntJZs/Recipify/blob/main/app/src/main/java/com/progweb/recipify/viewmodel/AddRecipeViewModel.kt) y [AddRecipe.kt](https://github.com/AntJZs/Recipify/blob/main/app/src/main/java/com/progweb/recipify/addRecipe/AddRecipe.kt)    

## Capturas de pantalla
[img1.png](ruta)
    
## ¿Dónde vive el estado?
En `_uiState = MutableStateFlow(UiState())` dentro del ViewModel. El Fragment/Activity no tiene ninguna variable de estado propia.

## ¿Qué pasa si se rota la pantalla?
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { ... }
    }
}
```
repeatOnLifecycle cancela el collector cuando la Activity va a background y lo reanuda cuando vuelve. El ViewModel sobrevive la rotación con el estado intacto. 

##  ¿Qué pasa si presiona Guardar dos veces?
```kotlin
fun saveRecipe(...) {
    if (_uiState.value.guardando) return  // segunda llamada ignorada
    _uiState.value = UiState(guardando = true)
    ...
}
```


kotlin
binding.btnGuardar.isEnabled = !estado.guardando  // botón deshabilitado visualmente.