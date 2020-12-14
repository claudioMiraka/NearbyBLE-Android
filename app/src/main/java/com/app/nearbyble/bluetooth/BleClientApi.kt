package com.app.nearbyble.bluetooth

import android.bluetooth.*
import android.content.ClipDescription
import android.content.Context
import android.util.Log

/**
 *  BLE API for Client
 *
 *  Connects and disconnects to Gatt Server
 *  Exchanges a 256-SHA token
 *
 *  Start discovering when MTU size changes successfully
 *
 */
class BleClientApi(private var device: BluetoothDevice, private val context: Context) {

    private val TAG = BleClientApi::class.java.simpleName

    private var connectionState = BLE_CONSTANTS.STATE_DISCONNECTED

    private lateinit var bluetoothGatt: BluetoothGatt

    /**
     * Connect to the gatt server
     */
    fun connect() {
        Log.i(TAG, "Connecting to ${device.name} , ${device.address}")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    /**
     * Disconnect from the gatt server
     */
    private fun disconnect() {
        Log.i(TAG, "Disconnecting from ${device.name} , ${device.address}")
        bluetoothGatt.close()
    }

    /**
     * Gatt callback to read and write on characteristics and descriptors
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    connectionState = BLE_CONSTANTS.STATE_CONNECTED
                    gatt.requestMtu(256) //  SHA-256
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    connectionState = BLE_CONSTANTS.STATE_DISCONNECTED
                }
                else -> Log.i(TAG, "Connection change to unknown state: $newState")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i(TAG, "onServicesDiscovered: $status, ${gatt.device.address}")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    //once service discovered try to read on server character
                    val service = gatt.getService(BLE_CONSTANTS.DISCOVER_SERVICE)
                    readCharacteristic(gatt, service.getCharacteristic(BLE_CONSTANTS.SERVER_CHAR))
                }
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicRead ${characteristic.uuid}")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    connectionUpdates(characteristic)
//                    writeCharacteristic(
//                        gatt,
//                        gatt.getService(BLE_CONSTANTS.DISCOVER_SERVICE)
//                            .getCharacteristic(BLE_CONSTANTS.CLIENT_CHAR),
//                        //256 SHA encryption token example
//                        //sent to the BLE gatt server
//                        "2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824".toByteArray()
//                    )
                    writeDescriptor(
                        gatt,
                        gatt.getService(BLE_CONSTANTS.DISCOVER_SERVICE)
                            .getCharacteristic(BLE_CONSTANTS.SERVER_CHAR)
                            .getDescriptor(BLE_CONSTANTS.SERVER_DESC),
                        //256 SHA encryption token example
                        //sent to the BLE gatt server
                        "2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824".toByteArray()
                    )
                }
                else -> {
                    Log.w(TAG, "Error reading Characteristic. Returned status $status")
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            disconnect()
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicWrite called with status: $status")
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(TAG, "Wrote to characteristic $uuid ")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e(TAG, "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e(TAG, "Write not permitted for $uuid!")
                    }
                    BluetoothGatt.CONNECTION_PRIORITY_HIGH -> {
                        Log.e(TAG, "Failed $uuid, error: $status")
                    }
                    else -> {
                        Log.e(TAG, "Characteristic write failed for $uuid, error: $status")
                    }
                }
                Log.i(TAG, "onCharacteristicWrite: Disconnecting")
                disconnect()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.i(TAG, "on onCharacteristicChanged called at ${characteristic.uuid}")
            connectionUpdates(characteristic)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i(TAG, "ATT MTU changed to $mtu, returned: $status")
            if (status == BluetoothGatt.GATT_SUCCESS)
                gatt.discoverServices()
            else
                Log.e(TAG, "ATT MTU  failed to change")
        }
    }

    private fun connectionUpdates(characteristic: BluetoothGattCharacteristic) {
        with(characteristic) {
            Log.i(TAG, "onConnectionUpdated $uuid")
            when (uuid) {
                BLE_CONSTANTS.SERVER_CHAR ->
                    Log.i(TAG, "Message on SERVER_CHAR: " + value.toString(Charsets.UTF_8))
                BLE_CONSTANTS.CLIENT_CHAR ->
                    Log.i(TAG, "Message on CLIENT_CHAR " + value.toString(Charsets.UTF_8))
                else -> {
                    value.let { bytes ->
                        val hexString: String = bytes.joinToString(separator = " ") { byte ->
                            String.format("%02X", byte)
                        }
                        Log.w(TAG, "Message generic: $bytes\n$hexString")
                    }
                }
            }
        }
    }

    private fun writeCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }
        characteristic.writeType = writeType
        characteristic.value = payload
        //Log.i(TAG, "Message to send to the server : " + payload.toString(Charsets.UTF_8))
        val status = gatt.writeCharacteristic(characteristic)
        Log.i(TAG, "writeCharacteristic  returned value: $status")
    }

    private fun writeDescriptor(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        payload: ByteArray
    ) {

        descriptor.value = payload
        //Log.i(TAG, "Message to send to the server : " + payload.toString(Charsets.UTF_8))
        val status = gatt.writeDescriptor(descriptor)
        Log.i(TAG, "writeDescriptor  returned value: $status")
    }

    private fun readCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.isReadable()) {
            val status = gatt.readCharacteristic(characteristic)
            Log.i(TAG, "readCharacteristic returned value: $status")
        } else
            Log.w(TAG, "Could not read characteristic: Not readable")
    }

    private fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

}