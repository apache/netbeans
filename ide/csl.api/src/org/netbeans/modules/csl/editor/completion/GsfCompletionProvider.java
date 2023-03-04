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
package org.netbeans.modules.csl.editor.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolTip;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.GsfLanguage;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Code completion provider - delegates to language plugin for actual population of result set.
 * Based on JavaCompletionProvider by Dusan Balek.
 * 
 * @todo I may be able to rip out the code I had in here to work around the
 *   automatic completion vs "No Suggestions" issue; see 
 *    http://hg.netbeans.org/main?cmd=changeset;node=6740db8e6988
 *
 * @author Tor Norbye
 */
public class GsfCompletionProvider implements CompletionProvider {
    
    //private static final String COMMENT_CATEGORY_NAME = "comment";
    
//    public static CodeCompletionHandler getCompletable (ParserResult parserResult, int offset) {
//        Document document = info.getDocument();
//        if (document != null) {
//            return getCompletable(document,offset);
//        } else {
//            return null;
//        }
//    }

    private static Language getCompletableLanguage(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument)doc;
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (Language l : list) {
            if (l.getCompletionProvider() != null) {
                return l;
            }
        }

        return null;
    }
    
    public static CodeCompletionHandler getCompletable(Document doc, int offset) {
        Language l = getCompletableLanguage(doc, offset);
        if (l != null) {
            return l.getCompletionProvider();
        }

        return null;
    }

    public static CodeCompletionHandler getCompletable(ParserResult result) {
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(result.getSnapshot().getMimeType());
        if(l != null) {
            return l.getCompletionProvider();
        } else {
            return null;
        }
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (!autoPopup) {
            return 0;
        }
        if (typedText.length() > 0) {
            CodeCompletionHandler provider = getCompletable(component.getDocument(), component.getCaretPosition());
            if (provider != null) {
                QueryType autoQuery = provider.getAutoQuery(component, typedText);
                switch (autoQuery) {
                case NONE: return 0;
                case STOP: {
                    Completion.get().hideAll();
                    return 0;
                }
                case COMPLETION: return COMPLETION_QUERY_TYPE;
                case DOCUMENTATION: return DOCUMENTATION_QUERY_TYPE;
                case TOOLTIP: return TOOLTIP_QUERY_TYPE;
                case ALL_COMPLETION: return COMPLETION_ALL_QUERY_TYPE;
                }
            }
        }

        return 0;
    }

    // From Utilities
    public static boolean isJavaContext(final JTextComponent component, final int offset) {
        Document doc = component.getDocument();
        org.netbeans.api.lexer.Language language = (org.netbeans.api.lexer.Language)doc.getProperty(org.netbeans.api.lexer.Language.class);
        if (language == null) {
            return true;
        }
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            TokenSequence ts = TokenHierarchy.get(component.getDocument()).tokenSequence();

            if (ts == null) {
                return false;
            }
            if (!ts.moveNext() || ts.move(offset) == 0) {
                return true;
            }
            if (!ts.moveNext()) { // Move to the next token after move(offset)
                return false;
            }

// I used to block completion in comments... but why should I do that?            
//            TokenId tokenId = ts.token().id();
//            
//            Set s = language.tokenCategories().contains(COMMENT_CATEGORY_NAME) 
//                    ? language.tokenCategoryMembers(COMMENT_CATEGORY_NAME) 
//                    : null;
//            
//            return s == null || !s.contains(tokenId); //NOI18N
            return true;
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
    }

    public static boolean startsWith(String theString, String prefix) {
        if (theString == null || theString.length() == 0)
            return false;
        if (prefix == null || prefix.length() == 0)
            return true;
        return isCaseSensitive() ? theString.startsWith(prefix) :
            theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public CompletionTask createTask(int type, JTextComponent component) {
        if (((type & COMPLETION_QUERY_TYPE) != 0) || (type == TOOLTIP_QUERY_TYPE) ||
                (type == DOCUMENTATION_QUERY_TYPE)) {
            return new AsyncCompletionTask(new JavaCompletionQuery(type,
                    component.getSelectionStart()), component);
        }

        return null;
    }

    static CompletionTask createDocTask(ElementHandle element, ParserResult info) { // TODO - use ComObjectHandle ??
        JavaCompletionQuery query = new JavaCompletionQuery(DOCUMENTATION_QUERY_TYPE, -1);
        query.element = element;

        //return new AsyncCompletionTask(query, Registry.getMostActiveComponent());
        return new AsyncCompletionTask(query, EditorRegistry.lastFocusedComponent());
    }

    static final class JavaCompletionQuery extends AsyncCompletionQuery {
        private Collection<CompletionItem> results;
        private JToolTip toolTip;
        private CompletionDocumentation documentation;
        private int anchorOffset;
        //private int toolTipOffset;
        private JTextComponent component;
        private int queryType;
        private int caretOffset;
        private String filterPrefix;
        private ElementHandle element;
        private boolean isTruncated;
        private boolean isFilterable;
        //private Source source;
        /** The compilation info that the Element was generated for */

        JavaCompletionQuery(int queryType, int caretOffset) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int newCaretOffset = component.getSelectionStart();

            if (newCaretOffset >= caretOffset) {
                try {
                    Document doc = component.getDocument();
                    Language language = getCompletableLanguage(doc, caretOffset);
                    if (isJavaIdentifierPart(language, doc.getText(caretOffset,
                                    newCaretOffset - caretOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }

            Completion.get().hideCompletion();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, final int caretOffset) {
            final CancelSupportImplementation cs = CompletionCancelSupportImpl.create(this);
            SpiSupportAccessor.getInstance().setCancelSupport(cs);
            try {
                this.caretOffset = caretOffset;
                if (queryType == TOOLTIP_QUERY_TYPE || queryType == DOCUMENTATION_QUERY_TYPE || isJavaContext(component, caretOffset)) {
                    results = null;
                    isTruncated = false;
                    isFilterable = true;
                    documentation = null;
                    toolTip = null;
                    anchorOffset = -1;
                    Source source = Source.create (doc);
                    if (source == null) {
                        FileObject fo = null;
                        if (element != null) {
                            fo = element.getFileObject();
                            if (fo != null) {
                                source = Source.create (fo);
                            }
                        }
                    }
                    //if (queryType == DOCUMENTATION_QUERY_TYPE && element != null) {
                    //    FileObject fo = SourceUtils.getFile(element, js.getClasspathInfo());
                    //    if (fo != null)
                    //        js = Source.forFileObject(fo);
                    //}
                    if (source != null) {
                        final Collection<Source> sources = Collections.<Source> singleton (source);
                        final UserTask task = new UserTask () {
                            public void run (ResultIterator resultIterator) throws Exception {
                                Parser.Result result = resultIterator.getParserResult (caretOffset);
                                if(!(result instanceof ParserResult)) {
                                    return ;
                                }
                                ParserResult parserResult = (ParserResult) result;
                                if (parserResult == null) {
                                    return;
                                }
                                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                                    resolveCompletion(parserResult);
                                } else if (queryType == TOOLTIP_QUERY_TYPE) {
                                    resolveToolTip(parserResult);
                                } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                                    resolveDocumentation(parserResult);
                                }
                                GsfCompletionItem.tipProposal = null;
                            }
                        };
                        ParserManager.parse(sources, task);
                        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                            if (results != null)
                                resultSet.addAllItems(results);
                        } else if (queryType == TOOLTIP_QUERY_TYPE) {
                            if (toolTip != null)
                                resultSet.setToolTip(toolTip);
                        } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                            if (documentation != null)
                                resultSet.setDocumentation(documentation);
                        }

                        if (results != null && results.size() == 0) {
                            isFilterable = false;
                        }

                        if (anchorOffset > -1)
                            resultSet.setAnchorOffset(anchorOffset);
                    }
                }
            } catch (ParseException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                try {
                    resultSet.finish();
                } finally {
                    SpiSupportAccessor.getInstance().removeCancelSupport(cs);
                }
            }
        }
        
        @Override
        protected boolean canFilter(JTextComponent component) {
            filterPrefix = null;
            if (component.getCaret() == null) {
                return false;
            }
            int newOffset = component.getSelectionStart();
            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                if (isTruncated || !isFilterable) {
                    return false;
                }
                int offset = Math.min(anchorOffset, caretOffset);
                if (offset > -1) {
                    if (newOffset < offset)
                        return true;
                    if (newOffset >= caretOffset) {
                        try {
                            Document doc = component.getDocument();
                            Language language = getCompletableLanguage(doc, caretOffset);
                            String prefix = doc.getText(offset, newOffset - offset);
                            filterPrefix = isJavaIdentifierPart(language, prefix) ? prefix : null;
                            if (filterPrefix != null && filterPrefix.length() == 0)
                                anchorOffset = newOffset;
                        } catch (BadLocationException e) {}
                        return true;
                    }
                }
                return false;
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                try {
                    if (newOffset == caretOffset)
                        filterPrefix = "";
                    else if (newOffset - caretOffset > 0)
                        filterPrefix = component.getDocument().getText(caretOffset, newOffset - caretOffset);
                    else if (newOffset - caretOffset < 0)
                        //filterPrefix = newOffset > toolTipOffset ? component.getDocument().getText(newOffset, caretOffset - newOffset) : null;
                        filterPrefix = component.getDocument().getText(newOffset, caretOffset - newOffset);
                } catch (BadLocationException ex) {}
                return (filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1); // NOI18N
            }
            return false;            
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null) {
                        if (filterPrefix != null) {
                            resultSet.addAllItems(getFilteredData(results, filterPrefix));
                        } else {
                            Completion.get().hideDocumentation();
                            Completion.get().hideCompletion();
                        }
                    }
                } else if (queryType == TOOLTIP_QUERY_TYPE) {
                    resultSet.setToolTip(toolTip);
                }

                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            resultSet.finish();
        }
        
        private void resolveToolTip(final ParserResult controller) throws IOException {
            CompletionProposal proposal = GsfCompletionItem.tipProposal;
            Env env = getCompletionEnvironment(controller, false);
            CodeCompletionHandler completer = env.getCompletable();

            if (completer != null) {
                int offset = env.getOffset();
                ParameterInfo info = completer.parameters(controller, offset, proposal);
                if (info != ParameterInfo.NONE) {
                    
                    List<String> params = info.getNames();
             
                    // Take the parameter list, and balance them out into
                    // a "2d" set of lists used by the method params tip component:
                    // a list of lists - one for each row, and each row is a list
                    // for the clumn
                    int MAX_WIDTH = 50; // Max width before wrapping to the next line
                    int column = 0;
                    List<List<String>> parameterList = new ArrayList<List<String>>();
                    List<String> p = new ArrayList<String>();
                    for (int length = params.size(), i = 0; i < length; i++) {
                        String parameter = params.get(i);
                        if (i < length-1) {
                            parameter = parameter + ", ";
                        }
                        p.add(parameter);
                        
                        column += parameter.length();
                        if (column > MAX_WIDTH) {
                            column = 0;
                            parameterList.add(p);
                            p = new ArrayList<String>();
                            
                        }
                    }
                    if (p.size() > 0) {
                        parameterList.add(p);
                    }
            
                    int index = info.getCurrentIndex();
                    anchorOffset = info.getAnchorOffset();
                    if (anchorOffset == -1) {
                        anchorOffset = offset;
                    }
                    toolTip = new MethodParamsTipPaintComponent(parameterList, index, component);
                    //startPos = (int)sourcePositions.getEndPosition(env.getRoot(), mi.getMethodSelect());
                    //String text = controller.getText().substring(startPos, offset);
                    //anchorOffset = startPos + controller.getPositionConverter().getOriginalPosition(text.indexOf('(')); //NOI18N
                    //toolTipOffset = startPos + controller.getPositionConverter().getOriginalPosition(text.lastIndexOf(',')); //NOI18N
                    //if (toolTipOffset < anchorOffset)
                    //    toolTipOffset = anchorOffset;
                    return;

                }
            }
        }        
        
        private static class CodeCompletionContextImpl extends CodeCompletionContext {
            private final int caretOffset;
            private final ParserResult info;
            private final String prefix;
            private final boolean prefixMatch;
            private final QueryType queryType;

            public CodeCompletionContextImpl(int caretOffset, ParserResult info, String prefix, boolean prefixMatch, QueryType queryType) {
                this.caretOffset = caretOffset;
                this.info = info;
                this.prefix = prefix;
                this.prefixMatch = prefixMatch;
                this.queryType = queryType;
            }

            @Override
            public int getCaretOffset() {
                return caretOffset;
            }

//            @Override
//            public org.netbeans.modules.csl.api.CompilationInfo getInfo() {
//                return info;
//            }

            @Override
            public String getPrefix() {
                return prefix;
            }

            @Override
            public QueryType getQueryType() {
                return queryType;
            }

            @Override
            public boolean isPrefixMatch() {
                return prefixMatch;
            }

            @Override
            public boolean isCaseSensitive() {
                return GsfCompletionProvider.isCaseSensitive();
            }

            @Override
            public ParserResult getParserResult() {
                return info;
            }
        }

        void resolveDocumentation(ParserResult controller) throws IOException {
            if (element != null) {
                documentation = GsfCompletionDoc.create(controller, element, new Callable<Boolean>(){
                    @Override
                    public Boolean call() throws Exception {
                        return isTaskCancelled();
                    }
                });
            } else {
                Env env = getCompletionEnvironment(controller, false);
                int offset = env.getOffset();
                String prefix = env.getPrefix();
                results = new ArrayList<CompletionItem>();
                isTruncated = false;
                isFilterable = true;
                anchorOffset = env.getOffset() - ((prefix != null) ? prefix.length() : 0);

                CodeCompletionHandler completer = env.getCompletable();

                if (completer != null) {
                    CodeCompletionContext context = new CodeCompletionContextImpl(offset, controller, prefix, false, QueryType.DOCUMENTATION);
                    CodeCompletionResult result = completer.complete(context);
                    if (result == null) {
                        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, completer.getClass().getName() + " should return CodeCompletionResult.NONE rather than null");
                        result = CodeCompletionResult.NONE;
                    }

                    if (result != CodeCompletionResult.NONE) {
                        for (CompletionProposal proposal : result.getItems()) {
                            ElementHandle el = proposal.getElement();
                            if (el != null) {
                                documentation = GsfCompletionDoc.create(controller, el, new Callable<Boolean>(){
                                    @Override
                                    public Boolean call() throws Exception {
                                        return isTaskCancelled();
                                    }
                                });
                                // TODO - find some way to show the multiple overloaded methods?
                                if (documentation != null && documentation.getText() != null && documentation.getText().length() > 0) {
                                    // Make sure we at least pick an alternative that has documentation
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        CompletionDocumentation getDocumentation() {
            return documentation;
        }

        private void resolveCompletion (ParserResult controller)
            throws IOException {
            Env env = getCompletionEnvironment(controller, true);
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            results = new ArrayList<CompletionItem>();
            isTruncated = false;
            isFilterable = true;
            anchorOffset = env.getOffset() - ((prefix != null) ? prefix.length() : 0);

            CodeCompletionHandler completer = env.getCompletable();

            if (completer != null) {
                addCodeCompletionItems(controller, completer, offset, prefix);
                
                if (isTruncated) {
                    // Add truncation item
                    GsfCompletionItem item = GsfCompletionItem.createTruncationItem();
                    results.add(item);
                }
            }
        }
        
        private void addCodeCompletionItems(ParserResult controller, CodeCompletionHandler completer, int offset, String prefix) {
            QueryType qtype = queryType == 9 ? QueryType.ALL_COMPLETION : QueryType.COMPLETION;
            CodeCompletionContext context = new CodeCompletionContextImpl(offset, controller, prefix, true, qtype);
            CodeCompletionResult result = completer.complete(context);

            if (result == null) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, completer.getClass().getName() + " should return CodeCompletionResult.NONE rather than null");
                result = CodeCompletionResult.NONE;
            }

            if (result != CodeCompletionResult.NONE) {
                if (result.isTruncated()) {
                    isTruncated = true;
                }
                if (!result.isFilterable()) {
                    isFilterable = false;
                }

                for (CompletionProposal proposal : result.getItems()) {
                    GsfCompletionItem item = GsfCompletionItem.createItem(proposal, result, controller);

                    if (item != null) {
                        results.add(item);
                    }
                }

                
            }
        }

        private boolean isJavaIdentifierPart(Language language, String text) {
            GsfLanguage gsfLanguage = language != null ? language.getGsfLanguage() : null;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (gsfLanguage == null) {
                    if (!Character.isJavaIdentifierPart(c)) {
                        return false;
                    }
                } else if (!gsfLanguage.isIdentifierChar(c)) {
                    return false;
                }
            }

            return true;
        }

        private Collection getFilteredData(Collection<CompletionItem> data, String prefix) {
            if (prefix.length() == 0) {
                return data;
            }

            List ret = new ArrayList();

            for (Iterator<CompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();

                if (startsWith(itm.getInsertPrefix().toString(), prefix)) {
                    ret.add(itm);
                }

                //                else if (itm instanceof LazyTypeCompletionItem && Utilities.startsWith(((LazyTypeCompletionItem)itm).getItemText(), prefix))
                //                    ret.add(itm);
            }

            return ret;
        }

        /**
         * 
         * @param upToOffset If set, complete only up to the given caret offset, otherwise complete
         *   the full symbol at the offset
         */
        private Env getCompletionEnvironment(ParserResult controller, boolean upToOffset)
            throws IOException {
            // If you invoke code completion while indexing is in progress, the
            // completion job (which stores the caret offset) will be delayed until
            // indexing is complete - potentially minutes later. When the job
            // is finally run we need to make sure the caret position is still valid. (93017)
            Document doc = controller.getSnapshot ().getSource ().getDocument (false);
            int length = doc != null ? doc.getLength() : (int)controller.getSnapshot ().getSource ().getFileObject().getSize();
            if (caretOffset > length) {
                caretOffset = length;
            }
            
            int offset = caretOffset;
            String prefix = null;

            // 
            // TODO - handle the upToOffset parameter
            // Look at the parse tree, and find the corresponding end node
            // offset...
            
            CodeCompletionHandler completer = getCompletable(controller);
            try {
                // TODO: use the completion helper to get the contxt
                if (completer != null && offset != -1) {
                    prefix = completer.getPrefix(controller, offset, upToOffset);
                }
                if (prefix == null && doc != null) {
                    int[] blk =
                        org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)doc,
                            offset);

                    if (blk != null) {
                        int start = blk[0];

                        if (start < offset ) {
                            if (upToOffset) {
                                prefix = doc.getText(start, offset - start);
                            } else {
                                prefix = doc.getText(start, blk[1]-start);
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
            return new Env(offset, prefix, controller, completer);
        }
        
        private class Env {
            private int offset;
            private String prefix;
            private ParserResult controller;
            private CodeCompletionHandler completable;
            private boolean autoCompleting;

            private Env(int offset, String prefix, ParserResult controller, CodeCompletionHandler completable) {
                this.offset = offset;
                this.prefix = prefix;
                this.controller = controller;
                this.completable = completable;
            }

            public int getOffset() {
                return offset;
            }

            public String getPrefix() {
                return prefix;
            }
            
            public boolean isAutoCompleting() {
                return autoCompleting;
            }
            
            public void setAutoCompleting(boolean autoCompleting) {
                this.autoCompleting = autoCompleting;
            }

            public ParserResult getController() {
                return controller;
            }
            
            public CodeCompletionHandler getCompletable() {
                return completable;
            }
        }
    }
    
    // From Utilities
    private static boolean caseSensitive = true;
    private static boolean autoPopup = true;
    private static boolean inited;

    private static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    private static class SettingsListener implements PreferenceChangeListener {

//        public void settingsChange(SettingsChangeEvent evt) {
//            setCaseSensitive(SettingsUtil.getBoolean(GsfEditorKitFactory.GsfEditorKit.class,
//                    ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
//                    ExtSettingsDefaults.defaultCompletionCaseSensitive));
//        }

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey() == null || SimpleValueNames.COMPLETION_CASE_SENSITIVE.equals(evt.getKey())) {
                setCaseSensitive(Boolean.valueOf(evt.getNewValue()));
            } else if (SimpleValueNames.COMPLETION_AUTO_POPUP.equals(evt.getKey())) {
                setAutoPopup(Boolean.valueOf(evt.getNewValue()));
            }
        }
    }

    private static PreferenceChangeListener settingsListener = new SettingsListener();

    private static void setCaseSensitive(boolean b) {
        lazyInit();
        caseSensitive = b;
    }

    private static void setAutoPopup(boolean b) {
        lazyInit();
        autoPopup = b;
    }

    private static void lazyInit() {
        if (!inited) {
            inited = true;
            
            // correctly we should use a proper mime type for the document where the completion runs,
            // but at the moment this is enough, because completion settings are mainted globaly for all mime types
            // (ie. their the same for all mime types). Also, if using a specific mime type
            // this code should hold the prefs instance somewhere, but not in a static field!
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, settingsListener, prefs));
            
            setCaseSensitive(prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false));
            setAutoPopup(prefs.getBoolean(SimpleValueNames.COMPLETION_AUTO_POPUP, false));
        }
    }
}
