package com.bosch.bleinterface

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bosch.bleinterface.databinding.ActivityBleBinding
import com.bosch.blelibrary.BleManager
import com.bosch.blelibrary.IBleManager
import com.bosch.blelibrary.ServiceCharacteristicsMap

class BleActivity : AppCompatActivity() {

    private var scanResults = mutableListOf<ScanResult>()

    private lateinit var binding: ActivityBleBinding

    private lateinit var bleManager: IBleManager

    private val requestBlePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value
        }
        if (permissions.isNotEmpty() && granted) {
            requestLocationPermission()
        } else {
            // PERMISSION NOT GRANTED
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value
        }
        if (permissions.isNotEmpty() && granted) {
            enableAndScanBle()
        } else {
            // PERMISSION NOT GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    val scanResult = { it: ScanResult? ->
        it?.let { scanResult ->
            if (scanResults.isEmpty()) {
                scanResults.add(scanResult)
            } else {
                val newList =
                    scanResults.filter { scan -> scan.device.address == scanResult.device.address }
                if (newList.isEmpty()) {
                    scanResults.add(scanResult)
                } else {

                }
            }


        }
        setupRecyclerView(scanResults)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceCharacteristicArray = arrayOf(
            ServiceCharacteristicsMap(
                Constants.BATTERY_SERVICE,
                Constants.BATTERY_SERVICE_CHARACTERISTIC
            )
        )

        bleManager = BleManager(
            this,
            serviceCharacteristicsMap = serviceCharacteristicArray,
            result = scanResult,
            properties = ::properties
        )
        bleManager.initialize()

        executeBleOperations()
    }

    private fun <K> properties(result: K) {
        runOnUiThread {
            when (result) {
                is Int -> Toast.makeText(this, "Battery Level : $result", Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(this, "Battery Level : $result", Toast.LENGTH_LONG)
                    .show()

                else -> Toast.makeText(this, "Unknown Data type", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun executeBleOperations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!bleManager.isBlePermissionGranted()) {
                requestBluetoothPermissions()
                return
            }
        }
        if (!bleManager.isLocationPermissionGranted()) {
            requestLocationPermission()
            return
        }
        enableAndScanBle()
    }

    private fun enableAndScanBle() {
        if (bleManager.isBluetoothEnabled()) {
            scanLeDevices()
        } else {
            launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun scanLeDevices() {
        bleManager.scanLeDevices(Constants.THINGY_BASE_UUID)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                scanLeDevices()
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView(scanResults: List<ScanResult>) {
        if (binding.rvScanList.adapter == null) {
            val scanAdapter = ScanAdapter(scanResults) {
                if (bleManager.isBlePermissionGranted())
                    bleManager.connect(it)
            }
            binding.rvScanList.layoutManager = LinearLayoutManager(this)
            binding.rvScanList.adapter = scanAdapter
        } else {
            (binding.rvScanList.adapter as? ScanAdapter)?.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothPermissions() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Bluetooth permissions required")
        alert.setMessage("Starting from Android 12, the system requires apps to be granted, Bluetooth access in order to scan for and connect to BLE devices.")
        alert.setCancelable(false)
        alert.setPositiveButton(
            "OK"
        ) { dialog, _ ->
            requestBlePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
            dialog.cancel()
        }
        val aDialog = alert.create()
        aDialog.show()
    }

    private fun requestLocationPermission() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Location Permission Required")
        alert.setMessage("Starting from Android M (6.0), the system requires apps to be granted, location access in order to scan for BLE devices.")
        alert.setCancelable(false)
        alert.setPositiveButton(
            "OK"
        ) { dialog, _ ->
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            dialog.cancel()
        }
        val aDialog = alert.create()
        aDialog.show()
    }
}