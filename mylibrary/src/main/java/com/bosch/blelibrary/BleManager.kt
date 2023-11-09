package com.bosch.blelibrary

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.UUID

class BleManager(
    private val context: Context,
    result: (
        scanResult: ScanResult?
    ) -> Unit,
    vararg val serviceCharacteristicsMap: ServiceCharacteristicsMap<UUID, UUID> = arrayOf(),
    val properties: (value: Any)-> Unit
) : IBleManager {

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var requestedCharacteristic: ArrayList<BluetoothGattCharacteristic> = ArrayList()

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt?.discoverServices()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                requestedCharacteristic.clear()
                if (serviceCharacteristicsMap.isNotEmpty()) {
                    serviceCharacteristicsMap.forEach { map ->
                        val service: BluetoothGattService? = gatt?.getService(map.service)
                        map.characteristic.forEach { characteristic ->
                            service?.getCharacteristic(characteristic)
                                ?.let { requestedCharacteristic.add(it) }
                        }

                    }
                }
                readRequest(gatt)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic?.uuid) {
                    ThingyConstants.BATTERY_SERVICE_CHARACTERISTIC -> {
                        val mBatteryLevel =
                            characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                        if (mBatteryLevel != null) {
                            properties(mBatteryLevel)
                        }
                        Log.d("Battery Value", "onCharacteristicRead: $mBatteryLevel")
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
        }

    }

    @SuppressLint("MissingPermission")
    private fun readRequest(gatt: BluetoothGatt?) {
        gatt?.readCharacteristic(requestedCharacteristic[requestedCharacteristic.size - 1])
    }

    override fun initialize() {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun getBleAdapter(): BluetoothAdapter {
        return bluetoothAdapter ?: throw NullPointerException("Ble Adapter is not initialized")
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }

    override fun isBlePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    override fun scanLeDevices(filterUuId: UUID) {
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        if (isBlePermissionGranted()) {
            val settings: ScanSettings = ScanSettings.Builder()
                //.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000)
                .build()
            val filters: MutableList<ScanFilter> = ArrayList()
            filters.add(
                ScanFilter.Builder().setServiceUuid(ParcelUuid(filterUuId)).build()
            )
            bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        }

    }

    @SuppressLint("MissingPermission")
    fun stopLeDevices() {
        if (isBlePermissionGranted()) {
            bluetoothLeScanner?.stopScan(scanCallback)
        }

    }

    @SuppressLint("MissingPermission")
    override fun connect(scanResult: ScanResult) {
        bluetoothGatt = scanResult.device.connectGatt(context, false, bluetoothGattCallback)
    }
}