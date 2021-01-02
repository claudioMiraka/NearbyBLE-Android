package com.app.nearbyble.bluetooth

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.app.nearbyble.database.BLEDeviceDao
import com.app.nearbyble.database.BleDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  BLE API for Server
 *
 *  Serves 2 Characteristic:
 *      1 for the client to read which contains  the server 256-bit token
 *      and 1 containing a descriptor the client can write with its token
 */

class BleServerApi(
    private val coroutineScope: CoroutineScope,
    private val context: Context,
    private val dataSource: BLEDeviceDao
) {

    private val TAG = BleServerApi::class.java.simpleName

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var advertiserHelper: AdvertiseHelper

    private var bluetoothGattServer: BluetoothGattServer? = null

    private var isServerRunning: Boolean = false
    private val connectedDevices = mutableListOf<BluetoothDevice>()

    /**
     *  Start advertising for nearby devices
     *  start serving to specific UUID
     */
    fun startServer() {
        if (!isServerRunning) {
            bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            advertiserHelper = AdvertiseHelper(bluetoothManager)
            advertiserHelper.startAdvertising()

            bluetoothGattServer = bluetoothManager.openGattServer(this.context, gattServerCallback)
            isServerRunning = true
            bluetoothGattServer?.addService(createBleService())
                ?: run {
                    isServerRunning = false
                    Log.e(TAG, "Unable to create GATT server")
                }
            Log.i(TAG, "Server started")
        }
    }

    /**
     * Shut down the GATT server.
     */
    fun stopServer() {
        if (isServerRunning) {
            isServerRunning = false
            bluetoothGattServer?.close()
            advertiserHelper.stopAdvertising()
            Log.i(TAG, "Server stopped")
        }
    }

    /**
     *  Server call backs
     *  For more information:
     *      https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback
     *
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.i(TAG, "onConnectionStateChange")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (connectedDevices.indexOfFirst { it.address == device.address } == -1) {
                        Log.i(
                            TAG,
                            "Found BLE device! Name: ${device.name ?: "Unnamed"}, address: $device.address"
                        )
                        connectedDevices.add(device)
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                val bleDevice = BleDevice(
                                    deviceName = device.name ?: "Unknown",
                                    deviceMacAddress = device.address,
                                    deviceFoundFrom = BLE_CONSTANTS.FOUND_FROM_SERVER
                                )
                                dataSource.insert(bleDevice)
                            }
                        }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED ->
                    Log.i(TAG, "BluetoothDevice DISCONNECTED: $device")
                else -> Log.i(TAG, "BluetoothDevice state unknown: $device")
            }
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Log.i(TAG, "MTU changed to $mtu")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            Log.i(TAG, "onCharacteristicWriteRequest from ${characteristic?.uuid}")
            characteristic?.let { connectionUpdates(it) }
            when (characteristic?.uuid) {
                BLE_CONSTANTS.SERVER_CHAR -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        null
                    )
                }
                BLE_CONSTANTS.CLIENT_CHAR -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        null
                    )
                }
                else -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.i(TAG, "onCharacteristicReadRequest from ${characteristic.uuid}")
            when (characteristic.uuid) {
                BLE_CONSTANTS.SERVER_CHAR -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        "F4E454F802B88D2F64168FF1742E8CF413FD677D38B87CBEFB45821F8981B912".toByteArray()
                    )
                }
                else -> {
                    Log.e(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            Log.i(
                TAG,
                "Request to write on ${descriptor?.uuid}\t${value?.toString(Charsets.UTF_8)}"
            )
            //send responce
            bluetoothGattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_FAILURE,
                0,
                null
            )
        }
    }

    private fun connectionUpdates(characteristic: BluetoothGattCharacteristic) {
        with(characteristic) {
            Log.i(TAG, "onConnectionUpdated $uuid")
            when (uuid) {
                BLE_CONSTANTS.SERVER_CHAR ->
                    Log.i(TAG, "Message on SERVER_CHAR : " + value?.toString(Charsets.UTF_8))
                BLE_CONSTANTS.CLIENT_CHAR -> {
                    Log.i(TAG, "Message on CLIENT_CHAR: " + value?.toString(Charsets.UTF_8))
                }
                else -> {
                    value.let { bytes ->
                        val hexString: String = bytes.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        Log.w(TAG, "Message generic: $bytes\n$hexString")
                    }
                }
            }
        }
    }

    /**
     * Create a BLE Service which defines the characters
     * and descriptors served by the server,
     * 1 characteristic will be read only, which server the server token,
     * 1 characteristic will have a descriptor which the client will be writing
     */
    private fun createBleService(): BluetoothGattService {
        val service = BluetoothGattService(
            BLE_CONSTANTS.DISCOVER_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val firstCharacteristic = BluetoothGattCharacteristic(
            BLE_CONSTANTS.SERVER_CHAR,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val secondCharacteristic = BluetoothGattCharacteristic(
            BLE_CONSTANTS.CLIENT_CHAR,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val firstDescriptor = BluetoothGattDescriptor(
            BLE_CONSTANTS.SERVER_DESC,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        firstCharacteristic.addDescriptor(firstDescriptor)

        service.addCharacteristic(firstCharacteristic)
        service.addCharacteristic(secondCharacteristic)
        return service
    }
}