package org.black.kotlin.diagnostics.netbeans.indentation;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Александр
 */
public class KotlinIndentTask implements IndentTask {

    private Context context;

    KotlinIndentTask(Context context) {
        this.context = context;
    }

    @Override
    public void reindent() throws BadLocationException {
    //TODO make indentation here
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

}
