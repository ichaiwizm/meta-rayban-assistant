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
import com.ichaiwizm.metaraybanassistant.data.model.BluetoothDevice
import com.ichaiwizm.metaraybanassistant.data.model.ConnectionState
import com.ichaiwizm.metaraybanassistant.data.source.ApkInstaller
import com.ichaiwizm.metaraybanassistant.data.source.WearablesManager
import com.ichaiwizm.metaraybanassistant.data.source.UpdateChecker
import com.ichaiwizm.metaraybanassistant.ui.screens.DeviceSelectionScreen
import com.ichaiwizm.metaraybanassistant.ui.screens.HomeScreen
import com.ichaiwizm.metaraybanassistant.ui.theme.MetaRayBanAssistantTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private val updateChecker = UpdateChecker()
    private val wearablesManager by lazy { WearablesManager(applicationContext) }

    // Update state
    private var updateStatus by mutableStateOf("")
    private var showUpdateDialog by mutableStateOf(false)
    private var downloadProgress by mutableStateOf(0)
    private var isDownloading by mutableStateOf(false)
    private var pendingApkFile: File? = null

    // Bluetooth state
    private var bluetoothStatus by mutableStateOf("")
    private var isBluetoothConnected by mutableStateOf(false)
    private var showDeviceSelection by mutableStateOf(false)
    private var discoveredDevices by mutableStateOf<List<BluetoothDevice>>(emptyList())
    private var isScanning by mutableStateOf(false)

    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 1001
    }

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

        // Collect Wearables connection state changes
        lifecycleScope.launch {
            wearablesManager.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Disconnected -> {
                        bluetoothStatus = "Déconnecté"
                        isBluetoothConnected = false
                    }
                    is ConnectionState.Scanning -> {
                        bluetoothStatus = "Recherche d'appareils..."
                        isScanning = true
                    }
                    is ConnectionState.Connecting -> {
                        bluetoothStatus = "Connexion en cours..."
                        isBluetoothConnected = false
                    }
                    is ConnectionState.Connected -> {
                        bluetoothStatus = "Connecté à ${state.device.name}"
                        isBluetoothConnected = true
                        showDeviceSelection = false
                    }
                    is ConnectionState.Error -> {
                        bluetoothStatus = "Erreur: ${state.message}"
                        isBluetoothConnected = false
                        Toast.makeText(
                            this@MainActivity,
                            "Erreur Bluetooth: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Collect discovered devices
        lifecycleScope.launch {
            wearablesManager.discoveredDevices.collect { devices ->
                discoveredDevices = devices
                if (devices.isNotEmpty() && isScanning) {
                    isScanning = false
                }
            }
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
                            currentVersion = currentVersion,
                            onConnectBluetooth = { handleBluetoothConnection() },
                            onRegisterWearables = { registerWithMetaAI() },
                            isBluetoothConnected = isBluetoothConnected,
                            bluetoothStatus = bluetoothStatus
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

                    // Device selection bottom sheet
                    if (showDeviceSelection) {
                        DeviceSelectionScreen(
                            devices = discoveredDevices,
                            isScanning = isScanning,
                            onDeviceSelected = { device ->
                                connectToDevice(device)
                            },
                            onDismiss = {
                                showDeviceSelection = false
                                wearablesManager.stopScanning()
                            }
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

    private fun registerWithMetaAI() {
        wearablesManager.startRegistration(this)
        Toast.makeText(
            this,
            "Ouverture de Meta AI app pour enregistrement...",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun handleBluetoothConnection() {
        if (isBluetoothConnected) {
            // Disconnect
            wearablesManager.disconnect()
        } else {
            // Check permissions and start scanning
            if (checkBluetoothPermissions()) {
                startBluetoothScan()
            } else {
                requestBluetoothPermissions()
            }
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        ActivityCompat.requestPermissions(
            this,
            permissions,
            BLUETOOTH_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startBluetoothScan()
            } else {
                bluetoothStatus = "Permissions Bluetooth refusées"
                Toast.makeText(
                    this,
                    "Les permissions Bluetooth sont requises pour connecter les lunettes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startBluetoothScan() {
        showDeviceSelection = true
        wearablesManager.startScanning()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        wearablesManager.connectToDevice(device)
    }

    override fun onDestroy() {
        super.onDestroy()
        wearablesManager.cleanup()
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
