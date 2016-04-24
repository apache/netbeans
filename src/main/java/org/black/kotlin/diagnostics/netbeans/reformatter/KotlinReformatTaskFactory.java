package org.black.kotlin.diagnostics.netbeans.reformatter;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ReformatTask;

@MimeRegistration(mimeType="text/x-kt",service=ReformatTask.Factory.class)
public class KotlinReformatTaskFactory implements ReformatTask.Factory {

    @Override
    public ReformatTask createTask(Context context) {
        return new KotlinReformatTask(context);
    }
    
}
