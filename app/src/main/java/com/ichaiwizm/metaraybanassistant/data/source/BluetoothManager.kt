package com.ichaiwizm.metaraybanassistant.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice
import android.bluetooth.BluetoothManager as AndroidBluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.ichaiwizm.metaraybanassistant.data.model.BluetoothDevice
import com.ichaiwizm.metaraybanassistant.data.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bluetooth Manager for Meta Ray-Ban Glasses
 *
 * Uses Android Bluetooth API to scan and connect to devices.
 *
 * Note: Full Meta SDK integration requires official documentation
 * from https://wearables.developer.meta.com/ (developer preview access needed)
 */
class BluetoothManager(private val context: Context) {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as AndroidBluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val discoveredDevicesMap = mutableMapOf<String, BluetoothDevice>()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AndroidBluetoothDevice.ACTION_FOUND -> {
                    val device: AndroidBluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE, AndroidBluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE)
                    }

                    val rssi = intent.getShortExtra(AndroidBluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()

                    device?.let {
                        if (checkBluetoothPermission()) {
                            val deviceName = it.name ?: "Unknown Device"
                            val deviceAddress = it.address

                            // Filter for Ray-Ban or show all devices
                            val bluetoothDevice = BluetoothDevice(
                                id = deviceAddress,
                                name = deviceName,
                                address = deviceAddress,
                                rssi = rssi,
                                isConnected = false
                            )

                            discoveredDevicesMap[deviceAddress] = bluetoothDevice
                            _discoveredDevices.value = discoveredDevicesMap.values.toList()

                            Log.d(TAG, "Device found: $deviceName ($deviceAddress) RSSI: $rssi dBm")
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Discovery finished. Found ${discoveredDevicesMap.size} devices")
                    if (_connectionState.value is ConnectionState.Scanning) {
                        _connectionState.value = ConnectionState.Disconnected
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Discovery started")
                }
            }
        }
    }

    private var isReceiverRegistered = false

    /**
     * Start scanning for Bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun startScanning(): Result<Unit> {
        return try {
            if (bluetoothAdapter == null) {
                val error = "Bluetooth not supported on this device"
                _connectionState.value = ConnectionState.Error(error)
                return Result.failure(Exception(error))
            }

            if (!bluetoothAdapter.isEnabled) {
                val error = "Bluetooth is disabled. Please enable it in settings."
                _connectionState.value = ConnectionState.Error(error)
                return Result.failure(Exception(error))
            }

            if (!checkBluetoothPermission()) {
                val error = "Bluetooth permissions not granted"
                _connectionState.value = ConnectionState.Error(error)
                return Result.failure(Exception(error))
            }

            Log.d(TAG, "Starting Bluetooth scan...")
            _connectionState.value = ConnectionState.Scanning
            discoveredDevicesMap.clear()
            _discoveredDevices.value = emptyList()

            // Register receiver if not already registered
            if (!isReceiverRegistered) {
                val filter = IntentFilter().apply {
                    addAction(AndroidBluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context.registerReceiver(bluetoothReceiver, filter)
                isReceiverRegistered = true
            }

            // Cancel any ongoing discovery
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            // Start discovery
            val started = bluetoothAdapter.startDiscovery()
            if (!started) {
                val error = "Failed to start Bluetooth discovery"
                _connectionState.value = ConnectionState.Error(error)
                return Result.failure(Exception(error))
            }

            // Also add already paired devices
            bluetoothAdapter.bondedDevices?.forEach { device ->
                val bluetoothDevice = BluetoothDevice(
                    id = device.address,
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    rssi = -50, // Mock RSSI for bonded devices
                    isConnected = false
                )
                discoveredDevicesMap[device.address] = bluetoothDevice
                Log.d(TAG, "Bonded device: ${device.name} (${device.address})")
            }
            _discoveredDevices.value = discoveredDevicesMap.values.toList()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanning: ${e.message}", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Failed to start scan")
            Result.failure(e)
        }
    }

    /**
     * Connect to a specific device
     *
     * Note: This is a placeholder. Full connection requires Meta SDK DeviceSession
     * See: https://wearables.developer.meta.com/docs/reference/android/dat/0.3/
     */
    fun connectToDevice(device: BluetoothDevice): Result<Unit> {
        return try {
            Log.d(TAG, "Connecting to device: ${device.name}")
            _connectionState.value = ConnectionState.Connecting

            // TODO: Implement Meta SDK DeviceSession connection
            // Example (when SDK docs are available):
            // val session = DeviceSession.create(context, device, callback)
            // session.connect()

            // For now, simulate connection
            _connectionState.value = ConnectionState.Connected(device)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            Result.failure(e)
        }
    }

    /**
     * Disconnect from current device
     */
    fun disconnect(): Result<Unit> {
        return try {
            Log.d(TAG, "Disconnecting...")
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
    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            if (_connectionState.value is ConnectionState.Scanning) {
                _connectionState.value = ConnectionState.Disconnected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan: ${e.message}", e)
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            stopScanning()
            disconnect()
            if (isReceiverRegistered) {
                context.unregisterReceiver(bluetoothReceiver)
                isReceiverRegistered = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    private fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private const val TAG = "BluetoothManager"
    }
}
