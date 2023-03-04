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
