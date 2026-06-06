# Uso de IA en el Proyecto

## 1. Propósito del documento

Este documento registra de forma transparente y responsable el uso de herramientas de inteligencia artificial (IA) como apoyo durante el proceso de desarrollo y documentación del proyecto **Recipify**. Se aclara que la IA ha sido empleada exclusivamente como una herramienta auxiliar para tareas de ideación, explicación técnica, revisión de documentación y depuración puntual. El equipo de desarrollo mantiene la responsabilidad total sobre el diseño, la implementación y la validación de todos los componentes del sistema.

## 2. Herramientas utilizadas

| Herramienta | Uso principal | Alcance del uso | Observaciones |
| :--- | :--- | :--- | :--- |
| **Gemini en Android Studio** | Apoyo técnico y depuración | Revisión de la estructura del proyecto, explicación de errores de compilación y sugerencias para la generación inicial de textos técnicos. | Integrado directamente en el entorno de desarrollo. |
| **Claude (Anthropic)** | Revisión de lógica y redacción | Apoyo en la revisión de fragmentos de código, sugerencias de mejores prácticas y pulido de la redacción técnica. | Herramienta de consulta para mejorar la calidad del código y textos. |
| **ChatGPT / Microsoft Copilot** | Redacción y revisión | Apoyo en la redacción de prompts, explicación de conceptos de arquitectura y revisión gramatical de la documentación. | Usado como consultor externo para mejorar la claridad de los textos. |
| **Android Studio** | Entorno de desarrollo | Plataforma principal para la codificación, gestión de dependencias y pruebas. | No es una IA generativa, sino la herramienta base de construcción. |

## 3. Prompts relevantes o resumen de prompts

A continuación se resumen las interacciones y solicitudes realizadas a las herramientas de IA durante el proyecto:

1.  **Explicación de errores de Git**: Solicitud de aclaraciones sobre comandos básicos (`add`, `commit`, `push`, `pull`) y resolución de conflictos de fusión.
2.  **Organización del proyecto**: Orientación sobre la ubicación correcta de la carpeta `docs/` y otros archivos de documentación en la raíz del proyecto Android.
3.  **Estructura de documentación**: Generación de esquemas iniciales para el documento **SRS** en `docs/SRS.md` basándose en requerimientos del proyecto.
4.  **Justificación del stack**: Apoyo en la redacción técnica de los motivos por los cuales se eligieron tecnologías como Room y Firebase en `docs/arquitectura.md`.
5.  **Mejora de tablas Markdown**: Revisión y mejora del formato de tablas relacionadas con requerimientos funcionales, no funcionales y matriz de trazabilidad.
6.  **Documentación de uso de IA**: Guía inicial para la redacción del presente archivo `docs/uso-ia.md`.

*Nota: Todos los resultados generados por IA fueron revisados y adaptados por el equipo para asegurar su correspondencia con el código real.*

## 4. Partes generadas, modificadas o descartadas con apoyo de IA

| Parte del proyecto | Tipo de apoyo de IA | Acción del equipo | Resultado final |
| :--- | :--- | :--- | :--- |
| **Documentación SRS** | Propuesta de estructura y redacción | Revisión completa, ajuste de requerimientos funcionales según el código real y adaptación al dominio de la app. | Documento completo y verificado por el equipo. |
| **Documentación de arquitectura** | Organización de la justificación técnica | Validación de las tecnologías realmente usadas (Room, Firebase) y corrección de justificaciones según la implementación. | Justificación técnica coherente y fiel al proyecto. |
| **Documentación de uso de IA** | Redacción inicial de secciones | Verificación de que el contenido reflejara el uso real de las herramientas por parte de los integrantes. | Documento de ética y transparencia. |
| **Comandos Git** | Explicación de comandos y errores | Ejecución manual de los comandos en la terminal y validación de resultados en el repositorio. | Gestión de versiones exitosa. |
| **Código fuente** | Explicación y depuración puntual | La IA se usó para explicar fragmentos de código o sugerir correcciones menores; el equipo implementó y probó cada cambio. | Código fuente comprendido y funcional. |
| **Contenido descartado** | Sugerencias genéricas | Se descartaron funcionalidades inventadas, librerías no utilizadas y explicaciones técnicas que el equipo no podía defender. | Información precisa y limitada a lo real. |

## 5. Validaciones realizadas por el equipo

El equipo de Recipify realizó las siguientes validaciones sobre el apoyo brindado por la IA:

*   Revisión manual y exhaustiva de cada párrafo generado antes de ser incluido en los documentos definitivos.
*   Comparación constante de la documentación con la estructura física de carpetas y archivos en Android Studio.
*   Verificación de la presencia de dependencias reales en los archivos `build.gradle.kts` para contrastar con la arquitectura documentada.
*   Pruebas de ejecución en emuladores y dispositivos físicos para asegurar que los requerimientos funcionales descritos existen realmente.
*   Inspección de seguridad para garantizar que no se incluyeran tokens de Firebase, claves de API o rutas personales en los prompts o la documentación final.
*   Confirmación de que cada integrante del equipo comprende y puede explicar técnicamente las partes del proyecto en las que recibió apoyo de IA.

## 6. Riesgos detectados en el uso de IA

| Riesgo | Descripción | Mitigación aplicada |
| :--- | :--- | :--- |
| **Código no comprendido** | La IA puede sugerir patrones de diseño o código que el equipo no domina. | Se descartaron sugerencias complejas y solo se conservó lo que el equipo entendió y pudo probar. |
| **Funcionalidades inventadas** | La IA tiende a asumir características genéricas de apps de recetas que no están implementadas. | Revisión manual contra el código fuente para eliminar cualquier mención a funciones inexistentes. |
| **Dependencias innecesarias** | Sugerencia de librerías que sobrecargan el proyecto sin una necesidad real. | Solo se incluyeron en la documentación las tecnologías justificadas en los archivos Gradle. |
| **Errores en documentación** | Generación de texto genérico o académico que no se adapta al contexto específico de Recipify. | Edición manual de cada sección para personalizar el tono y el contenido al dominio del proyecto. |
| **Filtración de datos** | Riesgo de compartir información privada en los prompts de entrada a la IA. | No se proporcionaron datos sensibles como credenciales de bases de datos o rutas de sistema local. |

## 7. Responsabilidad del equipo

El equipo de desarrollo de Recipify asume la responsabilidad total sobre el producto entregado, declarando que:

*   La inteligencia artificial fue utilizada exclusivamente como una herramienta de apoyo auxiliar para aumentar la productividad y mejorar la documentación técnica.
*   Las decisiones críticas de diseño, la elección de la arquitectura (MVVM) y la implementación del código fuente son resultado del criterio y trabajo de los desarrolladores.
*   El equipo ha revisado, adaptado y validado cada uno de los aportes externos antes de su integración oficial.
*   El dominio sobre el proyecto y la capacidad de defensa técnica del mismo residen en el equipo, no dependiendo en ningún caso de la disponibilidad o sugerencias de una IA.
