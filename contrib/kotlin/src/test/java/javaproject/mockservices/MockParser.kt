package javaproject.mockservices

import javax.swing.event.ChangeListener
import org.netbeans.modules.parsing.api.Snapshot
import org.netbeans.modules.parsing.api.Task
import org.netbeans.modules.parsing.spi.ParseException
import org.netbeans.modules.parsing.spi.Parser
import org.netbeans.modules.parsing.spi.SourceModificationEvent

class MockParser : Parser() {
    override fun parse(snapshot: Snapshot?, task: Task?, event: SourceModificationEvent?) {}

    override fun getResult(task: Task?) = object : Result(null) {
        override fun invalidate() {}
    }
    
    override fun addChangeListener(changeListener: ChangeListener?) {
    }

    override fun removeChangeListener(changeListener: ChangeListener?) {
    }
}