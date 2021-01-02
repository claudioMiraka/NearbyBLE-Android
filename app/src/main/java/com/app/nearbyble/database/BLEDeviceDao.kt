package com.app.nearbyble.database


import androidx.lifecycle.LiveData
import androidx.room.*
import com.app.nearbyble.bluetooth.BLE_CONSTANTS

/**
 * Access point to database
 */
@Dao
interface BLEDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bleDevice: BleDevice)

    /**
     * Update ble device with new object
     */
    @Update
    suspend fun update(bleDevice: BleDevice)

    /**
     * This does not delete the table but only its content
     */
    @Query("DELETE FROM ble_device_table")
    suspend fun clear()

    /**
     * Return a list of BLE devices that can be observed for changes
     */
    @Query("SELECT * FROM ble_device_table ORDER BY deviceID DESC")
    fun getAllDevices(): LiveData<List<BleDevice>>

    /**
     * Given a device ID return a BLE device object to be observed
     */
    @Query("SELECT * from ble_device_table WHERE deviceID = :key")
    fun getDeviceById(key: Long): LiveData<BleDevice>

    /**
     * Return a list of BLE devices found from an identifier that can be observed for changes
     */
    @Query("SELECT * FROM ble_device_table WHERE device_found_from = :deviceFoundFrom ORDER BY deviceID DESC")
    fun getDevicesFrom(deviceFoundFrom : Int): LiveData<List<BleDevice>>


}