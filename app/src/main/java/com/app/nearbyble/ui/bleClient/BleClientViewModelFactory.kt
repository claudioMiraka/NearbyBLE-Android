package com.app.nearbyble.ui.bleClient

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.app.nearbyble.database.BLEDeviceDao
import com.app.nearbyble.util.PermissionsHelper

/**
 *  A View Model Factory to help passing parameters to view model
 */
class BleClientViewModelFactory(
    private val dataSource: BLEDeviceDao,
    private val application: Application,
    private val permissionsHelper: PermissionsHelper
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BleClientViewModel::class.java)) {
            return BleClientViewModel(dataSource, application, permissionsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}