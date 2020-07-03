package de.florian.view.component

import com.github.weisj.darklaf.components.ClosableTabbedPane
import com.github.weisj.darklaf.components.TabEvent
import de.florian.view.component.table.DefaultTabListener
import java.awt.CardLayout
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTabbedPane

class ClosableDefaultViewTabbedPane(
        defaultComponent: JComponent,
        private val tabbedPane: ClosableTabbedPane = ClosableTabbedPane(),
        val switchPredicate: ((JTabbedPane, TabEvent?) -> Boolean) = { pane, event -> event?.type == TabEvent.Type.TAB_CLOSED && pane.tabCount == 0 }
) : JPanel() {
    private val cardLayout = CardLayout()

    init {
        layout = cardLayout

        tabbedPane.addTabListener(object : DefaultTabListener() {
            override fun tabOpened(e: TabEvent?) = setViewByPredicateExpression(e)
            override fun tabClosed(e: TabEvent?) = setViewByPredicateExpression(e)
        })

        add(defaultComponent, 0)
        add(tabbedPane, 1)

        setViewByPredicateExpression(null)
    }

    private fun setViewByPredicateExpression(e: TabEvent?) {
        if (switchPredicate(tabbedPane, e)) cardLayout.first(this) else cardLayout.last(this)
    }

    fun addTab(title: String, component: Component, tabClosedListener: () -> Unit) {
        with(tabbedPane){
            addTab(title, component)
            addTabListener(object : DefaultTabListener() {
                override fun tabClosed(e: TabEvent?) {
                    if (component == e?.component) tabClosedListener()
                }
            })
        }
    }
}