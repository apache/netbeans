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

package org.netbeans.modules.gradle.execute;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author lkishalmi
 */
@MimeRegistration(mimeType = GradleCliEditorKit.MIME_TYPE, service = CompletionProvider.class)
public class GradleCliCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                String filter = null;
                int startOffset = caretOffset - 1;

                try {
                    final StyledDocument bDoc = (StyledDocument) doc;
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOffset = indexOfWhite(line);
                    filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
                    if (whiteOffset > 0) {
                        startOffset = lineStartOffset + whiteOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    Object prop = doc.getProperty(Document.StreamDescriptionProperty);
                    GradleCommandLine cli = new GradleCommandLine(doc.getText(0, doc.getLength()));
                    if (prop != null && prop instanceof GradleBaseProject) {
                        GradleBaseProject gbp = (GradleBaseProject) prop;
                        for (GradleTask task : gbp.getTasks()) {
                            if (!task.isPrivate() 
                                    && !cli.getTasks().contains(task.getName())
                                    && !cli.getExcludedTasks().contains(task.getName())
                                    && (task.getName().startsWith(filter) || task.matches(filter))) {
                                resultSet.addItem(new GradleCliCompletionItem(task, startOffset, caretOffset));
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    // Nothing to do.
                }
                resultSet.finish();
            }
        }, component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 1;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset) throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start
                ).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    private static class GradleCliCompletionItem implements CompletionItem {

        private final GradleTask task;
        private final int startOffset;
        private final int caretOffset;

        public GradleCliCompletionItem(GradleTask task, int startOffset, int caretOffset) {
            this.task = task;
            this.startOffset = startOffset;
            this.caretOffset = caretOffset;
        }

        @Override
        public void defaultAction(JTextComponent jtc) {
            try {
                Document doc = jtc.getDocument();
                doc.remove(startOffset, caretOffset - startOffset);
                doc.insertString(startOffset, task.getName(), null);
                //This statement will close the code completion box:
                Completion.get().hideAll();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font font) {
            return CompletionUtilities.getPreferredWidth(task.getName(), null, g, font);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(null, task.getName(), null, g, defaultFont, (selected ? Color.white : Color.BLACK), width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    resultSet.setDocumentation(new GradleTaskCompletionDocumentation());
                    resultSet.finish();
                }
            });
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        public int getSortPriority() {
            switch (task.getGroup()) {
                case "application": return 0;
                case "build": return 1;
                case "distribution": return 2;
                default: return 3;
            }
        }

        @Override
        public CharSequence getSortText() {
            return task.getName();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return task.getName();
        }

        private class GradleTaskCompletionDocumentation implements CompletionDocumentation {

            @Override
            public String getText() {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<b>Name:</b> ").append(task.getName()).append("<br/>");
                sb.append("<b>Group:</b> ").append(task.getGroup()).append("<br/>");
                sb.append("<b>Path:</b> ").append(task.getPath()).append("<br/>");
                sb.append("<b>Description:</b><p>").append(task.getDescription());
                return sb.toString();
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

        }
    }

}
