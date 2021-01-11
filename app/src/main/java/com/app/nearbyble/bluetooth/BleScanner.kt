package com.app.nearbyble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.app.nearbyble.database.BLEDeviceDao
import com.app.nearbyble.database.BleDevice
import kotlinx.coroutines.*

/***
 *  BLE Device scanner
 *
 *  Scan for new device on a specif UUID and handle discover
 *
 */
class BleScanner(
    private val coroutineScope: CoroutineScope,
    private val context: Context,
    private val dataSource: BLEDeviceDao
) {

    private val TAG = BleScanner::class.java.simpleName

    private val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    //  Temporary list of devices discovered
    private val scanResults = mutableListOf<ScanResult>()

    private var isScanning = false

    //Ble scanner settings
    private val settings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()

    /**
     *  Filter for a specific service
     *
     *  This is where we filter to find app running the same app
     */
    private val filter =
        ScanFilter.Builder().setServiceUuid(ParcelUuid(BLE_CONSTANTS.DISCOVER_SERVICE)).build()

    /**
     * Start scanning for ble devbleServer.startServer()ices
     */
    fun scanLeDevice() {
        if (!isScanning) {
            Log.i(TAG, "BLE scanner scanning.")
            isScanning = true
            bluetoothLeScanner.startScan(listOf(filter), settings, leScanCallback)
        }
    }

    /**
     * Stop scanning
     */
    fun stopBleScan() {
        if (isScanning) {
            Log.i(TAG, "BLE scanner stopped scanning.")
            bluetoothLeScanner.stopScan(leScanCallback)
            isScanning = false
        }
    }

    /**
     * Ble scanner callBack handle new discovers
     */
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    if (scanResults.indexOfFirst { it.device.address == result.device.address } == -1) {
                        Log.i(
                            TAG,
                            "New  BLE device found! Name: ${result.device.name ?: "Unnamed"}, address: ${result.device.address}"
                        )
                        scanResults.add(result)
                        BleClientApi(result.device, context).connect()
                        val device = BleDevice(
                            deviceName = result.device.name,
                            deviceMacAddress = result.device.address,
                            deviceSignalStrength = result.rssi.toString(),
                            deviceFoundFrom = BLE_CONSTANTS.FOUND_FROM_CLIENT
                        )
                        dataSource.insert(device)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed: code $errorCode")
        }
    }
}
