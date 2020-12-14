package com.app.nearbyble.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  Data class to hold the information about
 *  a BLE device detected
 */
@Entity(tableName = "ble_device_table")
data class BleDevice(
    @PrimaryKey(autoGenerate = true)
    val deviceID: Long = 0L,

    @ColumnInfo(name = "device_name")
    val deviceName: String = "N/A",

    @ColumnInfo(name = "device_mac_address")
    val deviceMacAddress: String = "N/A",

    @ColumnInfo(name = "device_signal_strength")
    val deviceSignalStrength: String = "N/A",

    @ColumnInfo(name = "device_first_time_seen")
    val deviceTimeSeen: Long = System.currentTimeMillis()
)