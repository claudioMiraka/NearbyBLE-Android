package com.app.nearbyble.ui.bleClient

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.nearbyble.bluetooth.BLE_CONSTANTS

import com.app.nearbyble.bluetooth.BleScanner
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

    private val _showToast = MutableLiveData<String?>()

    val showToast: LiveData<String?> get() = _showToast

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
        if (checkPermissions()) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bleScanner.scanLeDevice()
                    _showToast.postValue("Scanning...")
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!permissionsHelper.isBTScanGranted()) {
                permissionsHelper.requestBTScanPermission()
            } else if (!permissionsHelper.isBTConnectGranted()) {
                permissionsHelper.requestBTConnectPermission()
//            } else if (!permissionsHelper.isBTAdvertiseGranted()) {
//                permissionsHelper.requestBTAdvertisePermission()
            } else {
                return true
            }
        } else {
            if (!permissionsHelper.isBluetoothPermissionGranted()) {
                permissionsHelper.requestBluetoothPermission()
            } else if (!permissionsHelper.isForegroundLocationPermissionGranted()) {
                permissionsHelper.requestForegroundLocationPermission()
            } else {
                return true
            }
        }
        _showToast.postValue("Permissions needed...")
        return false
    }

    /**
     * Stop scanning ble devices
     */
    fun stop() {
        bleScanner.stopBleScan()
        _showToast.value = "Stop scanning"
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

    fun showMToastDone() {
        _showToast.value = null
    }
}