package org.black.kotlin.diagnostics.netbeans.report.error;

import java.util.Collection;
import java.util.Collections;
import org.black.kotlin.model.KotlinEnvironment;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Александр
 */
@MimeRegistration(mimeType="text/x-kt",service=TaskFactory.class)
public class KotlinSyntaxErrorHighlightingTaskFactory extends TaskFactory {

    @Override
    public Collection<? extends SchedulerTask> create (Snapshot snapshot) {    
        return Collections.singleton (new KotlinSyntaxErrorHighlightingTask());
    }

}
