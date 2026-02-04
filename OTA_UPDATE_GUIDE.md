# Guide des Mises à Jour OTA (Over-The-Air)

Cette app inclut un système de mise à jour automatique qui permet de déployer de nouvelles versions directement sur votre téléphone sans passer par le Play Store.

## Comment ça fonctionne?

L'app vérifie un fichier `version.json` hébergé sur GitHub qui contient les informations de la dernière version. Si une nouvelle version est disponible, elle la télécharge et propose de l'installer automatiquement.

## Configuration

### 1. Fichier version.json

Le fichier `version.json` doit être accessible publiquement. Par défaut, il est cherché ici:
```
https://raw.githubusercontent.com/ichaiwizm/meta-rayban-assistant/master/version.json
```

**Structure du fichier:**
```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "https://github.com/ichaiwizm/meta-rayban-assistant/releases/download/v1.1.0/app-release.apk",
  "changelog": "- Correction de bugs\n- Amélioration des performances",
  "mandatory": false
}
```

**Champs:**
- `versionCode`: Numéro de version (doit être > à la version actuelle)
- `versionName`: Nom de version affiché (ex: "1.1.0")
- `downloadUrl`: URL directe vers l'APK à télécharger
- `changelog`: Description des changements
- `mandatory`: Si true, force la mise à jour (future feature)

### 2. Héberger l'APK

Vous avez plusieurs options pour héberger l'APK:

#### Option A: GitHub Releases (Recommandé)
1. Build l'APK release: `./gradlew assembleRelease`
2. L'APK se trouve dans: `app/build/outputs/apk/release/app-release.apk`
3. Créer une release sur GitHub:
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```
4. Aller sur GitHub → Releases → Create a new release
5. Upload `app-release.apk`
6. L'URL sera: `https://github.com/ichaiwizm/meta-rayban-assistant/releases/download/v1.1.0/app-release.apk`

#### Option B: Serveur personnel
- Héberger l'APK sur votre serveur web
- Assurer que l'URL est accessible publiquement
- Mettre à jour `downloadUrl` dans `version.json`

#### Option C: Firebase Hosting / Cloud Storage
- Upload l'APK vers Firebase Storage
- Obtenir l'URL publique
- Mettre à jour `version.json`

### 3. Processus de mise à jour

**Étapes dans l'app:**
1. L'utilisateur appuie sur "Vérifier les mises à jour"
2. L'app télécharge `version.json`
3. Compare `versionCode` avec la version actuelle
4. Si nouvelle version disponible:
   - Télécharge l'APK
   - Affiche un dialog
   - Propose d'installer la mise à jour
5. L'utilisateur confirme → Installation Android standard

## Déployer une nouvelle version

### Étape 1: Incrémenter la version

Dans `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2  // Incrémenter
    versionName = "1.1.0"  // Mettre à jour
    ...
}
```

### Étape 2: Build l'APK release

```bash
# Générer l'APK release
./gradlew assembleRelease

# L'APK sera ici:
# app/build/outputs/apk/release/app-release.apk
```

### Étape 3: Upload l'APK

**Sur GitHub Releases:**
```bash
# Créer un tag
git tag v1.1.0
git push origin v1.1.0

# Puis sur GitHub UI:
# 1. Releases → New release
# 2. Tag: v1.1.0
# 3. Upload app-release.apk
# 4. Publish release
```

### Étape 4: Mettre à jour version.json

Éditer `version.json`:
```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "https://github.com/ichaiwizm/meta-rayban-assistant/releases/download/v1.1.0/app-release.apk",
  "changelog": "- Feature XYZ ajoutée\n- Bug ABC corrigé",
  "mandatory": false
}
```

### Étape 5: Commit et push

```bash
git add version.json
git commit -m "Update version to 1.1.0"
git push origin master
```

**C'est tout!** Les utilisateurs verront la mise à jour la prochaine fois qu'ils ouvriront l'app.

## Signer l'APK pour la release

Pour que les mises à jour fonctionnent, tous les APK doivent être signés avec la même clé.

### Créer une clé de signature (première fois seulement)

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

**⚠️ IMPORTANT:** Sauvegarder `my-release-key.jks` en lieu sûr!

### Configurer Gradle pour signer automatiquement

Dans `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../my-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "my-key-alias"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            ...
        }
    }
}
```

**Variables d'environnement:**
```bash
export KEYSTORE_PASSWORD="votre_mot_de_passe"
export KEY_PASSWORD="votre_mot_de_passe"
./gradlew assembleRelease
```

## Personnaliser l'URL du version.json

Par défaut, l'URL est:
```
https://raw.githubusercontent.com/ichaiwizm/meta-rayban-assistant/master/version.json
```

Pour changer l'URL, modifier dans `UpdateChecker.kt`:
```kotlin
class UpdateChecker(
    private val updateJsonUrl: String = "VOTRE_URL_ICI"
)
```

## Tester les mises à jour

### Test en local

1. Build deux versions différentes:
   - Version 1 (versionCode = 1)
   - Version 2 (versionCode = 2)

2. Installer la version 1 sur le téléphone

3. Héberger temporairement:
   - `version.json` (avec versionCode = 2)
   - `app-release.apk` (version 2)

4. Utiliser un serveur local ou ngrok:
   ```bash
   # Serveur Python simple
   python3 -m http.server 8000

   # Ou avec ngrok pour HTTPS
   ngrok http 8000
   ```

5. Mettre à jour l'URL dans `UpdateChecker.kt` pour pointer vers votre serveur

6. Tester la mise à jour dans l'app

## Sécurité

### Bonnes pratiques

1. **HTTPS uniquement**: Ne jamais utiliser HTTP pour les mises à jour
2. **Vérifier la signature**: Future feature pour vérifier que l'APK est signé correctement
3. **Checksum**: Ajouter un hash SHA-256 dans version.json pour vérifier l'intégrité
4. **Ne pas exposer la clé**: Garder `my-release-key.jks` privé et sécurisé

### Amélioration future: Vérification de signature

Ajouter dans `version.json`:
```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "...",
  "sha256": "abcd1234...",
  "changelog": "...",
  "mandatory": false
}
```

## Troubleshooting

### "Installation bloquée"
- Activer "Autoriser l'installation d'applications de sources inconnues" dans les paramètres Android
- Sur Android 8+: Cette permission est par app

### "Échec du téléchargement"
- Vérifier que l'URL de l'APK est accessible
- Tester dans un navigateur
- Vérifier les permissions Internet dans AndroidManifest.xml

### "Erreur de vérification"
- Le fichier `version.json` n'est pas accessible
- Format JSON invalide
- Vérifier l'URL dans `UpdateChecker.kt`

### "Mise à jour non détectée"
- Vérifier que `versionCode` dans version.json est bien > à la version actuelle
- Vérifier les logs: `adb logcat | grep Update`

## Limites

- Nécessite l'activation manuelle de "Sources inconnues" par l'utilisateur
- Pas de mise à jour silencieuse (Android ne le permet pas sans root)
- L'utilisateur doit confirmer l'installation
- Taille du fichier: Pas optimale pour les très gros APK (>100MB)

## Alternative: Google Play Console

Pour une distribution plus professionnelle:
- Utiliser les Internal/Closed/Open Testing tracks de Google Play
- Mises à jour automatiques via Play Store
- Pas besoin de ce système OTA

---

**Dernière mise à jour**: 2026-02-04
