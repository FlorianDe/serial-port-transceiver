package de.florian.service

import com.fazecast.jSerialComm.SerialPort

class SerialPortService {
    /**
     * Maybe check how PlatformIO does auto detect ports
     * https://github.com/platformio/platformio-core/blob/develop/platformio/builder/tools/pioupload.py#L98-L171
     */
    fun getCh340UsbPorts() = getCommPorts {
        it.baudRate == 9600 &&
                it.portDescription.contains("USB") &&
                it.descriptivePortName.contains("CH340")
    }

    private fun getCommPorts(predicate: (SerialPort) -> Boolean = { true }) = SerialPort.getCommPorts()
            .asSequence()
            .filter(predicate)
            .toList()
            .also { it.forEachIndexed { idx, port -> println("($idx): ${port.portDescription} -> ${port.descriptivePortName} [${if (port.isOpen) "open" else "closed"}]") } }
}