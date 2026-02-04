package com.ichaiwizm.metaraybanassistant.data.model

/**
 * Modèle pour les informations de version de l'app
 */
data class AppVersion(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String,
    val mandatory: Boolean = false
)
