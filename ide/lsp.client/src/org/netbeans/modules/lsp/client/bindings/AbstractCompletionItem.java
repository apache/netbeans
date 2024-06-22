/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lsp.client.bindings;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

abstract class AbstractCompletionItem<Edit> implements CompletionItem {
    private final Document doc;
    private final int caretOffset;
    private final String leftLabel;
    private final String rightLabel;
    private final ImageIcon icon;
    private final String sortText;
    private final String insert;

    AbstractCompletionItem(
        Document doc, int caretOffset, String leftLabel, String rightLabel,
        ImageIcon icon, String sortText, String insert
    ) {
        this.doc = doc;
        this.caretOffset = caretOffset;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        this.icon = icon;
        this.sortText = sortText;
        this.insert = insert;
    }

    abstract Edit findEdit();
    abstract int findStart(Document doc, Edit te);
    abstract int findEnd(Document doc, Edit te);
    abstract String findNewText(Edit te);

    abstract boolean isTextEdit(Edit te);

    abstract List<String> getCommitCharacters();
    abstract boolean isTriggerCharacter(String ch);
    abstract Supplier<String[]> resolveDocumentation(Document doc, int caretOffset);

    @Override
    public void defaultAction(JTextComponent jtc) {
        commit("");
    }

    private void commit(String appendText) {
        Edit te = findEdit();
        if (te == null) {
            //TODO: the NetBeans client does not current support InsertReplaceEdits, should not happen
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            return;
        }
        NbDocument.runAtomic((StyledDocument) doc, () -> {
            try {
                int endPos;
                if (isTextEdit(te)) {
                    int start = findStart(doc, te);
                    int end = findEnd(doc, te);
                    doc.remove(start, end - start);
                    String newText = findNewText(te);
                    doc.insertString(start, newText, null);
                    endPos = start + newText.length();
                } else {
                    String toAdd = insert;
                    int[] identSpan = Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);
                    if (identSpan != null) {
                        doc.remove(identSpan[0], identSpan[1] - identSpan[0]);
                        doc.insertString(identSpan[0], toAdd, null);
                        endPos = identSpan[0] + toAdd.length();
                    } else {
                        doc.insertString(caretOffset, toAdd, null);
                        endPos = caretOffset + toAdd.length();
                    }
                }
                doc.insertString(endPos, appendText, null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        Completion.get().hideDocumentation();
        Completion.get().hideCompletion();
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_TYPED) {
            String commitText = String.valueOf(ke.getKeyChar());
            List<String> commitCharacters = getCommitCharacters();
            if (commitCharacters != null && commitCharacters.contains(commitText)) {
                commit(commitText);
                ke.consume();
                if (isTriggerCharacter(commitText)) {
                    Completion.get().showCompletion();
                }
            }
        }
    }

    @Override
    public int getPreferredWidth(Graphics grphcs, Font font) {
        return CompletionUtilities.getPreferredWidth(leftLabel, rightLabel, grphcs, font);
    }

    @Override
    public void render(Graphics grphcs, Font font, Color color, Color color1, int i, int i1, boolean bln) {
        CompletionUtilities.renderHtml(icon, leftLabel, rightLabel, grphcs, font, color, i, i1, bln);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                Supplier<String[]> resolved = resolveDocumentation(doc, caretOffset);
                if (resolved != null) {
                    resultSet.setDocumentation(new CompletionDocumentation() {
                        @Override
                        public String getText() {
                            String[] both = resolved.get();
                            String detail = both[0];
                            String content = both[1];

                            StringBuilder documentation = new StringBuilder();
                            documentation.append("<html>\n");
                            if (detail != null) {
                                documentation.append("<b>").append(CompletionProviderImpl.escape(detail)).append("</b>");
                                documentation.append("\n<p>");
                            }
                            if (content != null) {
                                documentation.append(content);
                            }
                            return documentation.toString();
                        }

                        @Override
                        public URL getURL() {
                            return null;
                        }

                        @Override
                        public CompletionDocumentation resolveLink(String link) {
                            return null;
                        }

                        @Override
                        public Action getGotoSourceAction() {
                            return null;
                        }
                    });
                }
                resultSet.finish();
            }
        });
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 100;
    }

    @Override
    public CharSequence getSortText() {
        return sortText;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return insert;
    }
}
