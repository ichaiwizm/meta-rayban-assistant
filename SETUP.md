# Guide d'installation - Meta Ray-Ban Assistant

Ce guide vous accompagne pas à pas pour configurer le projet sur votre machine de développement.

## Prérequis

### 1. Vérifier l'installation de Java

Le projet nécessite **JDK 8 ou supérieur**.

```bash
java -version
```

Si Java n'est pas installé, télécharger depuis:
- [OpenJDK](https://adoptium.net/) (recommandé)
- [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)

### 2. Installer Android Studio

**Version minimale**: Android Studio Flamingo (2022.2.1)

1. Télécharger depuis [developer.android.com](https://developer.android.com/studio)
2. Suivre l'assistant d'installation
3. Au premier lancement, installer:
   - Android SDK
   - Android SDK Platform (API 29 minimum, API 34 recommandé)
   - Android SDK Build-Tools
   - Android Emulator (optionnel)

### 3. Configurer le SDK Android

**Via Android Studio**:
1. Tools → SDK Manager
2. SDK Platforms → Cocher:
   - Android 14.0 (API 34)
   - Android 10.0 (API 29)
3. SDK Tools → Cocher:
   - Android SDK Build-Tools
   - Android SDK Command-line Tools
   - Android Emulator
   - Android SDK Platform-Tools

**Note**: Le chemin du SDK sera nécessaire plus tard (généralement `/home/user/Android/Sdk` sur Linux).

## Configuration du projet

### 1. Cloner le repository

```bash
git clone https://github.com/ichaiwizm/meta-rayban-assistant.git
cd meta-rayban-assistant
```

### 2. Créer un GitHub Personal Access Token

Le Meta Wearables SDK est distribué via GitHub Packages et nécessite un token d'authentification.

1. Aller sur [github.com/settings/tokens](https://github.com/settings/tokens)
2. Cliquer sur **Generate new token** → **Generate new token (classic)**
3. Configurer:
   - **Note**: "Meta Wearables SDK Access"
   - **Expiration**: 90 days (ou selon préférence)
   - **Scopes**: Cocher uniquement `read:packages`
4. Générer le token et **le copier immédiatement** (il ne sera plus visible)

### 3. Créer le fichier `local.properties`

À la racine du projet, créer `local.properties` (ou copier depuis `local.properties.example`):

```bash
cp local.properties.example local.properties
```

Éditer `local.properties` et remplir:

```properties
# Path vers votre Android SDK (trouver le chemin dans Android Studio → SDK Manager)
sdk.dir=/home/ichai/Android/Sdk

# Vos credentials GitHub
github.username=VOTRE_USERNAME_GITHUB
github.token=ghp_VOTRE_TOKEN_ICI
```

**Remplacer**:
- `/home/ichai/Android/Sdk` par votre chemin SDK réel
- `VOTRE_USERNAME_GITHUB` par votre username GitHub
- `ghp_VOTRE_TOKEN_ICI` par le token créé à l'étape précédente

**Important**:
- Ne PAS committer ce fichier (déjà dans `.gitignore`)
- Ne PAS partager votre token

### 4. Vérifier la configuration

```bash
cat local.properties
```

Vérifier que:
- Le chemin `sdk.dir` existe et contient les dossiers `platforms/`, `build-tools/`, etc.
- Le token GitHub commence par `ghp_`
- Pas d'espaces autour des `=`

## Ouvrir le projet dans Android Studio

### 1. Lancer Android Studio

### 2. Ouvrir le projet

- **File → Open**
- Naviguer vers le dossier `meta-rayban-assistant`
- Cliquer sur **OK**

### 3. Gradle Sync

Android Studio va automatiquement:
1. Détecter le projet Gradle
2. Télécharger les dépendances (peut prendre 5-10 min la première fois)
3. Indexer le code

**Si Gradle sync échoue**, vérifier:
- Connexion internet active
- `local.properties` correctement configuré
- Token GitHub valide avec le scope `read:packages`

### 4. Résolution de problèmes courants

#### Erreur: "Unable to resolve dependency 'com.meta.wearable:mwdat-core:0.3.0'"

**Cause**: Problème d'authentification GitHub Packages

**Solutions**:
1. Vérifier que le token a le scope `read:packages`
2. Regénérer un nouveau token si nécessaire
3. S'assurer qu'il n'y a pas d'espaces dans `local.properties`
4. Tester avec:
   ```bash
   curl -H "Authorization: token ghp_VOTRE_TOKEN" \
        https://maven.pkg.github.com/facebook/meta-wearables-dat-android/
   ```

#### Erreur: "SDK location not found"

**Cause**: `sdk.dir` incorrect ou manquant

**Solution**:
1. Ouvrir Android Studio → SDK Manager
2. Copier le chemin affiché en haut (Android SDK Location)
3. Le coller dans `local.properties`:
   ```properties
   sdk.dir=/chemin/copié/ici
   ```

#### Build échoue avec "Unsupported Java"

**Cause**: Version Java incompatible

**Solution**:
1. Android Studio → Settings → Build, Execution, Deployment → Build Tools → Gradle
2. Gradle JDK → Sélectionner "Embedded JDK" ou Java 11+

## Build & Run

### 1. Build le projet

**Via Android Studio**:
- Build → Make Project (`Ctrl+F9` / `Cmd+F9`)

**Via ligne de commande**:
```bash
./gradlew build
```

Si le build réussit, vous verrez:
```
BUILD SUCCESSFUL in Xs
```

### 2. Lancer l'application

#### Option A: Sur émulateur Android

1. **Créer un émulateur** (si pas déjà fait):
   - Tools → Device Manager
   - Create Device
   - Sélectionner un appareil (ex: Pixel 5)
   - Télécharger une image système (API 29+, recommandé: API 34)
   - Finish

2. **Lancer l'émulateur**:
   - Device Manager → Play (▶️) à côté de l'émulateur

3. **Run l'app**:
   - Run → Run 'app' (`Shift+F10` / `Ctrl+R`)

#### Option B: Sur appareil physique

1. **Activer le mode développeur** sur Android:
   - Paramètres → À propos du téléphone
   - Appuyer 7 fois sur "Numéro de build"

2. **Activer le débogage USB**:
   - Paramètres → Options pour développeurs
   - Activer "Débogage USB"

3. **Connecter l'appareil** via USB

4. **Vérifier la connexion**:
   ```bash
   adb devices
   ```
   Devrait afficher votre appareil.

5. **Run l'app**:
   - Run → Run 'app'
   - Sélectionner votre appareil dans la liste

### 3. Vérifier que l'app fonctionne

L'écran principal devrait s'afficher avec:
- Titre "Meta Ray-Ban Assistant"
- Card de statut "Déconnecté"
- Bouton "Connecter aux lunettes"
- Bouton "Capturer une photo" (désactivé)

## Configuration supplémentaire

### Claude API Key (pour l'assistant IA)

Pour utiliser l'assistant IA Claude, vous aurez besoin d'une clé API:

1. Créer un compte sur [console.anthropic.com](https://console.anthropic.com/)
2. Aller dans Settings → API Keys
3. Créer une nouvelle clé API
4. **Important**: La clé sera configurée dans l'app (pas dans `local.properties`)

Pour l'instant, l'intégration Claude n'est pas encore implémentée.

### Meta Ray-Ban Glasses

Pour tester avec de vraies lunettes Meta Ray-Ban:

1. Mettre à jour le firmware à **version 20.0+** via l'app Meta officielle
2. S'assurer que les lunettes sont chargées
3. Activer Bluetooth sur votre appareil Android
4. L'app demandera les permissions nécessaires au premier lancement

**Note**: L'émulateur Android ne peut pas se connecter aux lunettes (limitation Bluetooth).

## Tests

### Tests unitaires

```bash
./gradlew test
```

### Tests instrumentés (nécessite émulateur ou appareil)

```bash
./gradlew connectedAndroidTest
```

### Lint (analyse code)

```bash
./gradlew lint
```

## Commandes utiles

### Nettoyer le projet

```bash
./gradlew clean
```

### Build de release (APK)

```bash
./gradlew assembleRelease
```

L'APK sera dans: `app/build/outputs/apk/release/`

### Logs en temps réel

```bash
adb logcat | grep "MetaRayBan"
```

### Réinstaller l'app

```bash
./gradlew installDebug
```

## Prochaines étapes

Une fois le projet configuré et l'app lancée avec succès:

1. Lire `ARCHITECTURE.md` pour comprendre la structure du code
2. Explorer le code dans `app/src/main/java/`
3. Consulter les TODOs pour voir ce qui reste à implémenter
4. Rejoindre les issues GitHub pour contribuer

## Aide

### Ressources

- [Documentation Android](https://developer.android.com/)
- [Meta Wearables Docs](https://wearables.developer.meta.com/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

### Problèmes

Si vous rencontrez des problèmes non couverts par ce guide:
1. Chercher dans les [GitHub Issues](https://github.com/ichaiwizm/meta-rayban-assistant/issues)
2. Créer une nouvelle issue avec:
   - Message d'erreur complet
   - Sortie de `./gradlew build --stacktrace`
   - Version Android Studio
   - OS utilisé

---

**Félicitations!** Votre environnement de développement est prêt. Bon coding!
