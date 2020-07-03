package de.florian.view.component.table

import java.util.*
import javax.swing.table.AbstractTableModel

class DtoTableModel<T>(private val rows: ArrayList<T> = ArrayList()) : AbstractTableModel() {
    private val columns: ArrayList<ColumnProvider<T, out Any>> = ArrayList()

    fun getValue(rowIndex: Int) = rows[rowIndex]

    override fun getRowCount(): Int {
        return rows.size
    }

    override fun getColumnCount(): Int {
        return columns.size
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return columns[columnIndex].getValue(rows[rowIndex])
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return Objects.nonNull(columns[columnIndex].setValue)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        val setVal = columns[columnIndex].setValue as ((T, Any?) -> Unit)?
        setVal?.let { set ->
            set(rows[rowIndex], aValue)
            fireTableCellUpdated(rowIndex, columnIndex)
        }
    }

    override fun getColumnName(column: Int): String {
        return columns[column].title
    }

    fun <V> addColumn(title: String, valueClass: Class<V>, getValue: (T) -> V, setValue: ((T, V?) -> Unit)? = null) {
        addColumn(object : ColumnProvider<T, V>(title, valueClass, getValue, setValue) {})
    }

    fun addColumn(column: ColumnProvider<T, *>) {
        columns.add(column as ColumnProvider<T, out Any>)
        fireTableStructureChanged()
    }

    fun addRow(row: T) {
        rows.add(row)
        fireTableRowsInserted(rows.size - 1, rows.size - 1)
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return columns[columnIndex].valueClass
    }

    abstract class ColumnProvider<T, V> protected constructor(
            var title: String,
            val valueClass: Class<V>,
            val getValue: (T) -> V,
            val setValue: ((T, V?) -> Unit)? = null
    )
}