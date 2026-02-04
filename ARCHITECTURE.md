# Architecture - Meta Ray-Ban Assistant

## Vue d'ensemble

Cette application suit le pattern **MVVM (Model-View-ViewModel)** recommandé par Google pour les applications Android modernes. L'architecture est conçue pour être:
- **Testable**: Séparation claire des responsabilités
- **Maintenable**: Code organisé par fonctionnalité
- **Scalable**: Facile à étendre avec de nouvelles fonctionnalités

## Diagramme de l'architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          UI Layer (View)                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Jetpack Compose                                          │  │
│  │  - HomeScreen                                             │  │
│  │  - SettingsScreen (à venir)                              │  │
│  │  - Components (boutons, cards, etc.)                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │ observe StateFlow
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  MainViewModel                                            │  │
│  │  - Gestion des états UI (UiState)                        │  │
│  │  - Orchestration des repositories                        │  │
│  │  - Logique métier simple                                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │ appelle
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Repository Layer (Data)                       │
│  ┌──────────────────────────┐   ┌───────────────────────────┐  │
│  │  GlassesRepository       │   │  ClaudeRepository         │  │
│  │  - Connexion lunettes    │   │  - Appels API Claude      │  │
│  │  - Capture photos        │   │  - Gestion cache          │  │
│  └──────────┬───────────────┘   └──────────┬────────────────┘  │
└─────────────┼──────────────────────────────┼────────────────────┘
              │                              │
              ▼                              ▼
┌────────────────────────┐      ┌──────────────────────────────┐
│  Meta Wearables SDK    │      │  Retrofit + OkHttp           │
│  - mwdat-core          │      │  - ClaudeApiService          │
│  - mwdat-camera        │      │  - JSON serialization        │
└────────────────────────┘      └──────────────────────────────┘
```

## Couches de l'application

### 1. UI Layer (View)
**Responsabilité**: Afficher les données et gérer les interactions utilisateur

**Technologies**:
- Jetpack Compose pour l'UI déclarative
- Material 3 pour le design system
- State hoisting pour la gestion d'état

**Composants**:
- `MainActivity.kt`: Point d'entrée, configure le thème
- `ui/screens/`: Écrans de l'application
- `ui/components/`: Composables réutilisables
- `ui/theme/`: Configuration thème (couleurs, typographie)

**Principe**: Les screens sont des fonctions Composable **sans état** (stateless) qui reçoivent leurs données via paramètres et émettent des événements via callbacks.

### 2. ViewModel Layer
**Responsabilité**: Gérer l'état de l'UI et la logique de présentation

**Technologies**:
- ViewModel (lifecycle-aware)
- StateFlow pour les états observables
- Coroutines pour l'asynchrone

**Composants**:
- `viewmodel/MainViewModel.kt`: État principal de l'app
- États possibles: `Disconnected`, `Connecting`, `Connected`, `CapturingPhoto`

**Principe**: Le ViewModel ne contient **aucune référence** à l'UI. Il expose des StateFlow que l'UI observe.

### 3. Data Layer (Repository)
**Responsabilité**: Fournir et gérer les données de l'application

**Pattern Repository**: Abstraction entre les sources de données et le ViewModel

**Composants (à implémenter)**:
- `data/repository/GlassesRepository.kt`: Gestion connexion Meta Ray-Ban
- `data/repository/ClaudeRepository.kt`: Appels API Claude
- `data/source/`: Implémentations des sources de données
- `data/model/`: Modèles de données (DTOs)

**Principe**: Un repository peut combiner plusieurs sources de données (réseau, local, cache).

## États de l'Application (UiState)

```kotlin
sealed class UiState {
    object Disconnected : UiState()          // Aucune lunette connectée
    object Connecting : UiState()            // Connexion en cours
    data class Connected(                    // Connecté avec succès
        val deviceName: String
    ) : UiState()
    object CapturingPhoto : UiState()        // Capture en cours
    object PhotoCaptured : UiState()         // Photo prête
}
```

## Flux de données

### Exemple: Connexion aux lunettes

1. **User Action**: L'utilisateur appuie sur "Connecter"
   ```kotlin
   Button(onClick = { viewModel.connectToGlasses() })
   ```

2. **ViewModel**: Change l'état et appelle le repository
   ```kotlin
   fun connectToGlasses() {
       viewModelScope.launch {
           _uiState.value = UiState.Connecting
           val result = glassesRepository.connect()
           _uiState.value = when(result) {
               is Success -> UiState.Connected(result.deviceName)
               is Error -> UiState.Disconnected
           }
       }
   }
   ```

3. **Repository**: Utilise le Meta SDK
   ```kotlin
   suspend fun connect(): Result<Device> {
       return withContext(Dispatchers.IO) {
           metaWearablesSource.scanAndConnect()
       }
   }
   ```

4. **UI Update**: L'écran réagit au changement d'état
   ```kotlin
   val uiState by viewModel.uiState.collectAsState()
   when(uiState) {
       is Connecting -> CircularProgressIndicator()
       is Connected -> Text("Connecté à ${uiState.deviceName}")
   }
   ```

## Gestion des dépendances

### Actuellement: Constructor Injection Manuelle
Les dépendances sont créées directement dans les ViewModels.

### Future: Dependency Injection avec Hilt
Pour une meilleure testabilité:
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val glassesRepository: GlassesRepository,
    private val claudeRepository: ClaudeRepository
) : ViewModel()
```

