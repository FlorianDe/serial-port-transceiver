import de.florian.view.MainFrame
import javax.swing.SwingUtilities

class SerialPortTransceiverApplication {
    fun run() {
        SwingUtilities.invokeLater { MainFrame() }
    }
}

fun main() {
    SerialPortTransceiverApplication().run()
}
