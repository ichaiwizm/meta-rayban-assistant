# Meta Ray-Ban Assistant

Application Android simple avec système de mise à jour OTA (Over-The-Air) intégré.

## Fonctionnalités

- ✅ Écran d'accueil épuré
- ✅ **Système de mises à jour automatiques** - Déployez de nouvelles versions directement sur votre téléphone
- ✅ Interface moderne avec Jetpack Compose et Material 3
- ✅ Compatible Android 7.0+ (API 24)

## Comment ça fonctionne?

L'app vérifie automatiquement si une nouvelle version est disponible via un fichier `version.json` hébergé sur GitHub. Si une mise à jour existe, elle la télécharge et propose de l'installer.

**Aucun Play Store nécessaire!**

## Installation

### 1. Cloner le repository
```bash
git clone https://github.com/ichaiwizm/meta-rayban-assistant.git
cd meta-rayban-assistant
```

### 2. Ouvrir dans Android Studio
1. Lancer Android Studio
2. File → Open → Sélectionner le dossier `meta-rayban-assistant`
3. Attendre la synchronisation Gradle

### 3. Build & Run
```bash
# Build l'APK
./gradlew assembleRelease

# APK généré dans:
# app/build/outputs/apk/release/app-release.apk
```

### 4. Installer sur votre téléphone
```bash
# Via USB
adb install app/build/outputs/apk/release/app-release.apk

# Ou transférer l'APK sur votre téléphone et l'installer manuellement
```

## Déployer une Mise à Jour

### Étape 1: Incrémenter la version

Dans `app/build.gradle.kts`:
```kotlin
versionCode = 2  // Augmenter de 1
versionName = "1.1.0"  // Mettre à jour
```

### Étape 2: Build l'APK release
```bash
./gradlew assembleRelease
```

### Étape 3: Upload sur GitHub Releases
```bash
git tag v1.1.0
git push origin v1.1.0
```

Puis sur GitHub:
1. Releases → New release
2. Tag: v1.1.0
3. Upload: `app/build/outputs/apk/release/app-release.apk`
4. Publish

### Étape 4: Mettre à jour version.json

Éditer `version.json` à la racine:
```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "https://github.com/ichaiwizm/meta-rayban-assistant/releases/download/v1.1.0/app-release.apk",
  "changelog": "- Nouvelle fonctionnalité X\n- Correction bug Y",
  "mandatory": false
}
```

### Étape 5: Commit et push
```bash
git add version.json
git commit -m "Update to version 1.1.0"
git push origin master
```

**C'est tout!** Les utilisateurs verront la mise à jour la prochaine fois qu'ils ouvriront l'app.

## Stack Technique

- **Langage**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **HTTP**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines

## Documentation

- **[OTA_UPDATE_GUIDE.md](OTA_UPDATE_GUIDE.md)** - Guide complet des mises à jour OTA
- **version.json** - Fichier de configuration des versions

## Permissions

- `INTERNET` - Pour vérifier et télécharger les mises à jour
- `REQUEST_INSTALL_PACKAGES` - Pour installer les APK

## Sécurité

⚠️ **Important**: Pour que les mises à jour fonctionnent, tous les APK doivent être signés avec la même clé.

Voir [OTA_UPDATE_GUIDE.md](OTA_UPDATE_GUIDE.md) pour configurer la signature.

## Développement

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Nettoyer
./gradlew clean
```

## Licence

À définir

## Auteur

Ichai Wizman (@ichaiwizm)
