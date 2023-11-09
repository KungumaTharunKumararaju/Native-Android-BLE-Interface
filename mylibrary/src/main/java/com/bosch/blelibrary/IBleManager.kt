package com.bosch.blelibrary

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import java.util.UUID

interface IBleManager {

    fun initialize()

    fun getBleAdapter(): BluetoothAdapter

    fun isBluetoothEnabled(): Boolean

    fun isBlePermissionGranted(): Boolean

    fun isLocationPermissionGranted(): Boolean

    fun scanLeDevices(filterUuId: UUID)

    fun connect(scanResult: ScanResult)
}