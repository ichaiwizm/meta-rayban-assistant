package com.ichaiwizm.metaraybanassistant.data.source

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.ichaiwizm.metaraybanassistant.data.model.BluetoothDevice
import com.ichaiwizm.metaraybanassistant.data.model.ConnectionState
import com.meta.wearable.dat.core.Wearables
import com.meta.wearable.dat.core.session.SessionState
import com.meta.wearable.dat.core.types.DeviceIdentifier
import com.meta.wearable.dat.core.types.DeviceMetadata
import com.meta.wearable.dat.core.types.RegistrationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Wearables Manager for Meta Ray-Ban Glasses
 *
 * Uses Meta Wearables Device Access Toolkit SDK for real connection.
 */
class WearablesManager(private val context: Context) {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var currentDeviceId: DeviceIdentifier? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        // Initialize Wearables SDK
        try {
            Wearables.initialize(context)
            Log.d(TAG, "Wearables SDK initialized")

            // Observe registration state
            scope.launch {
                Wearables.registrationState.collect { state ->
                    Log.d(TAG, "Registration state: $state")
                    when (state) {
                        is RegistrationState.Registered -> {
                            Log.d(TAG, "App is registered with Meta AI")
                            observeDevices()
                        }
                        is RegistrationState.Unavailable -> {
                            Log.d(TAG, "Registration is unavailable")
                        }
                        is RegistrationState.Available -> {
                            Log.d(TAG, "Registration is available")
                        }
                        is RegistrationState.Registering -> {
                            Log.d(TAG, "Registration in progress")
                        }
                        is RegistrationState.Unregistering -> {
                            Log.d(TAG, "Unregistering in progress")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Wearables SDK: ${e.message}", e)
            _connectionState.value = ConnectionState.Error("Erreur SDK: ${e.message}")
        }
    }

    /**
     * Observe available devices from Wearables SDK
     */
    private fun observeDevices() {
        scope.launch {
            // Collect changes from devicesMetadata map
            val devicesMap = Wearables.devicesMetadata

            // For each device, observe its metadata
            val deviceList = mutableListOf<BluetoothDevice>()
            devicesMap.forEach { (deviceId, metadataFlow) ->
                launch {
                    metadataFlow.collect { metadata ->
                        Log.d(TAG, "Device updated: ${metadata.name}")

                        // Update or add device to list
                        val bluetoothDevice = BluetoothDevice(
                            id = deviceId.toString(),
                            name = metadata.name,
                            address = deviceId.toString(),
                            rssi = -50, // Not provided by SDK
                            isConnected = false
                        )

                        // Update the devices list
                        val currentDevices = _discoveredDevices.value.toMutableList()
                        val existingIndex = currentDevices.indexOfFirst { it.id == deviceId.toString() }

                        if (existingIndex >= 0) {
                            currentDevices[existingIndex] = bluetoothDevice
                        } else {
                            currentDevices.add(bluetoothDevice)
                        }

                        _discoveredDevices.value = currentDevices
                        Log.d(TAG, "Total Meta devices: ${currentDevices.size}")
                    }
                }
            }
        }
    }

    /**
     * Start registration with Meta AI app
     * @param activity The activity to use for registration flow
     */
    fun startRegistration(activity: Activity) {
        try {
            Log.d(TAG, "Starting registration with Meta AI app")
            Wearables.startRegistration(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start registration: ${e.message}", e)
            _connectionState.value = ConnectionState.Error("Erreur d'enregistrement: ${e.message}")
        }
    }

    /**
     * Start scanning - In Wearables SDK, devices are automatically discovered
     */
    fun startScanning(): Result<Unit> {
        return try {
            _connectionState.value = ConnectionState.Scanning

            // Check if registered
            scope.launch {
                val currentState = Wearables.registrationState.value
                if (currentState !is RegistrationState.Registered) {
                    _connectionState.value = ConnectionState.Error(
                        "App non enregistrée.\n" +
                        "Lance l'enregistrement d'abord."
                    )
                } else {
                    // Devices are automatically collected
                    delay(2000) // Give time for devices to appear
                    _connectionState.value = ConnectionState.Disconnected
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Scan error: ${e.message}", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Erreur de scan")
            Result.failure(e)
        }
    }

    /**
     * Connect to a device and play voice notification
     */
    fun connectToDevice(device: BluetoothDevice): Result<Unit> {
        return try {
            Log.d(TAG, "Connecting to device: ${device.name}")
            _connectionState.value = ConnectionState.Connecting

            scope.launch {
                try {
                    // Find the DeviceIdentifier from devicesMetadata
                    val deviceIdentifier = Wearables.devicesMetadata.keys.firstOrNull {
                        it.toString() == device.id
                    }

                    if (deviceIdentifier == null) {
                        _connectionState.value = ConnectionState.Error("Appareil introuvable")
                        return@launch
                    }

                    currentDeviceId = deviceIdentifier

                    // Mark as connected immediately since device was found
                    _connectionState.value = ConnectionState.Connected(device)
                    Log.d(TAG, "Device found - Marked as connected")

                    // Observe device session state
                    Wearables.getDeviceSessionState(deviceIdentifier).collect { state ->
                        Log.d(TAG, "Session state: $state")

                        when (state) {
                            SessionState.RUNNING -> {
                                Log.d(TAG, "Session RUNNING - Playing notification")
                                // Play voice notification through glasses
                                playVoiceNotification()
                            }
                            SessionState.PAUSED -> {
                                Log.d(TAG, "Session PAUSED")
                            }
                            SessionState.STOPPED -> {
                                Log.d(TAG, "Session STOPPED - Disconnecting")
                                _connectionState.value = ConnectionState.Disconnected
                            }
                            else -> {
                                Log.d(TAG, "Session state not handled: $state")
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Connection error: ${e.message}", e)
                    _connectionState.value = ConnectionState.Error(
                        "Erreur de connexion: ${e.message}"
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Échec de connexion")
            Result.failure(e)
        }
    }

    /**
     * Play voice notification through glasses speakers using HFP
     */
    private fun playVoiceNotification() {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Routing audio to Bluetooth device")

                // Route audio to Bluetooth HFP device
                val devices = audioManager.availableCommunicationDevices
                val bluetoothDevice = devices.firstOrNull {
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                }

                if (bluetoothDevice != null) {
                    audioManager.mode = AudioManager.MODE_NORMAL
                    audioManager.setCommunicationDevice(bluetoothDevice)

                    Log.d(TAG, "Audio routed to: ${bluetoothDevice.productName}")

                    // Wait for audio routing to complete
                    delay(500)

                    // Play a tone notification (beep)
                    playBeepNotification()

                    Log.d(TAG, "Voice notification played through glasses")
                } else {
                    Log.w(TAG, "No Bluetooth SCO device found for audio")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to play voice notification: ${e.message}", e)
            }
        }
    }

    /**
     * Play a beep tone through the glasses
     */
    private fun playBeepNotification() {
        try {
            val toneGenerator = ToneGenerator(
                AudioManager.STREAM_VOICE_CALL,
                ToneGenerator.MAX_VOLUME
            )

            // Play a pleasant notification tone
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500)

            // Release after playing
            Thread.sleep(600)
            toneGenerator.release()

            Log.d(TAG, "Beep notification played")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play beep: ${e.message}", e)
        }
    }

    /**
     * Disconnect from device
     */
    fun disconnect(): Result<Unit> {
        return try {
            Log.d(TAG, "Disconnecting...")
            currentDeviceId = null
            _connectionState.value = ConnectionState.Disconnected
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Stop scanning
     */
    fun stopScanning() {
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            stopScanning()
            disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "WearablesManager"
    }
}
