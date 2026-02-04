# TODO - Meta Ray-Ban Assistant

Liste des tâches à implémenter pour compléter l'application.

## Phase 1: Connexion Bluetooth & Meta SDK

### 1.1 Gestion des Permissions
- [ ] Créer `PermissionsManager` pour gérer les permissions runtime
- [ ] Implémenter les rationales pour chaque permission
- [ ] Gérer les cas de refus de permission
- [ ] Tester sur Android 10, 12 et 14 (permissions Bluetooth changent)

**Fichiers à créer**:
- `utils/PermissionsManager.kt`
- `ui/components/PermissionRationaleDialog.kt`

### 1.2 Meta Wearables SDK Integration
- [ ] Créer `MetaWearablesSource.kt` pour wrapper le SDK
- [ ] Implémenter le scan BLE des lunettes
- [ ] Gérer la connexion/déconnexion
- [ ] Implémenter le pairing si nécessaire
- [ ] Gérer les états de connexion (connecting, connected, disconnected)
- [ ] Implémenter la gestion des erreurs SDK

**Fichiers à créer**:
- `data/source/MetaWearablesSource.kt`
- `data/repository/GlassesRepository.kt`

**Références**:
- [Meta SDK Docs](https://github.com/facebook/meta-wearables-dat-android)
- [Example App](https://github.com/facebook/meta-wearables-dat-android/tree/main/examples)

### 1.3 Intégrer au ViewModel
- [ ] Modifier `MainViewModel` pour utiliser `GlassesRepository`
- [ ] Ajouter les états de connexion détaillés
- [ ] Gérer les timeouts de connexion
- [ ] Ajouter retry logic

## Phase 2: Capture Photo

### 2.1 Camera API
- [ ] Implémenter `capturePhoto()` via Meta SDK
- [ ] Gérer le transfert de la photo (peut être lent via Bluetooth)
- [ ] Implémenter un indicateur de progression
- [ ] Sauvegarder temporairement les photos

**Fichiers à créer**:
- `data/source/CameraSource.kt`
- Extension dans `GlassesRepository.kt`

### 2.2 Storage
- [ ] Créer un cache local pour les photos (Room ou filesystem)
- [ ] Implémenter la compression des images si nécessaire
- [ ] Gérer la limite de stockage (supprimer vieilles photos)

**Fichiers à créer**:
- `data/local/PhotoDatabase.kt` (Room)
- `data/local/PhotoDao.kt`

### 2.3 UI
- [ ] Créer `PhotoPreviewScreen.kt` pour afficher la photo capturée
- [ ] Ajouter navigation entre HomeScreen et PhotoPreviewScreen
- [ ] Afficher miniatures des photos récentes

**Fichiers à créer**:
- `ui/screens/PhotoPreviewScreen.kt`
- `navigation/NavGraph.kt`

## Phase 3: Intégration Claude AI

### 3.1 API Client
- [ ] Créer `ClaudeApiService.kt` (Retrofit interface)
- [ ] Implémenter `ClaudeApiSource.kt` pour les appels
- [ ] Créer `ClaudeRepository.kt`
- [ ] Gérer l'authentification (API key)
- [ ] Implémenter retry logic et error handling
- [ ] Convertir images en base64 pour l'API

**Fichiers à créer**:
- `data/source/remote/ClaudeApiService.kt`
- `data/source/remote/ClaudeApiSource.kt`
- `data/repository/ClaudeRepository.kt`

**API Endpoint**:
```
POST https://api.anthropic.com/v1/messages
```

### 3.2 Gestion API Key
- [ ] Créer un écran Settings
- [ ] Stocker l'API key de façon sécurisée (EncryptedSharedPreferences)
- [ ] Valider la clé API au premier appel
- [ ] Afficher message d'erreur si clé invalide

**Fichiers à créer**:
- `ui/screens/SettingsScreen.kt`
- `data/local/SecurePreferences.kt`

### 3.3 Analyse d'images
- [ ] Créer un prompt système pour l'assistant
- [ ] Implémenter l'envoi photo + question à Claude
- [ ] Parser et afficher la réponse
- [ ] Gérer les limites de tokens
- [ ] Implémenter un historique de conversation

**Fichiers à créer**:
- `ui/screens/ChatScreen.kt`
- `ui/components/MessageBubble.kt`

## Phase 4: Assistant Vocal

### 4.1 Speech-to-Text
- [ ] Intégrer Android Speech Recognition
- [ ] Implémenter le bouton "push to talk"
- [ ] Afficher le texte reconnu en temps réel
- [ ] Gérer les erreurs de reconnaissance

**Fichiers à créer**:
- `data/source/SpeechRecognitionSource.kt`
- `ui/components/VoiceInputButton.kt`

### 4.2 Text-to-Speech
- [ ] Intégrer Android TTS
- [ ] Lire les réponses de Claude à voix haute
- [ ] Permettre de désactiver TTS
- [ ] Gérer les langues (FR/EN)

**Fichiers à créer**:
- `data/source/TextToSpeechSource.kt`
- `viewmodel/VoiceViewModel.kt`

### 4.3 Flux conversationnel
- [ ] Implémenter le mode "conversation continue"
- [ ] Garder le contexte entre plusieurs échanges
- [ ] Permettre d'interrompre l'assistant

## Phase 5: UI/UX Polish

### 5.1 Design
- [ ] Créer des icônes custom pour les boutons
- [ ] Ajouter des animations (Compose animations)
- [ ] Implémenter dark mode
- [ ] Adapter l'UI pour tablettes (responsive)

### 5.2 Navigation
- [ ] Implémenter Navigation Compose
- [ ] Créer un bottom nav bar ou drawer
- [ ] Ajouter des transitions entre écrans

**Fichiers à créer**:
- `navigation/NavGraph.kt`
- `navigation/Screen.kt`

### 5.3 État vide / Erreurs
- [ ] Créer des composants pour états vides
- [ ] Améliorer les messages d'erreur
- [ ] Ajouter des illustrations

**Fichiers à créer**:
- `ui/components/EmptyState.kt`
- `ui/components/ErrorState.kt`

## Phase 6: Tests

### 6.1 Tests unitaires
- [ ] Tests pour `MainViewModel`
- [ ] Tests pour repositories
- [ ] Tests pour les modèles de données
- [ ] Mocker les APIs (MockWebServer)

**Fichiers à créer**:
- `app/src/test/java/.../MainViewModelTest.kt`
- `app/src/test/java/.../GlassesRepositoryTest.kt`
- `app/src/test/java/.../ClaudeRepositoryTest.kt`

### 6.2 Tests d'intégration
- [ ] Tests de bout en bout
- [ ] Tests de l'UI Compose
- [ ] Tests des flows de navigation

**Fichiers à créer**:
- `app/src/androidTest/java/.../HomeScreenTest.kt`
- `app/src/androidTest/java/.../NavigationTest.kt`

### 6.3 Tests manuels
- [ ] Créer un plan de test manuel
- [ ] Tester sur plusieurs appareils Android
- [ ] Tester avec de vraies lunettes Meta Ray-Ban

## Phase 7: Performance & Optimization

### 7.1 Performance
- [ ] Implémenter Compose performance best practices
- [ ] Profiler l'app (Android Profiler)
- [ ] Optimiser les images (compression, cache)
- [ ] Réduire la consommation batterie

### 7.2 Caching
- [ ] Implémenter cache pour les réponses Claude
- [ ] Cache LRU pour les images
- [ ] Implémenter refresh strategy

### 7.3 Background work
- [ ] Utiliser WorkManager pour syncs en background
- [ ] Gérer les notifications si nécessaire

## Phase 8: Documentation & Release

### 8.1 Documentation
- [x] README.md
- [x] ARCHITECTURE.md
- [x] SETUP.md
- [ ] KDoc pour toutes les classes publiques
- [ ] Ajouter des diagrammes (PlantUML)
- [ ] Créer une documentation utilisateur

### 8.2 CI/CD
- [ ] Configurer GitHub Actions
- [ ] Build automatique sur chaque PR
- [ ] Tests automatiques
- [ ] Lint checks

**Fichiers à créer**:
- `.github/workflows/android.yml`

### 8.3 Release
- [ ] Créer un keystore pour signing
- [ ] Configurer ProGuard pour release
- [ ] Générer un APK/AAB release
- [ ] Publier sur Play Store (optionnel)

## Améliorations futures

### Features avancées
- [ ] Support multi-lunettes (basculer entre plusieurs paires)
- [ ] Historique des photos et conversations
- [ ] Partage sur réseaux sociaux
- [ ] Géolocalisation des photos
- [ ] Filtres photo en temps réel
- [ ] Mode réalité augmentée

### Intégrations
- [ ] Google Photos backup
- [ ] Intégration Wear OS (smartwatch)
- [ ] Widget Android
- [ ] Raccourcis Siri/Google Assistant

### Analytics
- [ ] Firebase Analytics
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] Usage tracking

## Priorités

**P0 (Critical)**: Phases 1, 2, 3.1, 3.2
**P1 (High)**: Phases 3.3, 4, 5.1
**P2 (Medium)**: Phases 5.2, 5.3, 6, 7
**P3 (Low)**: Phase 8, Améliorations futures

## Notes

- Toujours tester sur appareil réel pour Bluetooth
- Documenter au fur et à mesure
- Commit fréquemment avec messages clairs
- Créer des branches pour chaque feature majeure

---

**Dernière mise à jour**: 2026-02-04
