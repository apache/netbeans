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
package org.netbeans.modules.java.editor.codegen;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Action which inserts an appropriate character at line-end without moving
 * the caret.
 *
 * @author Tim Boudreau
 */
public final class InsertSemicolonAction extends BaseAction {
    private final boolean withNewline;
    private final char what;
    
    protected InsertSemicolonAction (String name, char what, boolean withNewline) {
        super(name);
        this.withNewline = withNewline;
        this.what = what;
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(InsertSemicolonAction.class, name));
    }

    public InsertSemicolonAction(String name, boolean withNewline) {
        this (name, ';', withNewline); //NOI18N
    }
    
    public InsertSemicolonAction(boolean withNewLine) {
        this (withNewLine ? "complete-line-newline" : "complete-line", ';', withNewLine); //NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (!target.isEditable() || !target.isEnabled()) {
            target.getToolkit().beep();
            return;
        }
        final BaseDocument doc = (BaseDocument) target.getDocument();
        final Indent indenter = Indent.get(doc);
        final class R implements Runnable {
            public @Override void run() {
                try {
                    Caret caret = target.getCaret();
                    int dotpos = caret.getDot();
                    int eoloffset = Utilities.getRowEnd(target, dotpos);
                    doc.insertString(eoloffset, "" + what, null); //NOI18N
                    if (withNewline) {
                        //This is code from the editor module, but it is
                        //a pretty strange way to do this:
                        doc.insertString(dotpos, "-", null); //NOI18N
                        doc.remove(dotpos, 1);
                        int eolDot = Utilities.getRowEnd(target, caret.getDot());
                        int newDotPos = indenter.indentNewLine(eolDot);
                        caret.setDot(newDotPos);
                    }
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
