package com.app.nearbyble.util

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 *  Helper function used to ask runtime permissions and
 *  enable bluetooth
 *
 */

private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 101
private const val LOCATION_PERMISSION_REQUEST_CODE = 102

class PermissionsHelper(private val activity: Activity) {

    /**
     *  Give context it checks if the given permission is granted
     */
    private fun hasPermission(permissionType: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request permission given a string and a code for results
     */
    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }


    /**
     *  Return true if foreground location permission is granted
     */
    fun isForegroundLocationPermissionGranted(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION, activity.applicationContext)
    }

    /**
     *  Request foreground location permission if not granted
     */
    fun requestForegroundLocationPermission() {
        if (!isForegroundLocationPermissionGranted())
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE
            )
    }

    /**
     *  Return true if BlueTooth permission is granted
     */
    fun isBluetoothPermissionGranted(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH_ADMIN, activity.applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun isBTConnectGranted(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH_CONNECT, activity.applicationContext)
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun isBTScanGranted(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH_SCAN, activity.applicationContext)
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun isBTAdvertiseGranted(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE, activity.applicationContext)
    }

    /**
     *  Request Bluetooth permission if not granted
     */
    fun requestBluetoothPermission() {
        if (!isForegroundLocationPermissionGranted())
            requestPermission(
                Manifest.permission.BLUETOOTH_ADMIN,
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun requestBTScanPermission() {
        if (!isForegroundLocationPermissionGranted())
            requestPermission(
                Manifest.permission.BLUETOOTH_SCAN,
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun requestBTAdvertisePermission() {
        if (!isForegroundLocationPermissionGranted())
            requestPermission(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun requestBTConnectPermission() {
        if (!isForegroundLocationPermissionGranted())
            requestPermission(
                Manifest.permission.BLUETOOTH_CONNECT,
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
    }


    /**
     *  Get bluetooth adaptor
     */
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
}