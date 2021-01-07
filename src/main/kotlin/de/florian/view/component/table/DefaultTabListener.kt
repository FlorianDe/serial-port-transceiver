package de.florian.view.component.table;

import com.github.weisj.darklaf.components.TabEvent
import com.github.weisj.darklaf.components.TabListener

open class DefaultTabListener : TabListener {
    override fun tabOpened(e: TabEvent?) {}

    override fun tabClosing(e: TabEvent?) {}

    override fun tabClosed(e: TabEvent?) {}
}
