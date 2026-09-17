# FitTracker — Control de Calorías, Rutina y Asistente IA

App Android nativa para seguimiento nutricional, gestión de rutinas de fuerza, sincronización de wearable (Huawei Watch Fit 3) mediante visión artificial de Gemini, y asistente entrenador personal potenciado con Gemini Function Calling.

---

## 🚀 Características Principales

1. **Panel Diario ("Hoy")**:
   - **Balance Calórico Neto**: Calorías consumidas (`FoodLog`) − Calorías activas quemadas (`DailyActivitySummary`) vs Objetivo del usuario (`UserTargets`).
   - **Pasos del día**: Registrados desde el wearable.
   - **Tiempo de ejercicio**: Fuerza (gimnasio) + Cardio (Huawei Health).
   - **Desglose de Macronutrientes**: Barras de progreso visual para Proteínas, Carbohidratos y Grasas.

2. **Sincronización Huawei Watch Fit 3 (Gemini Visión)**:
   - Como Huawei Health no sincroniza nativamente con Health Connect en dispositivos sin HMS, la app permite subir una captura de pantalla del resumen diario.
   - **Gemini Multimodal Vision** analiza los widgets y extrae: `date`, `steps`, `activeCalories`, `exerciseMinutes`, `exerciseType`.
   - **Formulario editable de verificación**: El usuario puede revisar y corregir cualquier dato antes de confirmar y guardarlo en Room (`DailyActivitySummary`).

3. **Gestor de Rutinas y Gimnasio**:
   - CRUD de rutinas completas (ej. Push/Pull/Legs, Torso/Pierna).
   - Planificación por días de la semana (Lunes a Domingo) con series, repeticiones y peso objetivo.
   - Base de datos local de ejercicios precargada en Room desde `assets/exercises.json` (free-exercise-db), filtrable por grupo muscular y equipamiento.
   - **Registro de sesión real**: Comparativa de objetivo vs real (`actualSets`, `actualReps`, `actualWeight`, notas) guardada en `WorkoutLog`.

4. **Tracking de Alimentos y Calorías**:
   - Conectado en tiempo real a la API de **Open Food Facts** (`world.openfoodfacts.org`).
   - Caché local en Room (`food_cache`) para búsquedas offline y acceso instantáneo.
   - Selector de porción en gramos con recálculo dinámico de macronutrientes.
   - Configuración personalizada de objetivos diarios de calorías y macros.

5. **Entrenador y Asistente IA (Gemini Function Calling)**:
   - Chat interactivo especializado en recomposición corporal y sobrecarga progresiva.
   - Implementa **Function Calling** con 4 herramientas:
     - `get_routine(routineId)`: Consulta la rutina actual.
     - `get_workout_history(exerciseId, weeks)`: Analiza el progreso real de fuerza en semanas anteriores.
     - `get_activity_history(weeks)`: Inspecciona el cardio real (pasos, natación, running) y la fatiga acumulada.
     - `propose_routine_change(routineId, changes)`: Devuelve una propuesta estructurada con justificación.
   - 🛡️ **Seguridad Room**: La IA **NUNCA** escribe directamente en la base de datos. Genera una tarjeta interactiva en el chat con botones `[Aceptar y Aplicar]` y `[Descartar]`. Solo si el usuario acepta, se aplica el cambio en Room.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin 2.0
- **Interfaz (UI)**: Jetpack Compose (Material 3 Dark Theme)
- **Arquitectura**: MVVM con `StateFlow` y Repositorios (Single Source of Truth)
- **Persistencia local**: Room Database (SQLite) con migración y precarga inicial desde JSON
- **Red**: Retrofit 2 + OkHttp 4 + Gson
- **Inteligencia Artificial**: Gemini 1.5 Flash API (Visión Multimodal + Function Calling)
- **Carga de imágenes**: Coil Compose

---

## ⚙️ Configuración y Ejecución

### 1. Clave de Gemini API
Añade tu clave de Gemini en el archivo `local.properties` en la raíz del proyecto (este archivo está ignorado por Git):

```properties
GEMINI_API_KEY=tu_clave_de_gemini_aqui
```

> Obtén tu clave en [Google AI Studio](https://aistudio.google.com/).

### 2. Abrir en Android Studio
1. Abre **Android Studio** (Koala, Ladybug o superior).
2. Selecciona **Open** y elige la carpeta del proyecto `app_tracker`.
3. Deja que Gradle sincronice las dependencias.
4. Conecta un dispositivo físico o emulador Android (API 26+) y presiona **Run** (`Shift + F10`).
