package org.black.kotlin.diagnostics.netbeans.report.error;

import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Александр
 */
public class KotlinSyntaxErrorHighlightingTask extends ParserResultTask{
    @Override
    public void run (Result result, SchedulerEvent event) {
    }

    @Override
    public int getPriority () {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass () {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel () {
    }
    
}
