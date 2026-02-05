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

---

**Dernière mise à jour**: 2026-02-05
