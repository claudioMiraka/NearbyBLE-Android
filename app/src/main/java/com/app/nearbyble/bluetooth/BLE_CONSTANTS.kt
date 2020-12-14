package com.app.nearbyble.bluetooth

import java.util.*

object BLE_CONSTANTS {

    const val STATE_CONNECTED = 2
    const val STATE_DISCONNECTED = 0

    /**
     *  random UUID for Characteristics
     */
    val DISCOVER_SERVICE: UUID =
        UUID.fromString("00001805-0000-1000-8000-00805f9b34fb") //change to your service

    val SERVER_CHAR: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
    val CLIENT_CHAR: UUID = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")

    val SERVER_DESC : UUID = UUID.fromString("00002a33-0000-1000-8000-00805f9b34fb")
}