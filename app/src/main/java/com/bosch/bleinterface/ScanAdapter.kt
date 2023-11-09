package com.bosch.bleinterface

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScanAdapter(private val items: List<ScanResult>, val clickCallBack: (scanResult: ScanResult) -> Unit) :
    RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {

    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var device_name: TextView
        lateinit var mac_address: TextView
        lateinit var signal_strength: TextView
        lateinit var llLayout: LinearLayout

        @SuppressLint("MissingPermission")
        fun bind(result: ScanResult, clickCallBack: (scanResult: ScanResult) -> Unit) {
            device_name = itemView.findViewById(R.id.device_name)
            mac_address = itemView.findViewById(R.id.mac_address)
            signal_strength = itemView.findViewById(R.id.signal_strength)
            llLayout = itemView.findViewById(R.id.llLayout)

            device_name.text = result.device.name ?: result.scanRecord?.deviceName ?: "UnNamed"
            mac_address.text = result.device.address
            signal_strength.text = "${result.rssi} dBm"

            llLayout.setOnClickListener {
                clickCallBack(result)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_result, parent, false)
        return ScanViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, clickCallBack)
    }

}