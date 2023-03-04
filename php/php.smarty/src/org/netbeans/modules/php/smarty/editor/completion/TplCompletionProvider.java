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
package org.netbeans.modules.php.smarty.editor.completion;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 * Implementation of {@link CompletionProvider} for Tpl documents.
 *
 * @author Martin Fousek
 */
@MimeRegistration(mimeType = "x-tpl", service = CompletionProvider.class, position = 1250)
public class TplCompletionProvider implements CompletionProvider {

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
//        Document doc = component.getDocument();
//        int dotPos = component.getCaret().getDot();
//        boolean openCC = checkOpenCompletion(doc, dotPos, typedText);
//        return openCC ? COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE : 0;
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        AsyncCompletionTask task = null;
        if ((queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            task = new AsyncCompletionTask(new Query(), component);
        }
        return task;
    }

    private static class Query extends AbstractQuery {

        private volatile Set<TplCompletionItem> items = new HashSet<>();
        private JTextComponent component;

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void doQuery(CompletionResultSet resultSet, final Document doc, final int caretOffset) {
            try {
                final TplCompletionQuery.CompletionResult result = new TplCompletionQuery(doc).query();
                if (result != null) {
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            items = getItems(result, doc, caretOffset);
                        }
                    });
                } else {
                    items = Collections.<TplCompletionItem>emptySet();
                }
                resultSet.addAllItems(items);

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private Set<TplCompletionItem> getItems(TplCompletionQuery.CompletionResult result, Document doc, int offset) {
            Set<TplCompletionItem> entries = new HashSet<>();
            ArrayList<String> commands; boolean inSmarty = false;

            if (CodeCompletionUtils.insideSmartyCode(doc, offset)) {
                if (CodeCompletionUtils.inVariableModifiers(doc, offset)) {
                    entries.addAll(result.getVariableModifiers());
                    inSmarty = true;
                }
                commands = CodeCompletionUtils.afterSmartyCommand(doc, offset);
                if (!commands.isEmpty()) {
                    entries.addAll(result.getParamsForCommand(commands));
                    inSmarty = true;
                }
                if (!inSmarty) {
                    if (result != null) {
                        entries.addAll(result.getFunctions());
                    }
                }
            }

            return entries;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            try {
                if (component.getText(component.getCaretPosition() - 1, 1).toString().equals("|")) {
                    return false;
                }
                
            } catch (BadLocationException ex) {
                return false;
            }

            String prefix = CodeCompletionUtils.getTextPrefix(component.getDocument(), component.getCaretPosition());

            //check the items
            for (CompletionItem item : items) {
                if (CodeCompletionUtils.startsWithIgnoreCase(((TplCompletionItem) item).getItemText(), prefix)) {
                    return true; //at least one item will remain
                }
            }

            return false;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            String prefix = CodeCompletionUtils.getTextPrefix(component.getDocument(), component.getCaretPosition());

            //check the items
            for (CompletionItem item : items) {
                if (CodeCompletionUtils.startsWithIgnoreCase(((TplCompletionItem) item).getItemText(), prefix)) {
                    resultSet.addItem(item);
                }
            }
            resultSet.finish();
        }
    }

    public static class DocQuery extends AbstractQuery {

        private CompletionItem item;

        public DocQuery(TplCompletionItem item) {
            this.item = item;
        }

        @Override
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item == null) {
                try {
                    //item == null means that the DocQuery is invoked
                    //based on the explicit documentation opening request
                    //(not ivoked by selecting a completion item in the list)
                    TplCompletionQuery.CompletionResult result = new TplCompletionQuery(doc).query();
                    if (result != null && result.getFunctions().size() > 0) {
                        item = result.getFunctions().iterator().next();
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            TplCompletionItem tplItem = (TplCompletionItem) item;
            if (tplItem != null && tplItem.getHelp() != null) {
                resultSet.setDocumentation(new DocItem(tplItem));
            }
        }
    }
    private abstract static class AbstractQuery extends AsyncCompletionQuery {

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            checkHideCompletion((BaseDocument) component.getDocument(), component.getCaretPosition());
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                doQuery(resultSet, doc, caretOffset);
            } finally {
                resultSet.finish();
            }
        }

        abstract void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset);
    }

    private static void checkHideCompletion(final BaseDocument doc, final int caretOffset) {
        //test whether we are just in text and eventually close the opened completion
        //this is handy after end tag autocompletion when user doesn't complete the
        //end tag and just types a text
        //test whether the user typed an ending quotation in the attribute value
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

                tokenSequence.move(caretOffset == 0 ? 0 : caretOffset - 1);
                if (!tokenSequence.moveNext()) {
                    return;
                }
            }
        });
    }

    private static class DocItem implements CompletionDocumentation {

        TplCompletionItem item;

        public DocItem(TplCompletionItem tci) {
            this.item = tci;
        }

        @Override
        public String getText() {
            return item.getHelp();
        }

        @Override
        public URL getURL() {
            return item.getHelpURL();
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
