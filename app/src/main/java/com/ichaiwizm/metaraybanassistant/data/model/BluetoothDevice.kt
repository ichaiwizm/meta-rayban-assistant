package com.ichaiwizm.metaraybanassistant.data.model

data class BluetoothDevice(
    val id: String,
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnected: Boolean = false
)
