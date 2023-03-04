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
package org.netbeans.modules.jshell.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import jdk.jshell.Snippet;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.support.ShellHistory;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
// must be registered for java also, to give completions on the 1st line
@MimeRegistrations({
    @MimeRegistration(mimeType="text/x-repl", service=CompletionProvider.class, position = 100),
    @MimeRegistration(mimeType="text/x-java", service=CompletionProvider.class, position = 120)
})
public class HistoryCompletionProvider implements CompletionProvider {

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    static int isFirstJavaLine(JTextComponent component) {
        ShellSession s = ShellSession.get(component.getDocument());
        if (s == null) {
            return -1;
        }
        ConsoleSection sec = s.getModel().getInputSection();
        if (sec == null) {
            return -1;
        }
        LineDocument ld = LineDocumentUtils.as(component.getDocument(), LineDocument.class);
        if (ld == null) {
            return -1;
        }

        int off = sec.getStart();
        int caret = component.getCaretPosition();
        int s1 = LineDocumentUtils.getLineStart(ld, caret);
        int s2 = LineDocumentUtils.getLineStart(ld, off);
        try {
            return s1 == s2 ?
                    component.getDocument().getText(sec.getPartBegin(), sec.getPartLen()).trim().length() 
                    : -1;
        } catch (BadLocationException ex) {
            return 0;
        }
    }
    
    static ShellSession checkInputSection(JTextComponent component) {
        Document doc = component.getDocument();
        ShellSession session = ShellSession.get(doc);
        if (session == null) {
            return null;
        }
        ConsoleModel model = session.getModel();
        if (model == null) {
            return null;
        }
        ConsoleSection is = model.getInputSection();
        if (is == null) {
            return null;
        }
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return null;
        }

        int caret = component.getCaretPosition();
        int lineStart = is.getPartBegin();
        try {
            int lineEnd = LineDocumentUtils.getLineEnd(ld, caret);
            if (caret < lineStart || caret > lineEnd) {
                return null;
            }
        } catch (BadLocationException ex) {
            return null;
        }
        return session;
    }
    
    @Override
    public CompletionTask createTask(int queryType, final JTextComponent component) {
        int a = isFirstJavaLine(component);
        if (queryType != COMPLETION_ALL_QUERY_TYPE) {
            if (queryType != COMPLETION_QUERY_TYPE || a != 0) {
                return null;
            }
        }
        // check that the caret is at the first line of the editable area:
        ShellSession session = checkInputSection(component);
        if (session == null) {
            return null;
        }
        return new AsyncCompletionTask(new T(
            session,
            session.getModel(),
            session.getModel().getInputSection()
        ), component);
    }
    
    private static class T extends AsyncCompletionQuery {
        private ConsoleContents contents;
        private final ShellSession  session;
        private final ConsoleModel model;
        private final ConsoleSection input;
        private int counter = 1;
        
        public T(ShellSession session, ConsoleModel model, ConsoleSection input) {
            this.session = session;
            this.model = model;
            this.input = input;
        }
        
        private CompletionItem createHistoryItem(ShellHistory.Item item) {
            return createCompletionItem(item, true);
        }
        
        private CompletionItem createCurrentItem(ShellHistory.Item item) {
            return createCompletionItem(item, false);
        }
        
        private CompletionItem createCompletionItem(ShellHistory.Item item, boolean saved) {
            return new ItemImpl(saved, counter++, item.getKind(), item.isShellCommand(), item.getContents());
        }
        
        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (model.getDocument() != doc) {
                resultSet.finish();
                return;
            }
            int b = input.getPartBegin();
            if (caretOffset < b) {
                resultSet.finish();
                return;
            }
            String prefix = "";
            try {
                prefix = doc.getText(b, (caretOffset - b));
            } catch (BadLocationException ex) {
            }
            final String fPrefix = prefix;
            ShellHistory  h = session.getEnv().getLookup().lookup(ShellHistory.class);
            resultSet.addAllItems(
                    session.historyItems().stream().
                            filter(i -> i.getContents().startsWith(fPrefix)).
                            map(this::createCurrentItem).
                            collect(Collectors.toList())
            );
            if (h != null) {
                final Set<String> commands = new HashSet<>();
                resultSet.addAllItems(
                        h.getHistory().stream().
                        filter(i -> 
                                i.getContents().startsWith(fPrefix) &&
                                commands.add(i.getContents())
                        ).
                        map(this::createHistoryItem).
                        collect(Collectors.toList()));
            }
            resultSet.finish();
        }
    }
    
    private static final int PRIORITY_SAVED = 2000;
    private static final int PRIORITY_CURRENT = 1000;
    
    
    @NbBundle.Messages({
        "# {0} - item number in the history",
        "History_ItemIndex_html=<b><i>#{0}</i></b>"
    })
    private static class ItemImpl implements CompletionItem {
        private final int index;
        private final String text;
        private final boolean saved;
        private final Snippet.Kind  kind;
        private final boolean       command;
        
        public ItemImpl(boolean saved, int index, Snippet.Kind kind, boolean command, String text) {
            this.index = index;
            this.text = text;
            this.saved = saved;
            this.command = command;
            this.kind = kind;
        }
        
        private String getLeftText() {
            return text;
        }
        
        private String getRightText() {
            return Bundle.History_ItemIndex_html(index);
        }
        
        @Override
        public void defaultAction(JTextComponent component) {
            if (component == null) {
                return;
            }
            int last = text.length() - 1;
            while (last > 0 && 
                   Character.isWhitespace(text.charAt(last))) {
                last--;
            }
            if (last < 0) {
                Completion.get().hideAll();
                return;
            }
            
            final Document d = component.getDocument();
            final ShellSession s = ShellSession.get(d);
            if (s == null) {
                Completion.get().hideAll();
                return;
            }
            final ConsoleModel mdl = s.getModel();
            ConsoleSection is = mdl.getInputSection();
            final int from = is.getPartBegin();
            final int l = last + 1;
            AtomicLockDocument ald = LineDocumentUtils.asRequired(d, AtomicLockDocument.class);
            ald.runAtomicAsUser(() -> {
                try {
                    d.remove(from, d.getLength() - from);
                    d.insertString(from, text.substring(0, l), null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            });
            Completion.get().hideAll();
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(
                    getLeftText(), 
                    getRightText(), g, defaultFont);
        }
        
        private ImageIcon getIcon() {
            String baseName;
            
            if (command) {
                baseName = "command"; // NOI18N
            } else {
                switch (kind) {
                    case VAR:
                    case EXPRESSION:
                    case IMPORT:
                    case METHOD:
                    case TYPE_DECL:
                    case STATEMENT:
                            baseName = kind.name().toLowerCase();
                        break;
                    default:
                        baseName = "item"; // NOI18N
                        break;
                }
            }
            
            return ImageUtilities.loadImageIcon(ICON_BASE + baseName + ".png", true);
        }
        
        private static final String ICON_BASE = "org/netbeans/modules/jshell/resources/history_"; // NOI18N

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            ImageIcon icon = getIcon();
            
            CompletionUtilities.renderHtml(
                    icon, 
                    getLeftText(), 
                    getRightText(), 
                    g, 
                    defaultFont, 
                    defaultColor, 
                    width, 
                    height, 
                    selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            // probably show the whole completion item
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        public int getSortPriority() {
            return saved ? PRIORITY_SAVED : PRIORITY_CURRENT;
        }

        @Override
        public CharSequence getSortText() {
            return Integer.toString(1000 - index);
        }

        @Override
        public CharSequence getInsertPrefix() {
            return "";
        }
        
    }
}
