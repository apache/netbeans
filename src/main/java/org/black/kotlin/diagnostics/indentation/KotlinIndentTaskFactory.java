package org.black.kotlin.diagnostics.indentation;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 *
 * @author Александр
 */

@MimeRegistration(mimeType="text/x-kt",service=IndentTask.Factory.class)
public class KotlinIndentTaskFactory implements IndentTask.Factory {

    @Override
    public IndentTask createTask(Context context) {
        return new KotlinIndentTask(context);
    }

}
