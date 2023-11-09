package com.bosch.blelibrary

import java.util.UUID

object ThingyConstants {

    val THINGY_BASE_UUID = UUID(-0x1097feff64cab6cdL, -0x64efad00568bffbeL)
    val DEVICE_NAME_CHARACTERISTIC_UUID = lazy {UUID(-0x1097fefe64cab6cdL, -0x64efad00568bffbeL)  }

    val BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
    val BATTERY_SERVICE_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")
}