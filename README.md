# Meta Ray-Ban Assistant

Application Android pour se connecter aux lunettes Meta Ray-Ban et utiliser un assistant vocal/AI.

## Fonctionnalités
- Capture photo/vidéo depuis les lunettes
- Assistant vocal avec analyse d'images via Claude AI
- Connexion Bluetooth aux Meta Ray-Ban
- Interface moderne avec Jetpack Compose

## Stack Technique
- **Langage**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **SDK Hardware**: Meta Wearables Device Access Toolkit v0.3.0
- **API IA**: Claude API (Anthropic)
- **HTTP Client**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines + Flow

## Prérequis

### Matériel
- Appareil Android avec **Android 10 (API 29)** ou supérieur
- Lunettes **Meta Ray-Ban** avec firmware version 20.0+
- Bluetooth LE compatible

### Logiciels
- **Android Studio** Flamingo (2022.2.1) ou plus récent
- **JDK 8** ou supérieur
- **Git**

### Comptes & Tokens
- **GitHub Personal Access Token** avec scope `read:packages` ([Créer un token](https://github.com/settings/tokens))
- **Claude API Key** d'Anthropic ([Console Anthropic](https://console.anthropic.com/))

## Installation

### 1. Cloner le repository
```bash
git clone https://github.com/ichaiwizm/meta-rayban-assistant.git
cd meta-rayban-assistant
```

### 2. Configurer les credentials

Créer le fichier `local.properties` à la racine du projet:
```properties
# Android SDK path
sdk.dir=/path/to/your/Android/Sdk

# GitHub credentials for Meta Wearables SDK
github.username=YOUR_GITHUB_USERNAME
github.token=ghp_YOUR_GITHUB_TOKEN
```

**Important**: Ne jamais committer `local.properties` (déjà dans `.gitignore`)

### 3. Ouvrir dans Android Studio
1. Lancer Android Studio
2. File → Open → Sélectionner le dossier `meta-rayban-assistant`
3. Attendre la synchronisation Gradle (première fois peut prendre quelques minutes)

### 4. Résoudre les dépendances
Si Gradle échoue à télécharger le Meta Wearables SDK, vérifier:
- Le token GitHub a le scope `read:packages`
- Le token est correctement copié dans `local.properties` (sans espaces)
- Connexion internet active

### 5. Build & Run
```bash
# Via Android Studio
Run → Run 'app' (Shift+F10)

# Ou via ligne de commande
./gradlew assembleDebug
./gradlew installDebug
```

## Structure du Projet

```
app/src/main/java/com/ichaiwizm/metaraybanassistant/
├── MainActivity.kt                    # Point d'entrée de l'application
├── ui/
│   ├── theme/                         # Thème Material 3
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── screens/
│   │   └── HomeScreen.kt              # Écran principal
│   └── components/                    # Composables réutilisables (à venir)
├── viewmodel/
│   └── MainViewModel.kt               # Logique UI et états
├── data/
│   ├── repository/                    # Couche d'accès aux données (à venir)
│   ├── model/
│   │   ├── Photo.kt                   # Modèle de photo
│   │   └── AIResponse.kt              # Modèles API Claude
│   └── source/                        # Sources de données (à venir)
└── di/                                # Dependency Injection (à venir)
```

## Configuration

### Permissions Android
L'application demande les permissions suivantes (déclarées dans `AndroidManifest.xml`):
- `BLUETOOTH` / `BLUETOOTH_ADMIN` - Connexion classique
- `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` - BLE (Android 12+)
- `ACCESS_FINE_LOCATION` - Requis pour BLE sur Android 10+
- `INTERNET` - API Claude
- `RECORD_AUDIO` - Assistant vocal
- `CAMERA` - Métadonnées photo

### Niveaux d'API
- **Minimum SDK**: 29 (Android 10) - Requis par Meta Wearables SDK
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## Développement

### Compiler le projet
```bash
./gradlew build
```

### Lancer les tests
```bash
./gradlew test                  # Tests unitaires
./gradlew connectedAndroidTest  # Tests instrumentés
```

### Nettoyer le build
```bash
./gradlew clean
```

## Prochaines Étapes

- [ ] Implémenter la connexion Bluetooth aux lunettes
- [ ] Intégrer le Meta Wearables SDK pour la capture photo
- [ ] Créer le client API Claude
- [ ] Implémenter l'assistant vocal
- [ ] Gérer les permissions runtime
- [ ] Ajouter des tests unitaires et d'intégration

## Ressources

### Documentation Meta
- [Meta Wearables FAQ](https://developers.meta.com/wearables/faq/)
- [SDK GitHub](https://github.com/facebook/meta-wearables-dat-android)
- [Getting Started Guide](https://wearables.developer.meta.com/docs/getting-started-toolkit/)

### Documentation Android
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Bluetooth Low Energy](https://developer.android.com/guide/topics/connectivity/bluetooth-le)
- [Permissions](https://developer.android.com/guide/topics/permissions/overview)

### API Claude
- [Documentation API](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)
- [Console Anthropic](https://console.anthropic.com/)

## Licence

À définir

## Auteur

Ichai Wizman (@ichaiwizm)
