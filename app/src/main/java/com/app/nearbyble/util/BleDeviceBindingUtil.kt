package com.app.nearbyble.util

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.app.nearbyble.database.BleDevice
import java.text.SimpleDateFormat

/**
 * Take the Long milliseconds and
 * convert it to a formatted string for display.
 */
@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("MMM-dd-yyyy HH:mm")
        .format(systemTime).toString()
}

/**
 * Extends TextView to use this function inside xml adaptor
 */
@BindingAdapter("firstTimeSeen")
fun TextView.setFirstSeen(item: BleDevice?) {
    item?.let {
        text = convertLongToDateString(item.deviceTimeSeen)
    }
}