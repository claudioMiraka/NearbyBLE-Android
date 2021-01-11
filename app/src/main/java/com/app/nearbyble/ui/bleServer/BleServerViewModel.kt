package com.app.nearbyble.ui.bleServer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.nearbyble.bluetooth.BLE_CONSTANTS

import com.app.nearbyble.bluetooth.BleServerApi
import com.app.nearbyble.database.BLEDeviceDao
import com.app.nearbyble.util.PermissionsHelper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A view model for [BleClientFragment]
 */
class BleServerViewModel(
    private val database: BLEDeviceDao,
    application: Application,
    private val permissionsHelper: PermissionsHelper
) :
    AndroidViewModel(application) {

    private val TAG = BleServerViewModel::class.java.simpleName

    private val bleServer = BleServerApi(viewModelScope, application.applicationContext, database)

    private val _showToast = MutableLiveData<String>()

    val showToast : LiveData<String> get() = _showToast

    fun showToastDone(){
        _showToast.value = null
    }

    /**
     * Live data return from database containing the list of devices
     */
    val devices = database.getDevicesFrom(BLE_CONSTANTS.FOUND_FROM_SERVER)

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
                    bleServer.startServer()
                    _showToast.postValue("Serving...")
                }
            }
        }
    }

    /**
     * Stop scanning ble devices
     */
    fun stop() {
        bleServer.stopServer()
        _showToast.value = "Stop serving"
    }

    /**
     * Clear all devices from database
     */
    fun clear() {
        Log.i(TAG, "Clearing Database")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.clear()
                _showToast.postValue("Database cleared")
            }
        }
    }
}