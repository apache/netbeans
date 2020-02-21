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
package org.netbeans.modules.cnd.completion.cplusplus;

import java.util.*;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.MethodParamsTipPaintComponent;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * this is the modified copy of JavaCompletionProvider
 */
public class CsmCompletionProvider implements CompletionProvider {

    private static final boolean TRACE = false;

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        CompletionSupport sup = CompletionSupport.get(component);
        if (sup == null) {
            return 0;
        }
        String[] triggers = CsmCompletionUtils.getCppAutoCompletionTrigers();
        if (!CompletionSupport.needShowCompletionOnTextLite(component, typedText, triggers)) {
            return 0;
        }
        final int dot = component.getCaret().getDot();
        if (CsmCompletionQuery.checkCondition(component.getDocument(), dot, false)) {
            if (CompletionSupport.needShowCompletionOnText(component, typedText, triggers)) {
                return COMPLETION_QUERY_TYPE;
            }
        }
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        CompletionSupport sup = CompletionSupport.get(component);
        if (sup == null) {
            return null;
        }
        final int dot = component.getCaret().getDot();
        // disable code templates for smart mode of completion
        //CsmCodeTemplateFilter.enableAbbreviations(((queryType & COMPLETION_ALL_QUERY_TYPE) == COMPLETION_ALL_QUERY_TYPE));
        CsmResultItem.setEnableInstantSubstitution(false);
        if (TRACE) {
            System.err.println("createTask called on " + dot); // NOI18N
        }
        // do not work together with include completion
        if (CsmCompletionQuery.checkCondition(component.getDocument(), dot, false, queryType)) {
            if ((queryType & COMPLETION_QUERY_TYPE) == COMPLETION_QUERY_TYPE) {
                return new AsyncCompletionTask(new Query(dot, queryType), component);
            } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                return new AsyncCompletionTask(new DocumentationQuery(), component);
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                return new AsyncCompletionTask(new ToolTipQuery(), component);
            }
        }
        return null;
    }

    private static NbCsmCompletionQuery getCompletionQuery(CompletionResolver.QueryScope scope) {
        return new NbCsmCompletionQuery(null, scope, null, false);
    }

    /** for tests */
    public static CsmCompletionQuery getTestCompletionQuery(CsmFile csmFile, CompletionResolver.QueryScope scope) {
        return new NbCsmCompletionQuery(csmFile, scope, null, false);
    }

    public static CsmCompletionQuery createCompletionResolver(CsmFile csmFile, CompletionResolver.QueryScope queryScope, FileReferencesContext fileReferencesContext) {
        return new NbCsmCompletionQuery(csmFile, queryScope, fileReferencesContext, true);
    }

    static final class Query extends AsyncCompletionQuery {

        private JTextComponent component;
        private NbCsmCompletionQuery.CsmCompletionResult queryResult;
        private int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;
        private boolean caseSensitive = false;
        private CompletionResolver.QueryScope queryScope;

        Query(int caretOffset, int queryType) {
            if (TRACE) {
                System.err.println("Query started creating"); // NOI18N
            }
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
            if ((queryType & COMPLETION_ALL_QUERY_TYPE) != COMPLETION_ALL_QUERY_TYPE) {
                this.queryScope = CompletionResolver.QueryScope.SMART_QUERY;
            } else {
                this.queryScope = CompletionResolver.QueryScope.GLOBAL_QUERY;
            }
            if (TRACE) {
                System.err.println("Query created " + getTestState()); // NOI18N
            }
        }

        private String getTestState() {
            StringBuilder builder = new StringBuilder();
            builder.append(" creationCaretOffset = ").append(creationCaretOffset); // NOI18N
            builder.append(" queryAnchorOffset = ").append(queryAnchorOffset); // NOI18N
            builder.append(" queryScope = ").append(queryScope); // NOI18N
            builder.append(" filterPrefix = ").append(filterPrefix); // NOI18N
            if (queryResult == null) {
                builder.append(" no queryResult"); // NOI18N
            } else if (queryResult.isSimpleVariableExpression()) {
                builder.append(" queryResult is simple"); // NOI18N
            } else {
                builder.append(" queryResult is not simple"); // NOI18N
            }
            return builder.toString();
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            if (TRACE) {
                System.err.println("preQueryUpdate" + getTestState()); // NOI18N
            }
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            String mimeType = CsmCompletionUtils.getMimeType(component);
            caseSensitive = mimeType != null ? CsmCompletionUtils.isCaseSensitive(mimeType) : false;
            if (creationCaretOffset > 0 && caretOffset >= creationCaretOffset) {
                try {
                    if (isCppIdentifierPart(doc.getText(creationCaretOffset, caretOffset - creationCaretOffset))) {
                        if (TRACE) {
                            System.err.println("preQueryUpdate return" + getTestState()); // NOI18N
                        }
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            if (TRACE) {
                System.err.println("preQueryUpdate hide completion" + getTestState()); // NOI18N
            }
            Completion.get().hideCompletion();
        }
        private static final int MAX_ITEMS_TO_DISPLAY;


        static {
            int val = 256;
            if (System.getProperty("cnd.completion.items") != null) { // NOI18N
                try {
                    val = Integer.parseInt(System.getProperty("cnd.completion.items")); // NOI18N
                } catch (NumberFormatException numberFormatException) {
                    val = 256;
                }
            }
            if (val < 0) {
                val = Integer.MAX_VALUE;
            }
            MAX_ITEMS_TO_DISPLAY = val;
        }

        private void addItems(CompletionResultSet resultSet, Collection<? extends CompletionItem> toAdd) {
            if (TRACE) {
                System.err.println("adding items " + getTestState()); // NOI18N
            }
            boolean limit = false;
            Collection<CompletionItem> items = new ArrayList<CompletionItem>(toAdd.size());
            Set<String> handled = new HashSet<String>(toAdd.size());
            for (CompletionItem item : toAdd) {
                if (item instanceof CsmResultItem) {
                    final String stringPresentation = ((CsmResultItem)item).getStringPresentation();
                    if (handled.add(stringPresentation)) {
                        items.add(item);
                    } else if (TRACE) {
                        System.err.println("skip object with same text " + item); // NOI18N
                    }
                } else {
                    if (TRACE) {
                        System.err.println("add not CsmResultItem item " + item); // NOI18N
                    }
                    items.add(item);
                }
            }
            if (items.size() > MAX_ITEMS_TO_DISPLAY && queryResult.isSimpleVariableExpression()) {
                limit = true;
            }
//            ((queryScope == CompletionResolver.QueryScope.GLOBAL_QUERY) && queryResult.isSimpleVariableExpression())
//                             || (items.size() > MAX_ITEMS_TO_DISPLAY);
            resultSet.setHasAdditionalItems(queryScope == CompletionResolver.QueryScope.SMART_QUERY);
            if (!limit) {
                //CsmResultItem.setEnableInstantSubstitution(queryScope == CompletionResolver.QueryScope.GLOBAL_QUERY);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            } else {
                resultSet.estimateItems(MAX_ITEMS_TO_DISPLAY + 1, -1);
                int count = 0;
                for (CompletionItem item : items) {
                    resultSet.addItem(item);
                    if (++count > MAX_ITEMS_TO_DISPLAY) {
                        break;
                    }
                }
                // need fake item
                resultSet.addItem(lastItem);
            }
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (TRACE) {
                System.err.println("query begin" + getTestState()); // NOI18N
            }
            boolean hide = (caretOffset <= queryAnchorOffset) && (filterPrefix == null);
            if (!hide) {
                creationCaretOffset = caretOffset;
                NbCsmCompletionQuery query = getCompletionQuery(queryScope);
                NbCsmCompletionQuery.CsmCompletionResult res = query.query(component, caretOffset, true);
                if (res == null || (res.getItems().isEmpty() && (queryScope == CompletionResolver.QueryScope.SMART_QUERY))) {
                    // switch to global context
                    if (TRACE) {
                        System.err.println("query switch to global" + getTestState()); // NOI18N
                    }
                    queryScope = CompletionResolver.QueryScope.GLOBAL_QUERY;
                    if (res == null || res.isSimpleVariableExpression()) {
                        // try once more for non dereferenced expressions
                        query = getCompletionQuery(queryScope);
                        res = query.query(component, caretOffset, true);
                    }
                    if (TRACE) {
                        System.err.println("query switched to global" + getTestState()); // NOI18N
                    }
                }
                if (res != null) {
                    if (queryScope == CompletionResolver.QueryScope.SMART_QUERY &&
                            !res.isSimpleVariableExpression()) {
                        // change to global mode
                        queryScope = CompletionResolver.QueryScope.GLOBAL_QUERY;
                    }
                    queryAnchorOffset = res.getSubstituteOffset();
                    Collection<? extends CompletionItem> items = res.getItems();
                    // no more title in NB 6 in completion window
                    //resultSet.setTitle(res.getTitle());
                    resultSet.setAnchorOffset(queryAnchorOffset);
                    queryResult = res;
                    addItems(resultSet, items);
                }
            } else {
                if (TRACE) {
                    System.err.println("query hide completion" + getTestState()); // NOI18N
                }
                Completion.get().hideCompletion();
            }
            if (TRACE) {
                System.err.println("query end" + getTestState()); // NOI18N
            }
            resultSet.finish();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            if (TRACE) {
                System.err.println("prepareQuery" + getTestState()); // NOI18N
            }
            this.component = component;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            if (TRACE) {
                System.err.println("canFilter on " + caretOffset + getTestState()); // NOI18N
            }
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= creationCaretOffset) {
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                    if (queryResult == null || !isCppIdentifierPart(filterPrefix)) {
                        filterPrefix = null;
                    } else {
                        Collection<?> items = getFilteredData(queryResult.getItems(), filterPrefix);
                        if (items.isEmpty()) {
                            filterPrefix = null;
                        }
                    }
                } catch (BadLocationException e) {
                    // filterPrefix stays null -> no filtering
                }
            }
            if (TRACE) {
                if (filterPrefix == null) {
                    System.err.println("canFilter ended with false:" + getTestState()); // NOI18N
                } else {
                    System.err.println("canFilter ended with true:" + getTestState()); // NOI18N
                }
            }
            return (filterPrefix != null);
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (TRACE) {
                System.err.println("filter begin" + getTestState());// NOI18N
            }
            if (filterPrefix != null && queryResult != null) {
                // no more title in NB 6 in completion window
                //resultSet.setTitle(getFilteredTitle(queryResult.getTitle(), filterPrefix));
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<CompletionItem> items = getFilteredData(queryResult.getItems(), filterPrefix);
                if (TRACE) {
                    System.err.println("filter with prefix" + getTestState()); // NOI18N
                }
                addItems(resultSet, items);
            }
            if (TRACE) {
                System.err.println("filter end" + getTestState()); // NOI18N
            }
            resultSet.finish();
        }

        private boolean isCppIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(CndLexerUtilities.isCppIdentifierPart(text.charAt(i)))) {
                    return false;
                }
            }
            return true;
        }

        private Collection<CompletionItem> getFilteredData(Collection<? extends CompletionItem> data, String prefix) {
            List<CompletionItem> ret = new ArrayList<CompletionItem>(1024);
            for (CompletionItem itm : data) {
                // TODO: filter
                if (matchPrefix(itm.getInsertPrefix(), prefix, caseSensitive)) {
                    ret.add(itm);
                }
            }
            return ret;
        }

        private boolean matchPrefix(CharSequence text, String prefix, boolean caseSensitive) {
            if (CharSequenceUtils.startsWith(text, prefix)) {
                return true;
            }
            if (!caseSensitive) {
                return CharSequenceUtils.startsWithIgnoreCase(text, prefix);
            }
            return false;
        }

        private String getFilteredTitle(String title, String prefix) {
            int lastIdx = title.lastIndexOf('.');
            String ret = lastIdx == -1 ? prefix : title.substring(0, lastIdx + 1) + prefix;
            if (title.endsWith("*")) {// NOI18N
                ret += "*"; // NOI18N
            }
            return ret;
        }
    }

    static class ToolTipQuery extends AsyncCompletionQuery {

        private JTextComponent component;
        private int queryCaretOffset;
        private int queryAnchorOffset;
        private JToolTip queryToolTip;
        /** Method/constructor '(' position for tracking whether the method is still
         * being completed.
         */
        private Position queryMethodParamsStartPos = null;
        private boolean otherMethodContext;

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            Position oldPos = queryMethodParamsStartPos;
            queryMethodParamsStartPos = null;
            NbCsmCompletionQuery query = getCompletionQuery(CompletionResolver.QueryScope.GLOBAL_QUERY);
            BaseDocument bdoc = (BaseDocument) doc;
            //NbCsmCompletionQuery.CsmCompletionResult res = null;// (NbCsmCompletionQuery.CsmCompletionResult)query.tipQuery(component, caretOffset, bdoc.getSyntaxSupport(), false);
//            NbCsmCompletionQuery query = new NbCsmCompletionQuery();
            NbCsmCompletionQuery.CsmCompletionResult res = query.query(component, caretOffset, true, false, true, true);
            if (res != null) {
                queryCaretOffset = caretOffset;
                List<List<String>> list = new ArrayList<List<String>>();
                int idx = -1;
                boolean checked = false;
                for (Iterator<?> it = res.getItems().iterator(); it.hasNext();) {
                    Object o = it.next();
                    if (o instanceof CsmResultItem.ConstructorResultItem) {
                        CsmResultItem.ConstructorResultItem item = (CsmResultItem.ConstructorResultItem) o;

                        if (!checked) {
                            CsmCompletionExpression exp = item.getExpression();
                            int idxLast = exp.getTokenCount() - 1;
                            if (idxLast >= 0) {
                                if (exp.getExpID() == CsmCompletionExpression.METHOD &&
                                        exp.getTokenID(idxLast) == CppTokenId.RPAREN) {
                                    // check if query offset is after closing ")"
                                    if (exp.getTokenOffset(idxLast) + exp.getTokenLength(idxLast) <= caretOffset) {
                                        resultSet.finish();
                                        return;
                                    }
                                } else if (exp.getExpID() == CsmCompletionExpression.VARIABLE) {
                                    if (exp.getTokenOffset(0) + exp.getTokenLength(0) >= caretOffset) {
                                        resultSet.finish();
                                        return;
                                    }
                                }
                                try {
                                    queryMethodParamsStartPos = bdoc.createPosition(exp.getTokenOffset(0));
                                } catch (BadLocationException ble) {
                                }
                            }
                            checked = true;
                        }

                        List<String> parms = item.createParamsList();
                        if (parms.size() > 0) {
                            idx = item.getCurrentParamIndex();
                        } else {
                            parms.add(NbBundle.getMessage(CsmCompletionProvider.class, "CC-no-parameters")); // NOI18N
                        }
                        list.add(parms);
                    }
                }

                resultSet.setAnchorOffset(queryAnchorOffset = res.getSubstituteOffset());
                resultSet.setToolTip(queryToolTip = new MethodParamsTipPaintComponent(list, idx));
            }
            resultSet.finish();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            String text = null;
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            try {
                if (caretOffset - queryCaretOffset > 0) {
                    text = doc.getText(queryCaretOffset, caretOffset - queryCaretOffset);
                } else if (caretOffset - queryCaretOffset < 0) {
                    text = doc.getText(caretOffset, queryCaretOffset - caretOffset);
                } else {
                    text = "";
                } //NOI18N
            } catch (BadLocationException e) {
            }
            if (text == null) {
                return false;
            }

            boolean filter = true;
            int balance = 0;
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                switch (ch) {
                    case ',':
                        filter = false;
                        break;
                    case '(':
                        balance++;
                        filter = false;
                        break;
                    case ')':
                        balance--;
                        filter = false;
                        break;
                }
                if (balance < 0) {
                    otherMethodContext = true;
                }
            }
            if (otherMethodContext && balance < 0) {
                otherMethodContext = false;
            }
            if (queryMethodParamsStartPos == null || caretOffset <= queryMethodParamsStartPos.getOffset()) {
                filter = false;
            }
            return otherMethodContext || filter;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (!otherMethodContext) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                resultSet.setToolTip(queryToolTip);
            }
            resultSet.finish();
        }
    }

    private static final class DocumentationQuery extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CsmCacheManager.enter();
            try {
                CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
                if (csmFile != null) {
                    CsmObject csmObject = ReferencesSupport.findDeclaration(csmFile, doc, null, caretOffset);
                    if (csmObject != null) {
                        CsmDocProvider docProvider = Lookup.getDefault().lookup(CsmDocProvider.class);
                        if (docProvider != null) {
                            CompletionDocumentation documentation = docProvider.createDocumentation(csmObject, csmFile);
                            if (documentation != null) {
                                resultSet.setDocumentation(documentation);
                            }
                        }
                    }
                }
                resultSet.finish();
            } finally {
                CsmCacheManager.leave();
            }
        }
    }

    private static final CompletionItem lastItem = new LastResultItem();

    private final static class LastResultItem extends CsmResultItem {

        private final String str;
        private static CsmPaintComponent.StringPaintComponent stringComponent = null;

        public LastResultItem() {
            super(null, Integer.MAX_VALUE);
            this.str = "" + Query.MAX_ITEMS_TO_DISPLAY + " " + NbBundle.getMessage(CsmCompletionProvider.class, "last-item-text"); // NOI18N
        }

        @Override
        public java.awt.Component getPaintComponent(boolean isSelected) {
            // lack of sync is intentional, no harm if we do this twice
            if (stringComponent == null) {
                stringComponent = new CsmPaintComponent.StringPaintComponent();
            }
            stringComponent.setString(str);
            return stringComponent;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            // do nothing
        }

        @Override
        public String getItemText() {
            return str;
        }
    }
}

