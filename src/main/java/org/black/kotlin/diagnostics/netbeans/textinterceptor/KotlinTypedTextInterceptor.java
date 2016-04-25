package org.black.kotlin.diagnostics.netbeans.textinterceptor;

import javax.swing.text.BadLocationException;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Александр
 */
public class KotlinTypedTextInterceptor implements TypedTextInterceptor{

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        char character = context.getText().charAt(0);
        switch (character) {
            case '(': {
                context.setText("()", 1);
                break;
            }
            case ')': {
                char nextChar = context.getDocument().getText(context.getOffset(), 1).charAt(0);
                if (nextChar == ')') {
                    context.getDocument().remove(context.getOffset(), 1);
                }
                break;
            }
            case '{': {
                context.setText("{}", 1);
                break;
            }
            case '}': {
                char nextChar = context.getDocument().getText(context.getOffset(), 1).charAt(0);
                if (nextChar == '}') {
                    context.getDocument().remove(context.getOffset(), 1);
                }
                break;
            }
            case '"':
                context.setText("\"\"", 1);
                break;
            case '\'':
                context.setText("''", 1);
                break;
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }
    
}
