/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;
import org.openide.util.Exceptions;

/**
 *
 * @author Roman Svitanic
 */
public class IndentationCounter {

    private final BaseDocument doc;

    public IndentationCounter(BaseDocument doc) {
        this.doc = doc;
    }

    public Indentation count(int caretOffset) {
        Indentation result = Indentation.NONE;
        doc.readLock();
        try {
            result = countUnderReadLock(caretOffset);
        } finally {
            doc.readUnlock();
        }
        return result;
    }

    private Indentation countUnderReadLock(int caretOffset) {
        int newIndent = 0;
        try {
            final TokenSequence<JadeTokenId> ts = TokenHierarchy.get(doc).tokenSequence(JadeTokenId.jadeLanguage());
            final int caretLineStart = LineDocumentUtils.getLineStart(doc, LineDocumentUtils.getLineStart(doc, caretOffset) - 1);
            if (ts != null) {
                ts.move(caretOffset);
                ts.moveNext();
                if (ts.token() == null) {
                    return Indentation.NONE;
                }
                while (ts.token().id() == JadeTokenId.EOL || ts.token().id() == JadeTokenId.WHITESPACE) {
                    // find the last token on the current line before EOL or WS
                    if (!ts.movePrevious()) {
                        break;
                    }
                }
                if (ts.token().id() == JadeTokenId.COMMENT || ts.token().id() == JadeTokenId.UNBUFFERED_COMMENT) {
                    final int firstNonWsIndex = LineDocumentUtils.getLineFirstNonWhitespace(doc, LineDocumentUtils.getLineStart(doc, caretOffset) - 1);
                    ts.move(firstNonWsIndex);
                    ts.moveNext();
                    if (ts.token() != null
                            && (ts.token().id() == JadeTokenId.COMMENT_DELIMITER
                            || ts.token().id() == JadeTokenId.UNBUFFERED_COMMENT_DELIMITER)) {
                        return new IndentationImpl(Utilities.getRowIndent(doc, caretLineStart) + 1);
                    }
                }
                newIndent = Utilities.getRowIndent(doc, caretLineStart);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (newIndent < 0) {
            newIndent = 0;
        }
        return new IndentationImpl(newIndent);
    }

    public interface Indentation {

        Indentation NONE = new Indentation() {

            @Override
            public int getIndentation() {
                return 0;
            }

            @Override
            public void modify(Context context) {
            }

        };

        int getIndentation();

        void modify(Context context);

    }

    private static final class IndentationImpl implements Indentation {

        private final int indentation;

        public IndentationImpl(int indentation) {
            this.indentation = indentation;
        }

        @Override
        public int getIndentation() {
            return indentation;
        }

        @Override
        public void modify(final Context context) {
            assert context != null;
            context.document().render(new Runnable() {

                @Override
                public void run() {
                    modifyUnderWriteLock(context);
                }
            });
        }

        private void modifyUnderWriteLock(Context context) {
            try {
                context.modifyIndent(
                        LineDocumentUtils.getLineStart((BaseDocument) context.document(), context.caretOffset()),
                        indentation);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }
}
