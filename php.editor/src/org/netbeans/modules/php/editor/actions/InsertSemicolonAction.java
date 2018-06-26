/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Inspired by org.netbeans.modules.java.editor.codegen.InsertSemicolonAction.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
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
                        int eolOffset = LineDocumentUtils.getLineEnd(doc, caretPosition);
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
        mimeType = FileUtils.PHP_MIME_TYPE,
        shortDescription = "#complete-line-newline"
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
        mimeType = FileUtils.PHP_MIME_TYPE,
        shortDescription = "#complete-line"
    )
    public static class CompleteLine extends InsertSemicolonAction {

        static final String ACTION_NAME = "complete-line"; //NOI18N

        public CompleteLine() {
            super(NewLineProcessor.WITHOUT_NEW_LINE);
        }
    }
}
