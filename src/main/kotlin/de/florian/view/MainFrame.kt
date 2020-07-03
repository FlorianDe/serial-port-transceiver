package de.florian.view

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import de.florian.service.SerialPortService
import de.florian.view.component.ClosableDefaultViewTabbedPane
import de.florian.view.component.table.DtoTableModel
import de.florian.view.component.table.TableColumnAdjuster
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.system.exitProcess


class MainFrame : JFrame() {
    private val serialPortService = SerialPortService()
    private val transceiverPanel: ClosableDefaultViewTabbedPane

    init {
        LafManager.install(DarculaTheme())

        this.layout = BorderLayout()
        this.title = "COM Transceiver"

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                //TODO Remove locks later on
                exitProcess(0)
            }
        })

        val sourcesWrapper = JPanel().apply {
            layout = GridLayout()

            val noTabsOpenPlaceholderView: JComponent = JLabel("Double click a port in the list to open a new tab.", SwingConstants.CENTER).apply {
                background = Color.RED
            }
            transceiverPanel = ClosableDefaultViewTabbedPane(noTabsOpenPlaceholderView);

            val data = serialPortService.getCh340UsbPorts()
            val tableModel = DtoTableModel(ArrayList(data)).apply {
                addColumn("Port Description", String::class.java, { it.portDescription })
                addColumn("Port Name", String::class.java, { it.descriptivePortName })
                addColumn("Baud Rate", Int::class.java, { it.baudRate })
                addColumn("System Port Name", String::class.java, { it.systemPortName })
                addColumn("Open", String::class.java, { it.isOpen.toString() })
            }

            val table = JTable(tableModel).apply {
                autoResizeMode = JTable.AUTO_RESIZE_OFF
                autoCreateRowSorter = true
                TableColumnAdjuster(this).apply {
                    setDynamicAdjustment(true)
                    adjustColumns()
                }

                addMouseListener(object : MouseAdapter() {
                    override fun mousePressed(mouseEvent: MouseEvent) {
                        val table = mouseEvent.source as JTable
                        val row = table.rowAtPoint(mouseEvent.point)
                        if (mouseEvent.clickCount == 2 && row != -1) {
                            val comPort = tableModel.getValue(table.rowSorter.convertRowIndexToModel(row))
                            if (!comPort.isOpen && comPort.openPort()) {
                                println("Open port $comPort")
                                val textArea = JTextArea()
                                val commandField = JTextField().apply { isEnabled = false }
                                val paneContent = JPanel().apply {
                                    layout = BorderLayout()
                                    add(commandField, BorderLayout.SOUTH)
                                    add(JScrollPane(textArea), BorderLayout.CENTER)
                                }

                                transceiverPanel.addTab(comPort.systemPortName, paneContent) {
                                    println("Closed port $comPort")
                                    comPort.closePort()
                                    tableModel.fireTableRowsUpdated(row, row);
                                }

                                commandField.isEnabled = true
                                commandField.addActionListener {
                                    val command = commandField.text.toByteArray()
                                    comPort.writeBytes(command, command.size.toLong())
                                    textArea.append("\n>>> ${commandField.text}")
                                    commandField.text = ""
                                }

                                comPort.addDataListener(object : SerialPortDataListener {
                                    override fun getListeningEvents(): Int {
                                        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
                                    }

                                    override fun serialEvent(event: SerialPortEvent) {
                                        if (event.eventType != SerialPort.LISTENING_EVENT_DATA_RECEIVED) return
                                        val newData = event.receivedData
                                        val sb = StringBuilder("\n<<< ")
                                        for (i in newData.indices) {
                                            sb.append(newData[i].toChar().toString())
                                        }
                                        textArea.append(sb.toString())
                                    }
                                })

                                comPort.addDataListener(object : SerialPortDataListener {
                                    override fun getListeningEvents(): Int {
                                        return SerialPort.LISTENING_EVENT_DATA_WRITTEN
                                    }

                                    override fun serialEvent(event: SerialPortEvent) {
                                        if (event.eventType == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
                                            textArea.append("All bytes were successfully transmitted!")
                                        }
                                    }
                                })
                            } else {
                                System.err.println("Couldn't open COM port!")
                            }
                        }
                    }
                })
            }

            this.add(JScrollPane(table))
        }

        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcesWrapper, transceiverPanel)
        this.add(splitPane, BorderLayout.CENTER)

        this.preferredSize = Dimension(600, 600)
        this.pack()
        this.setLocationRelativeTo(null)
        this.isVisible = true
    }
}