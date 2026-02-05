# Règles de Développement - Meta RayBan Assistant

## Principe Fondamental: Honnêteté UI

**CRITIQUE**: L'interface utilisateur ne doit JAMAIS afficher d'informations fausses ou simulées.

### Règles:
1. ❌ **INTERDIT**: Afficher "Connecté" si la connexion n'est pas réelle
2. ❌ **INTERDIT**: Simuler des états ou fonctionnalités qui ne marchent pas vraiment
3. ❌ **INTERDIT**: Donner l'impression qu'une feature marche alors qu'elle est en stub
4. ✅ **REQUIS**: Afficher clairement "En développement" ou "Non implémenté" si nécessaire
5. ✅ **REQUIS**: Toute fonctionnalité affichée doit être réellement fonctionnelle

### Exemples:

**❌ MAUVAIS:**
```kotlin
// Simule une connexion qui n'existe pas
_connectionState.value = ConnectionState.Connected(device)
// UI affiche "Connecté" alors que rien n'est connecté
```

**✅ BON:**
```kotlin
// Affiche clairement que c'est en développement
_connectionState.value = ConnectionState.Error("Connexion réelle en développement - SDK Meta requis")
// Ou désactive le bouton avec un message clair
```

### Application aux Lunettes Meta Ray-Ban:
- Ne pas dire "Connecté" tant que le SDK Meta n'est pas vraiment intégré
- Ne pas afficher de statut de connexion fictif
- Être transparent sur les limitations actuelles

## Architecture

- Utiliser StateFlow pour l'état réactif
- Pattern Repository pour la logique métier
- Result<T> pour la gestion d'erreurs
- Coroutines pour l'async

## Meta SDK

- SDK en developer preview
- Documentation limitée publiquement
- Nécessite configuration via app Meta AI officielle
- Connexion réelle requiert implémentation complète du SDK

## Processus de Déploiement

### Étape 1: Modifications du Code
1. Faire les modifications nécessaires dans le code
2. Tester localement si possible

### Étape 2: Bump Version
**Fichier**: `app/build.gradle.kts`

```kotlin
versionCode = X + 1      // Incrémenter de 1
versionName = "X.Y.Z"    // Suivre semver
```

**Règles de versioning**:
- **Patch (X.Y.Z+1)**: Bug fix, petite amélioration
- **Minor (X.Y+1.0)**: Nouvelle fonctionnalité
- **Major (X+1.0.0)**: Breaking change, refonte majeure

### Étape 3: Build Release
```bash
./gradlew assembleRelease
```

**Vérifier**:
- `BUILD SUCCESSFUL`
- APK généré dans `app/build/outputs/apk/release/app-release.apk`

### Étape 4: Commit et Tag
```bash
# Stage les fichiers modifiés
git add <fichiers-modifiés>

# Commit avec message descriptif
git commit -m "type: Description courte

- Détail changement 1
- Détail changement 2
- Bump version X.Y.Z (versionCode N)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

# Créer le tag
git tag vX.Y.Z

# Push tout
git push && git push --tags
```

**Types de commit**:
- `feat`: Nouvelle fonctionnalité
- `fix`: Correction de bug
- `chore`: Maintenance, mise à jour dépendances
- `refactor`: Refactoring sans changement fonctionnel
- `docs`: Documentation uniquement

### Étape 5: Release GitHub
```bash
gh release create vX.Y.Z \
  app/build/outputs/apk/release/app-release.apk \
  --title "vX.Y.Z - Titre Court" \
  --notes "$(cat <<'EOF'
## 🎉 Titre de la Release

### Nouveautés
- ✅ Feature 1
- ✅ Feature 2

### Corrections
- 🔧 Fix 1
- 🔧 Fix 2

### Notes
- Info importante 1
- Info importante 2

🚀 Build: Claude Sonnet 4.5
EOF
)"
```

**L'URL du release sera**: `https://github.com/ichaiwizm/meta-rayban-assistant/releases/tag/vX.Y.Z`

### Étape 6: Mise à Jour OTA (version.json)
**Fichier**: `version.json` (racine du projet)

```json
{
  "versionCode": N,
  "versionName": "X.Y.Z",
  "changelog": "🎉 vX.Y.Z - Titre\n\n✅ NOUVEAU:\n- Feature 1\n- Feature 2\n\n🔧 CORRIGÉ:\n- Fix 1\n- Fix 2\n\n📝 Notes importantes",
  "downloadUrl": "https://github.com/ichaiwizm/meta-rayban-assistant/releases/download/vX.Y.Z/app-release.apk",
  "releaseDate": "YYYY-MM-DD",
  "minAndroidVersion": 29,
  "targetAndroidVersion": 34
}
```

**Puis commit et push**:
```bash
git add version.json
git commit -m "chore: Update OTA to vX.Y.Z

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
git push
```

### Étape 7: Vérification
1. ✅ Tag existe: `git tag -l | grep vX.Y.Z`
2. ✅ Release GitHub visible: Ouvrir l'URL du release
3. ✅ APK téléchargeable depuis le release
4. ✅ version.json à jour sur GitHub: `curl https://raw.githubusercontent.com/ichaiwizm/meta-rayban-assistant/master/version.json`
5. ✅ OTA fonctionne: Ouvrir l'app → Vérifier les mises à jour

### Checklist Complète
- [ ] Code modifié et testé
- [ ] `versionCode` et `versionName` incrémentés
- [ ] `./gradlew assembleRelease` réussi
- [ ] Commit avec message clair
- [ ] Tag créé `vX.Y.Z`
- [ ] Push code + tags
- [ ] Release GitHub créé avec APK
- [ ] `version.json` mis à jour
- [ ] `version.json` commit et push
- [ ] Vérification: tag, release, APK, OTA

### Ordre Strict
**TOUJOURS respecter cet ordre**:
1. Build → 2. Commit → 3. Tag → 4. Push → 5. Release → 6. version.json → 7. Push

**Ne JAMAIS**:
- Créer le tag avant le commit
- Créer le release avant le push
- Oublier de mettre à jour version.json
- Push sans avoir testé le build

### En Cas d'Erreur
**Tag déjà existant**:
```bash
# Supprimer localement
git tag -d vX.Y.Z

# Supprimer sur remote (ATTENTION!)
git push --delete origin vX.Y.Z

# Recréer
git tag vX.Y.Z
git push --tags
```

**Release raté**:
```bash
# Supprimer le release
gh release delete vX.Y.Z

# Recréer
gh release create vX.Y.Z ...
```

---

**Dernière mise à jour**: 2026-02-05