## Threading Model

### Dispatchers Kotlin Coroutines

- **Main**: UI updates, StateFlow emissions
- **IO**: Réseau (API Claude), Bluetooth
- **Default**: Traitement CPU-intensif (image processing)

```kotlin
viewModelScope.launch {
    // Main dispatcher par défaut
    _uiState.value = UiState.Loading

    val result = withContext(Dispatchers.IO) {
        // Appel réseau sur IO dispatcher
        repository.fetchData()
    }

    // Retour automatique sur Main
    _uiState.value = UiState.Success(result)
}
```

## Modèles de données

### Photo (capture des lunettes)
```kotlin
data class Photo(
    val id: String,
    val timestamp: Date,
    val imageData: ByteArray,
    val metadata: PhotoMetadata?
)
```

### AIRequest/Response (API Claude)
```kotlin
data class AIRequest(
    val model: String,
    val messages: List<Message>,
    val maxTokens: Int
)

data class AIResponse(
    val content: String,
    val usage: Usage
)
```

## Gestion des erreurs

### Stratégie en 3 niveaux

1. **Repository**: Catch exceptions, retourne `Result<T>`
   ```kotlin
   suspend fun fetchData(): Result<Data> = try {
       Result.Success(api.getData())
   } catch (e: Exception) {
       Result.Error(e)
   }
   ```

2. **ViewModel**: Transforme en états UI
   ```kotlin
   when (result) {
       is Success -> _uiState.value = UiState.Success(result.data)
       is Error -> _errorMessage.value = result.exception.message
   }
   ```

3. **UI**: Affiche messages utilisateur
   ```kotlin
   errorMessage?.let { error ->
       Snackbar { Text(error) }
   }
   ```

## Tests

### Tests Unitaires (à implémenter)
- **ViewModels**: Tester la logique métier et les changements d'état
- **Repositories**: Mocker les sources de données
- **Use Cases**: Tester la logique métier complexe

### Tests d'intégration
- Tester les flux complets (UI → ViewModel → Repository)

### Tests UI
- Compose UI Tests pour les screens

## Sécurité

### Données sensibles
- API Keys stockées dans `SharedPreferences` chiffrées (EncryptedSharedPreferences)
- Jamais de credentials hardcodés dans le code
- `local.properties` dans `.gitignore`

### Permissions
- Runtime permissions avec `accompanist-permissions`
- Rationale expliqué à l'utilisateur
- Gestion des refus de permission

## Performance

### Optimisations prévues
- **Image Caching**: Cache LRU pour les photos
- **API Rate Limiting**: Throttling des requêtes Claude
- **Lazy Loading**: Pagination des listes
- **Background Work**: WorkManager pour tâches longues

## Évolution future

### Modules à ajouter
- `feature-voice`: Assistant vocal
- `feature-settings`: Paramètres app
- `feature-gallery`: Galerie photos
- `core-network`: HTTP client partagé
- `core-database`: Room pour cache local

### Multi-module architecture
Pour scalabilité et build plus rapide:
```
app/
feature-glasses/
feature-voice/
feature-gallery/
core-ui/
core-network/
core-database/
```

## Références

- [Guide Architecture Android](https://developer.android.com/topic/architecture)
- [State and Jetpack Compose](https://developer.android.com/jetpack/compose/state)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Meta Wearables SDK Docs](https://wearables.developer.meta.com/docs/)
