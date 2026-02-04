package com.ichaiwizm.metaraybanassistant

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ichaiwizm.metaraybanassistant.data.source.ApkInstaller
import com.ichaiwizm.metaraybanassistant.data.source.UpdateChecker
import com.ichaiwizm.metaraybanassistant.ui.screens.HomeScreen
import com.ichaiwizm.metaraybanassistant.ui.theme.MetaRayBanAssistantTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private val updateChecker = UpdateChecker()
    private var updateStatus by mutableStateOf("")
    private var showUpdateDialog by mutableStateOf(false)
    private var downloadProgress by mutableStateOf(0)
    private var isDownloading by mutableStateOf(false)
    private var pendingApkFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Récupérer la version actuelle
        val currentVersion = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: Exception) {
            "1.0.0"
        }

        setContent {
            MetaRayBanAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HomeScreen(
                            onCheckUpdate = { checkForUpdates() },
                            updateStatus = updateStatus,
                            currentVersion = currentVersion
                        )
                    }

                    // Dialog de mise à jour
                    if (showUpdateDialog && pendingApkFile != null) {
                        UpdateDialog(
                            onConfirm = {
                                showUpdateDialog = false
                                installUpdate()
                            },
                            onDismiss = {
                                showUpdateDialog = false
                                updateStatus = "Mise à jour annulée"
                            },
                            progress = downloadProgress,
                            isDownloading = isDownloading
                        )
                    }
                }
            }
        }
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
            updateStatus = "Vérification des mises à jour..."

            val currentVersionCode = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    ).longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0).versionCode
                }
            } catch (e: Exception) {
                1
            }

            // Debug log
            android.util.Log.d("UpdateCheck", "Current versionCode: $currentVersionCode")
            Toast.makeText(
                this@MainActivity,
                "Version actuelle: $currentVersionCode",
                Toast.LENGTH_LONG
            ).show()

            val result = updateChecker.checkForUpdate(currentVersionCode)

            result.onSuccess { appVersion ->
                if (appVersion != null) {
                    updateStatus = "Nouvelle version disponible: ${appVersion.versionName}\n${appVersion.changelog}"
                    downloadUpdate(appVersion.downloadUrl)
                } else {
                    updateStatus = "Vous avez la dernière version ✓"
                }
            }.onFailure { error ->
                updateStatus = "Erreur: ${error.message}"
                Toast.makeText(
                    this@MainActivity,
                    "Impossible de vérifier les mises à jour",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun downloadUpdate(downloadUrl: String) {
        lifecycleScope.launch {
            isDownloading = true
            updateStatus = "Téléchargement de la mise à jour..."

            val outputFile = File(externalCacheDir, "update.apk")

            val result = updateChecker.downloadApk(downloadUrl, outputFile) { progress ->
                downloadProgress = progress
                updateStatus = "Téléchargement: $progress%"
            }

            isDownloading = false

            result.onSuccess { apkFile ->
                pendingApkFile = apkFile
                showUpdateDialog = true
                updateStatus = "Mise à jour prête à installer"
            }.onFailure { error ->
                updateStatus = "Erreur de téléchargement: ${error.message}"
                Toast.makeText(
                    this@MainActivity,
                    "Échec du téléchargement",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun installUpdate() {
        pendingApkFile?.let { apkFile ->
            try {
                ApkInstaller.installApk(this, apkFile)
                updateStatus = "Lancementde l'installation..."
            } catch (e: Exception) {
                updateStatus = "Erreur d'installation: ${e.message}"
                Toast.makeText(
                    this,
                    "Impossible d'installer la mise à jour",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Composable
fun UpdateDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    progress: Int,
    isDownloading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mise à jour prête") },
        text = {
            if (isDownloading) {
                Text("Téléchargement en cours: $progress%")
            } else {
                Text("Une nouvelle version de l'application a été téléchargée. Voulez-vous l'installer maintenant?")
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(onClick = onConfirm) {
                    Text("Installer")
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Plus tard")
                }
            }
        }
    )
}
