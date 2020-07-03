import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import de.florian.service.SerialPortService
import java.util.*

class Application {
    private val scanner = Scanner(System.`in`)

    fun run() {

        val portService = SerialPortService()
        val commPorts = portService.getCh340UsbPorts()
        val comPort: SerialPort = if (commPorts.size > 1) {
            val idx = scanner.nextInt()
            commPorts[idx]
        } else {
            commPorts[0]
        }

        comPort.openPort().also { println("Connection to ${comPort.descriptivePortName} established: $it") }
        comPort.addDataListener(object : SerialPortDataListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED
            }

            override fun serialEvent(event: SerialPortEvent) {
                //if (event.eventType != SerialPort.LISTENING_EVENT_DATA_RECEIVED) return
                val newData = event.receivedData
                println("Received data of size: " + newData.size)
                for (i in newData.indices) print(newData[i].toChar())
                println("\n")
            }
        })

        comPort.addDataListener(object : SerialPortDataListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_DATA_WRITTEN
            }

            override fun serialEvent(event: SerialPortEvent) {
                if (event.eventType == SerialPort.LISTENING_EVENT_DATA_WRITTEN)
                    println("All bytes were successfully transmitted!");
            }
        })

        while (true) {
            val command = scanner.nextLine().toByteArray()
            comPort.writeBytes(command, command.size.toLong())
        }
    }
}

fun main() {
    Application().run()
}
