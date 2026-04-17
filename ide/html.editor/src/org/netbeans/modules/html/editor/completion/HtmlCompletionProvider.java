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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.javadoc.HelpManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Implementation of {@link CompletionProvider} for Html documents.
 *
 * @author Marek Fukala
 */
public class HtmlCompletionProvider implements CompletionProvider {

    private static final Logger LOG = Logger.getLogger(HtmlCompletionProvider.class.getName());
    private final AtomicBoolean AUTO_QUERY = new AtomicBoolean();

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        AUTO_QUERY.set(true);
        Document doc = component.getDocument();
        int dotPos = component.getCaret().getDot();
        return HtmlPreferences.autoPopupCompletionWindow() && checkOpenCompletion(doc, dotPos, typedText)
                ? COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE
                : 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        AsyncCompletionTask task = null;

        boolean triggeredByAutocompletion = AUTO_QUERY.getAndSet(false);

        if ((queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            task = new AsyncCompletionTask(new Query(triggeredByAutocompletion), component);
        } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
            task = new AsyncCompletionTask(new DocQuery(null, triggeredByAutocompletion), component);
        }

        return task;
    }

    private static class Query extends AbstractQuery {

        private int anchor;
        private volatile Collection<? extends CompletionItem> items = Collections.<CompletionItem>emptyList();
        private JTextComponent component;

        public Query(boolean triggeredByAutocompletion) {
            super(triggeredByAutocompletion);
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                HtmlCompletionQuery.CompletionResult result = new HtmlCompletionQuery(doc, caretOffset, triggeredByAutocompletion).query();
                if (result != null) {
                    items = result.getItems();
                    anchor = result.getAnchor();
                } else {
                    items = Collections.emptyList();
                    anchor = 0;
                }
                resultSet.addAllItems(items);
                resultSet.setAnchorOffset(anchor);

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            try {
                if (component.getCaret() == null || component.getCaretPosition() < anchor) {
                    return false;
                }
                
                Document doc = component.getDocument();
                int offset = component.getCaretPosition();

                String prefix = doc.getText(anchor, offset - anchor);

                //check the items
                for (CompletionItem item : items) {
                    if (item instanceof HtmlCompletionItem) {
                        String itemText = ((HtmlCompletionItem) item).getItemText();
                        if(itemText != null) { //http://netbeans.org/bugzilla/show_bug.cgi?id=222234
                            if (startsWithIgnoreCase(itemText, prefix)) {
                                return true; //at least one item will remain
                            }
                        } else {
                            LOG.log(Level.WARNING, "CompletionItem {0} returned null from getItemText()!", item);
                        }
                    }
                }


            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            return false;

        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                Document doc = component.getDocument();
                int offset = component.getCaretPosition();
                String prefix = doc.getText(anchor, offset - anchor);

                //check the items
                for (CompletionItem item : items) {
                    if (item instanceof HtmlCompletionItem) {
                        if (startsWithIgnoreCase(((HtmlCompletionItem) item).getItemText(), prefix)) {
                            resultSet.addItem(item);
                        }
                    }
                }

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                resultSet.setAnchorOffset(anchor);
                resultSet.finish();
            }

        }

        private static boolean startsWithIgnoreCase(String text, String prefix) {
            return text.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
        }
    }
    
    private static boolean assertsEnabled;
    static {
        assertsEnabled = false;
        assert assertsEnabled = true;
    }

    public static class DocQuery extends AbstractQuery {

        private CompletionItem item;

        public DocQuery(HtmlCompletionItem item, boolean triggeredByAutocompletion) {
            super(triggeredByAutocompletion);
            this.item = item;
        }

        @Override
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item == null) {
                try {
                    //item == null means that the DocQuery is invoked
                    //based on the explicit documentation opening request
                    //(not ivoked by selecting a completion item in the list)
                    HtmlCompletionQuery.CompletionResult result = new HtmlCompletionQuery(doc, caretOffset, false).query();
                    if (result == null) {
                        // Query method returned no CompletionResult.
                        return;
                    }
                        try {
                            int rowEnd = LineDocumentUtils.getLineEndOffset((BaseDocument)doc, caretOffset);
                            final String documentText = doc.getText(result.getAnchor(), rowEnd - result.getAnchor());

                            // Go through result items and select completionItem 
                            // with same tag document cursor is on.
                            for (CompletionItem completionItem : result.getItems()) {
                                if (LexerUtils.startsWith(documentText, completionItem.getInsertPrefix(), true, false)) {
                                    if(item == null) {
                                        item = completionItem;
                                        if(!assertsEnabled) {
                                            break; //be quick in production, the list of items can be really long
                                        }
                                    } else {
                                        // only warning
                                        LOG.log(Level.WARNING, 
                                                "More than one CompletionItem found with InsertPrefix {0}, item.insertPrefix={1}", 
                                                new Object[]{completionItem.getInsertPrefix(), item.getInsertPrefix()});
                                    }
                                }
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            HtmlCompletionItem htmlItem = (HtmlCompletionItem) item;
            if (htmlItem != null && htmlItem.hasHelp()) {
                resultSet.setDocumentation(createCompletionDocumentation(htmlItem));
            }
        }
    }

    private abstract static class AbstractQuery extends AsyncCompletionQuery {

        protected final boolean triggeredByAutocompletion;

        public AbstractQuery(boolean triggeredByAutocompletion) {
            this.triggeredByAutocompletion = triggeredByAutocompletion;
        }

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

                Token tokenItem = tokenSequence.token();
                if (tokenItem.id() == HTMLTokenId.TEXT && !tokenItem.text().toString().startsWith("<") && !tokenItem.text().toString().startsWith("&")) {
                    hideCompletion();
                }
            }
        });
    }

    static boolean checkOpenCompletion(Document document, final int dotPos, String typedText) {
        final BaseDocument doc = (BaseDocument) document;
        switch (typedText.charAt(typedText.length() - 1)) {
            case '/':
                if (dotPos >= 2) { // last char before inserted slash
                    try {
                        String txtBeforeSpace = doc.getText(dotPos - 2, 2);
                        if (txtBeforeSpace.equals("</")) // NOI18N
                        {
                            return true;
                        }
                    } catch (BadLocationException e) {
                        //no action
                    }
                }
                break;
            case ' ':
                //Bug 235048 - second tab activates suggestion in html editor 
                //trigger the completion window only if the user types space
                //char, not upon tab expand or enter + indentation.
                //
                //in theory one could set tab size to 1 and then the issue would
                //reappear, but it's not worth adding a new condition :-)
                if(typedText.length() > 1) {
                    return false;
                }
                final AtomicBoolean value = new AtomicBoolean();
                doc.render(new Runnable() {

                    @Override
                    public void run() {
                        TokenSequence ts = Utils.getJoinedHtmlSequence(doc, dotPos);
                        if (ts == null) {
                            //no suitable token sequence found
                            value.set(false);
                            return;
                        }

                        int diff = ts.move(dotPos);
                        if (diff == 0) {
                            //just after a token
                            if (ts.movePrevious()) {
                                if(ts.token().id() == HTMLTokenId.WS) {
                                    //just after a whitespace
                                    if(ts.movePrevious()) {
                                        //test what precedes the WS -- are we inside a tag?
                                        value.set(ts.token().id() == HTMLTokenId.TAG_OPEN
                                                || ts.token().id() == HTMLTokenId.VALUE
                                                || ts.token().id() == HTMLTokenId.VALUE_CSS
                                                || ts.token().id() == HTMLTokenId.VALUE_JAVASCRIPT);
                                    }
                                }
                            }
                        } else if (diff > 0) {
                            //after first char of the token
                            if (ts.moveNext()) {
                                if (ts.token().id() == HTMLTokenId.WS) {
                                    if (ts.movePrevious()) {
                                        value.set(ts.token().id() == HTMLTokenId.TAG_OPEN
                                                || ts.token().id() == HTMLTokenId.VALUE
                                                || ts.token().id() == HTMLTokenId.VALUE_CSS
                                                || ts.token().id() == HTMLTokenId.VALUE_JAVASCRIPT);
                                    }
                                }
                            }
                        }
                    }
                });
                return value.get();
            case '<':
            case '&':
                return true;
            case '>':
                //handle tag autocomplete
                if(HtmlPreferences.autoPopupEndTagAutoCompletion()) {
                    final boolean[] ret = new boolean[1];
                    doc.runAtomic(new Runnable() {

                        @Override
                        public void run() {
                            TokenSequence ts = Utils.getJoinedHtmlSequence(doc, dotPos);
                            if (ts == null) {
                                //no suitable token sequence found
                                ret[0] = false;
                            } else {
                                ts.move(dotPos - 1);
                                if (ts.moveNext() || ts.movePrevious()) {
                                    if (!CharSequenceUtilities.equals("/>", ts.token().text()) &&
                                        null == LexerUtils.followsToken(ts, HTMLTokenId.TAG_CLOSE, true, false,
                                            HTMLTokenId.WS,
                                            HTMLTokenId.TAG_CLOSE_SYMBOL)) {
                                        ret[0] = true;
                                    }
                                }
                            }
                        }
                    });
                    return ret[0];
                }

        }
        return false;

    }

    private static void hideCompletion() {
        Completion.get().hideCompletion();
        Completion.get().hideDocumentation();
    }

    private static class LegacyLinkDocItem implements CompletionDocumentation {

        private URL url;

        public LegacyLinkDocItem(URL url) {
            this.url = url;
        }

        @Override
        public String getText() {
            return null;
            /*
            String anchor = HelpManager.getDefault().getAnchorText(url);
            if(anchor != null)
            return HelpManager.getDefault().getHelpText(url, anchor);
            else
            return HelpManager.getDefault().getHelpText(url);
             */
        }

        @Override
        public URL getURL() {
            return url;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return new LegacyLinkDocItem(HelpManager.getDefault().getRelativeURL(url, link));
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static class LinkDocItem implements CompletionDocumentation {

        private URL url;
        private HelpResolver resolver;

        public LinkDocItem(HelpResolver resolver, URL url) {
            this.url = url;
            this.resolver = resolver;
        }

        @Override
        public String getText() {
            //normally it should be enough to return null here
            //and the documentation would be loaded from the URL.
            //However it seems that the html5 anchor navigation doesn't
            //properly work in the embedded swing browser so I need to
            //strip the begginning of the file to the anchor manually
            return resolver.getHelpContent(getURL());
        }

        @Override
        public URL getURL() {
            return url;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return new LinkDocItem(resolver, resolver.resolveLink(getURL(), link));
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static class NoDocItem implements CompletionDocumentation {

        @Override
        public String getText() {
            return NbBundle.getMessage(HtmlCompletionProvider.class, "MSG_No_Doc_For_Target"); //NOI18N
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

    private static CompletionDocumentation createCompletionDocumentation(HtmlCompletionItem item) {
        //fork for the new and old help approach, legacy html4 not migrated yet
        HelpItem helpItem = item.getHelpItem();
        if (helpItem != null) {
            return new HtmlTagDocumetationItem(item);
        }

        //else legacy approach
        return new DocItem(item);
    }

    private static class HtmlTagDocumetationItem implements CompletionDocumentation {

        private final HtmlCompletionItem item;
        private final String documentationText;

        public HtmlTagDocumetationItem(HtmlCompletionItem ri) {
            this.item = ri;
            
            //initialize the text in constructor as it is not called from EDT
            //in contrary to the {@link #getText()} method.
            this.documentationText = loadDocText(); 
        }

        private HelpItem getHelpItem() {
            return item.getHelpItem();
        }

        private String loadDocText() {
            //normally it should be enough to return null here
            //and the documentation would be loaded from the URL.
            //However it seems that the html5 anchor navigation doesn't
            //properly work in the embedded swing browser so I need to
            //strip the begginning of the file to the anchor manually

            //now the statement above is not fully true since I need to add
            //the header before the URL content
            StringBuilder sb = new StringBuilder();
            String header = getHelpItem().getHelpHeader();
            if (header != null) {
                sb.append(header);
            }

            String helpContent = getHelpItem().getHelpContent() != null
                    ? getHelpItem().getHelpContent()
                    : getHelpItem().getHelpResolver().getHelpContent(getURL());

            sb.append(helpContent);

            return sb.toString();
        }
        
        @Override
        public String getText() {
            return documentationText;
        }

        @Override
        public URL getURL() {
            return getHelpItem().getHelpURL();
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            URL itemUrl = getHelpItem().getHelpResolver().resolveLink(getURL(), link);
            return itemUrl != null
                    ? new LinkDocItem(getHelpItem().getHelpResolver(), itemUrl)
                    : new NoDocItem();
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static class DocItem implements CompletionDocumentation {

        HtmlCompletionItem item;

        public DocItem(HtmlCompletionItem ri) {
            this.item = ri;
            ri.prepareHelp();
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
            URL itemUrl = HelpManager.getDefault().getHelpURL(item.getHelpId());
            return itemUrl != null
                    ? new LegacyLinkDocItem(HelpManager.getDefault().getRelativeURL(itemUrl, link))
                    : new NoDocItem();
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }
}
