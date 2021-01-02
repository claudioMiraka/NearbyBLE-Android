package com.app.nearbyble.ui.bleClient

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.nearbyble.bluetooth.BLE_CONSTANTS

import com.app.nearbyble.bluetooth.BleScanner
import com.app.nearbyble.bluetooth.BleServerApi
import com.app.nearbyble.database.BLEDeviceDao
import com.app.nearbyble.util.PermissionsHelper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A view model for [BleClientFragment]
 */
class BleClientViewModel(
    private val database: BLEDeviceDao,
    application: Application,
    private val permissionsHelper: PermissionsHelper
) :
    AndroidViewModel(application) {

    private val TAG = BleClientViewModel::class.java.simpleName

    private val bleScanner = BleScanner(viewModelScope, application.applicationContext, database)

    /**
     * Live data return from database containing the list of devices
     */
    val devices = database.getDevicesFrom(BLE_CONSTANTS.FOUND_FROM_CLIENT)

    /**
     * Start scanning for BLE devices
     * ask user bluetooth and location permission
     * location has to be granted for android 6.0 and up
     */
    fun start() {
        if (!permissionsHelper.isBluetoothPermissionGranted())
            permissionsHelper.requestBluetoothPermission()
        else if (!permissionsHelper.isForegroundLocationPermissionGranted())
            permissionsHelper.requestForegroundLocationPermission()
        else {
            permissionsHelper.enableBluetooth()
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bleScanner.scanLeDevice()
                }
            }
        }
    }

    /**
     * Stop scanning ble devices
     */
    fun stop() {
        bleScanner.stopBleScan()
    }

    /**
     * Clear all devices from database
     */
    fun clear() {
        Log.i(TAG, "Clearing Database")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.clear()
            }
        }
    }
}