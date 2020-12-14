package com.app.nearbyble.ui.bleServer

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.app.nearbyble.database.BleDevice
import com.app.nearbyble.databinding.BleServerListItemBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  Custom Adapter to display list of Ble Devices with a click Listener
 */
class BleDevicesAdapter(private val clickListener: BleDevicesListener) : ListAdapter<BleDevice,
        BleDevicesAdapter.ViewHolder>(BleDevicesDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default);

    fun submitLewList(devices: List<BleDevice>){
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                submitList(devices)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: BleServerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: BleDevicesListener, item: BleDevice) {
            binding.device = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BleServerListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 *  Diff Class that specifies when to update items in the recycler view
 */
class BleDevicesDiffCallback : DiffUtil.ItemCallback<BleDevice>() {
    override fun areItemsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
        return oldItem.deviceID == newItem.deviceID
    }

    override fun areContentsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
        return oldItem == newItem
    }
}

/**
 *  Click Listener to take action when item on the list is clicked
 */
class BleDevicesListener(val clickListener: (bleDevice: BleDevice) -> Unit) {
    fun onClick(bleDevice: BleDevice) = clickListener(bleDevice)
}