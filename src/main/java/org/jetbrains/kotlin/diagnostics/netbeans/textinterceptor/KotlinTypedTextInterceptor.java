/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.diagnostics.netbeans.textinterceptor;

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
                checkNextChar(context, ')');
                break;
            }
            case '{': {
                context.setText("{}", 1);
                break;
            }
            case '}': {
                checkNextChar(context, '}');
                break;
            }
            case '[':
                context.setText("[]", 1);
                break;
            case ']':
                checkNextChar(context, ']');
                break;
            case '"':
                context.setText("\"\"", 1);
                break;
            case '\'':
                context.setText("''", 1);
                break;
        }
    }

    private void checkNextChar(MutableContext context, char character) throws BadLocationException{
        char nextChar = context.getDocument().getText(context.getOffset(), 1).charAt(0);
        if (nextChar == character) {
            context.getDocument().remove(context.getOffset(), 1);
        }
    }
    
    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }
    
}
