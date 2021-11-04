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
package org.netbeans.modules.html.editor.completion;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.spi.palette.PaletteController;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * CompletionProvider implementation offering all palette items on non-prefix completion request.
 *
 * @author mfukala@netbeans.org
 */
public class HtmlPaletteCompletionProvider implements CompletionProvider {

    public HtmlPaletteCompletionProvider() {
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (HtmlPreferences.isPaletteCompletionEnabled() // NETBEANS-5970
                && (queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            return new AsyncCompletionTask(new CCQuery(),
                    component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    static final class CCQuery extends AsyncCompletionQuery {

        private int completionExpressionStartOffset = -1;
        private JTextComponent component;
        private Collection<PaletteCompletionItem> items;

        @Override
        protected void query(final CompletionResultSet resultSet, final Document doc, final int offset) {
            try {
                doc.render(new Runnable() {

                    @Override
                    public void run() {
                        Collection<PaletteCompletionItem> newItems = new ArrayList<>();

                        TokenSequence htmlTs = getTokenSequence(doc, offset);

                        if (htmlTs == null) { //no html code
                            return;
                        }

                        int diff = htmlTs.move(offset);
                        if (!htmlTs.moveNext()) {
                            return;
                        }

                        Token current = htmlTs.token();
                        if (current.id() != HTMLTokenId.TEXT) { //works only in plain text
                            return;
                        }

                        //end tag autocompletion workaround - we do not want to see the palette items when user finished
                        //an open tag and the end tag autocompletion pops up
                        //or after the entity reference
                        if (diff == 0 && htmlTs.movePrevious()) {
                            TokenId id = htmlTs.token().id();
                            if (id == HTMLTokenId.TAG_CLOSE_SYMBOL ||
                                    id == HTMLTokenId.TAG_OPEN_SYMBOL ||
                                    id == HTMLTokenId.CHARACTER) {
                                return;
                            }
                        }

                        String prefix = current.text().subSequence(0, diff).toString();
                        //preserve only non-ws part of the prefix at the end (text token can contain mix of ws and non-ws chars)
                        int i;
                        for (i = prefix.length() - 1; i >= 0; i--) {
                            char ch = prefix.charAt(i);
                            if (Character.isWhitespace(ch)) {
                                i++;
                                break;
                            }
                        }
                        if (i > 0) {
                            prefix = prefix.substring(i, prefix.length());
                        }

                        completionExpressionStartOffset = offset - prefix.length();

                        TopComponent tc = NbEditorUtilities.getTopComponent(component);
                        if (tc == null) {
                            return;
                        }

                        PaletteController pc = tc.getLookup().lookup(PaletteController.class);
                        if(pc == null) {
                            //try to get the PaletteController from mime lookup
                            FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
                            String mimeType = file.getMIMEType();
                            pc = MimeLookup.getLookup(mimeType).lookup(PaletteController.class);
                        }
                        
                        if (pc != null) {
                            Node rootNode = pc.getRoot().lookup(Node.class);
                            Children children = rootNode.getChildren();
                            for (Node categoryNode : children.getNodes()) {
                                for (Node itemNode : categoryNode.getChildren().getNodes()) {
                                    Action insertAction = itemNode.getPreferredAction();
                                    String itemName = itemNode.getDisplayName();
                                    if (startsWithIgnoreCase(itemName, prefix)) {
                                        newItems.add(new PaletteCompletionItem(insertAction, completionExpressionStartOffset, categoryNode.getDisplayName(), itemName, itemNode.getIcon(BeanInfo.ICON_COLOR_16x16)));
                                    }
                                }
                            }
                        }
                        resultSet.addAllItems(newItems);
                        items = newItems;
                    }
                });


            } finally {
                resultSet.finish();
            }
        }
        
        @Override
        protected boolean canFilter(final JTextComponent component) {
            final Collection<PaletteCompletionItem> currentItems = items;
            if(currentItems == null) {
                return false;
            }
            final Document doc = component.getDocument();
            final AtomicBoolean retval = new AtomicBoolean();
            doc.render(new Runnable() {

                @Override
                public void run() {
                    try {
                        int offset = component.getCaretPosition();
                        if (completionExpressionStartOffset < 0  || offset < completionExpressionStartOffset) {
                            retval.set(false);
                            return;
                        }
                        String prefix = doc.getText(completionExpressionStartOffset, offset - completionExpressionStartOffset);
                        //check the items
                        for (PaletteCompletionItem item : currentItems) {
                            if (startsWithIgnoreCase(item.getItemName(), prefix)) {
                                retval.set(true); //at least one item will remain
                                return;
                            }
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });


            return retval.get();

        }

        @Override
        protected void filter(final CompletionResultSet resultSet) {
            try {
                final Collection<PaletteCompletionItem> currentItems = items;
                if(currentItems == null) {
                    return ; //the "items" has changed since canFilter() was called (and returned true)
                }
                final Document doc = component.getDocument();
                doc.render(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int offset = component.getCaretPosition();
                            String prefix = doc.getText(completionExpressionStartOffset, offset - completionExpressionStartOffset);
                            //check the items
                            for (PaletteCompletionItem item : currentItems) {
                                if (startsWithIgnoreCase(item.getItemName(), prefix)) {
                                    resultSet.addItem(item);
                                }
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

            } finally {
                resultSet.finish();
            }

        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        private TokenSequence getTokenSequence(Document doc, int offset) {
            TokenHierarchy<Document> th = TokenHierarchy.get(doc);
            List<TokenSequence<?>> sequences = th.embeddedTokenSequences(offset, true);
            TokenSequence htmlTs = null;
            for (TokenSequence ts : sequences) {
                if (ts.language().mimeType().equals("text/html")) { //NOI18N
                    htmlTs = ts;
                    break;
                }
            }
            return htmlTs;
        }
    }

    private static boolean startsWithIgnoreCase(String text, String prefix) {
        return text.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }

    private static class PaletteCompletionItem implements CompletionItem {

        protected Action action;
        protected String category;
        protected String item;
        protected Image icon;
        protected int completionExpressionStartOffset;

        public PaletteCompletionItem(Action action, int completionExpressionStartOffset, String category, String item, Image icon) {
            this.action = action;
            this.category = category;
            this.item = item;
            this.icon = icon;
            this.completionExpressionStartOffset = completionExpressionStartOffset;
        }

        public String getItemName() {
            return item;
        }

        public String getLeftHtmlText() {
            return getItemName();
        }

        public String getRightHtmlText() {
            return "<font color='" + HtmlCompletionItem.hexColorCode(Color.GRAY) + "'>" + category + "</font>"; //NOI18N
        }

        public ImageIcon getIcon() {
            return new ImageIcon(icon);
        }

        @Override
        public void defaultAction(JTextComponent component) {
            try {
                //first remove the typed prefix
                Document doc = component.getDocument();
                int currentCaretPosition = component.getCaretPosition();
                doc.remove(completionExpressionStartOffset, currentCaretPosition - completionExpressionStartOffset);

                Completion.get().hideAll();

                action.actionPerformed(null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
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
            return 1;
        }

        @Override
        public CharSequence getSortText() {
            return category + item;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return getSortText();
        }
    }
}
