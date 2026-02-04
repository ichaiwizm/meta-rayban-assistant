# Quick Start Guide - Meta Ray-Ban Assistant

## 🚀 Démarrage Rapide en 5 Étapes

### Étape 1: Créer un GitHub Token (2 min)

1. Aller sur https://github.com/settings/tokens
2. Cliquer "Generate new token (classic)"
3. Cocher uniquement **`read:packages`**
4. Générer et **copier le token** (commence par `ghp_`)

### Étape 2: Configurer les Credentials (1 min)

```bash
cd meta-rayban-assistant
cp local.properties.example local.properties
```

Éditer `local.properties` et ajouter:
```properties
sdk.dir=/home/VOTRE_USER/Android/Sdk
github.username=VOTRE_USERNAME
github.token=ghp_VOTRE_TOKEN_ICI
```

### Étape 3: Ouvrir dans Android Studio (2 min)

1. Lancer Android Studio
2. **File → Open**
3. Sélectionner le dossier `meta-rayban-assistant`
4. Cliquer **OK**
5. Attendre Gradle sync (5-10 min la première fois)

### Étape 4: Build le Projet (3 min)

Dans Android Studio:
- **Build → Make Project** (Ctrl+F9)

Ou en ligne de commande:
```bash
./gradlew build
```

Si succès, vous verrez:
```
BUILD SUCCESSFUL in Xs
```

### Étape 5: Lancer l'Application (1 min)

1. Créer/démarrer un émulateur (API 29+)
2. **Run → Run 'app'** (Shift+F10)
3. L'app s'ouvre avec l'écran "Meta Ray-Ban Assistant"

## ✅ Vérification

L'écran doit afficher:
- ✅ Titre "Meta Ray-Ban Assistant"
- ✅ Card "Statut: Déconnecté"
- ✅ Bouton "Connecter aux lunettes"
- ✅ Bouton "Capturer une photo" (grisé)

## 📚 Aller Plus Loin

| Fichier | Contenu |
|---------|---------|
| `README.md` | Vue d'ensemble du projet |
| `SETUP.md` | Guide complet d'installation |
| `ARCHITECTURE.md` | Explication technique |
| `TODO.md` | Liste des features à développer |
| `IMPLEMENTATION_SUMMARY.md` | Ce qui a été fait |

## 🐛 Problèmes Courants

### "Unable to resolve dependency 'com.meta.wearable:mwdat-core'"

**Solution**: Token GitHub invalide
1. Vérifier que le token a le scope `read:packages`
2. Vérifier qu'il n'y a pas d'espaces dans `local.properties`
3. Regénérer un nouveau token si nécessaire

### "SDK location not found"

**Solution**: Chemin SDK incorrect
1. Android Studio → Settings → SDK Manager
2. Copier le chemin affiché (Android SDK Location)
3. Le coller dans `local.properties` pour `sdk.dir`

### Gradle sync échoue

**Solutions**:
1. File → Invalidate Caches / Restart
2. Supprimer `.gradle/` et rebuild
3. Vérifier connexion internet

## 🎯 Prochaine Étape

Une fois l'app lancée avec succès, consultez `TODO.md` pour voir les features à implémenter:

**Priorités (P0)**:
1. Implémenter la connexion Bluetooth
2. Intégrer le Meta Wearables SDK
3. Créer le client API Claude

## 💬 Besoin d'Aide?

- **Installation**: Voir `SETUP.md`
- **Architecture**: Voir `ARCHITECTURE.md`
- **GitHub Issues**: Pour reporter des bugs

---

**Temps total estimé**: 15 minutes
**Prérequis**: Android Studio, JDK 8+
