package com.app.nearbyble.ui.bleServer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.app.nearbyble.*
import com.app.nearbyble.database.BleDeviceDatabase
import com.app.nearbyble.databinding.BleServerFragBinding
import com.app.nearbyble.util.PermissionsHelper

/**
 * A fragment containing the list of devices discovered
 */
class BleServerFragment : Fragment() {

    private val TAG = BleServerFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<BleServerFragBinding>(
            inflater,
            R.layout.ble_server_frag,
            container,
            false
        )

        //application needed to construct android view model
        val application = requireNotNull(this.activity).application

        //database access point for view model
        val database = BleDeviceDatabase.getInstance(application).bleDeviceDao

        //permission helper to ask permissions and enable bluetooth
        val permissionHelper = PermissionsHelper(this.requireActivity())

        val bleServerViewModelFactory =
            BleServerViewModelFactory(database, application, permissionHelper)
        val bleServerViewModel =
            ViewModelProvider(this, bleServerViewModelFactory).get(BleServerViewModel::class.java)

        binding.bleServerViewModel = bleServerViewModel
        binding.lifecycleOwner = this

        //set up recycler view with custom adapter
        val manager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        binding.devicesList.layoutManager = manager

        val adapter = BleDevicesAdapter(BleDevicesListener { bleDevice ->
            Toast.makeText(application, "device id: ${bleDevice.deviceID}", Toast.LENGTH_SHORT)
                .show()
        })
        binding.devicesList.adapter = adapter

        //update the list from database
        bleServerViewModel.devices.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitLewList(it)
            }
        })

        return binding.root
    }
}