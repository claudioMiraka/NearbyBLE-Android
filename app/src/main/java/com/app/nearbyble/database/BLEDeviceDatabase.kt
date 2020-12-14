package com.app.nearbyble.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 *  A database that stores [BLEDevice] information
 *  and methods to access it
 *
 */
@Database(entities = [BleDevice::class], version = 2, exportSchema = false)
abstract class BleDeviceDatabase : RoomDatabase() {

    /**
     *  Connection point to database
     */
    abstract val bleDeviceDao: BLEDeviceDao

    /**
     * Use companion objects, acts similar to static fields
     */
    companion object {

        /**
         *  INSTANCE of the database available to just by calling
         *      BleDeviceDatabase.getInstance(context)
         *
         *  The value of a volatile variable will never be cached, and all writes and
         *  reads will be done to and from the main memory. It means that changes made by one
         *  thread to shared data are visible to other threads.
         */
        @Volatile
        var INSTANCE: BleDeviceDatabase? = null

        fun getInstance(context: Context): BleDeviceDatabase {

            synchronized(this) {

                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE

                //if the instance is null create a new database instance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BleDeviceDatabase::class.java,
                        "ble_device_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}