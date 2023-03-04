/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Inspired by org.netbeans.modules.php.editor.actions.InsertSemicolonAction
 *
 * @author Petr Pisl
 */
public abstract class InsertSemicolonAction extends BaseAction {

    public static enum NewLineProcessor {
        WITH_NEW_LINE {
            @Override
            public void processNewLine(final int endOfLineOffset, final Caret caret, final Indent indenter) throws BadLocationException {
                int newCaretPosition = indenter.indentNewLine(endOfLineOffset + SEMICOLON.length());
                caret.setDot(newCaretPosition);
            }
        },
        WITHOUT_NEW_LINE {
            @Override
            public void processNewLine(final int endOfLineOffset, final Caret caret, final Indent indenter) throws BadLocationException {
            }
        };

        public abstract void processNewLine(int endOfLineOffset, Caret caret, Indent indenter) throws BadLocationException;
    }

    private static final String SEMICOLON = ";"; //NOI18N
    private final NewLineProcessor newLineProcessor;

    protected InsertSemicolonAction(NewLineProcessor newLineProcessor) {
        this.newLineProcessor = newLineProcessor;
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target.isEditable() && target.isEnabled()) {
            final BaseDocument doc = (BaseDocument) target.getDocument();
            final Indent indenter = Indent.get(doc);
            final class R implements Runnable {

                @Override
                public void run() {
                    try {
                        Caret caret = target.getCaret();
                        int caretPosition = caret.getDot();
                        int eolOffset = Utilities.getRowEnd(target, caretPosition);
                        doc.insertString(eolOffset, SEMICOLON, null);
                        newLineProcessor.processNewLine(eolOffset, caret, indenter);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            indenter.lock();
            try {
                doc.runAtomicAsUser(new R());
            } finally {
                indenter.unlock();
            }
        }
    }

    @NbBundle.Messages("complete-line-newline=Complete Line and Create New Line")
    @EditorActionRegistration(
        name = CompleteLineNewLine.ACTION_NAME,
        mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE,
        shortDescription="#complete-line-newline"
    )
    public static class CompleteLineNewLine extends InsertSemicolonAction {

        static final String ACTION_NAME = "complete-line-newline"; //NOI18N

        public CompleteLineNewLine() {
            super(NewLineProcessor.WITH_NEW_LINE);
        }
    }

    @NbBundle.Messages("complete-line=Complete Line")
    @EditorActionRegistration(
        name = CompleteLine.ACTION_NAME,
        mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE,
        shortDescription="#complete-line"
    )
    public static class CompleteLine extends InsertSemicolonAction {

        static final String ACTION_NAME = "complete-line"; //NOI18N

        public CompleteLine() {
            super(NewLineProcessor.WITHOUT_NEW_LINE);
        }
    }
}
