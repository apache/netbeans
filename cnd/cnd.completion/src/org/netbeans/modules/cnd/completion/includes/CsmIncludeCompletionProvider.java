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
package org.netbeans.modules.cnd.completion.includes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 *
 */
public class CsmIncludeCompletionProvider implements CompletionProvider {

    private final static boolean TRACE = Boolean.getBoolean("cnd.completion.includes.trace");

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        CompletionSupport sup = CompletionSupport.get(component);
        if (sup == null) {
            return 0;
        }
        String[] triggers = CsmCompletionUtils.getPreprocAutoCompletionTrigers();
        if (!CompletionSupport.needShowCompletionOnTextLite(component, typedText, triggers)) {
            return 0;
        }
        if (CompletionSupport.needShowCompletionOnText(component, typedText, triggers)) {
            int dot = component.getCaret().getDot();
            if (TRACE) {
                System.err.println("offset " + dot); // NOI18N
            }
            if (CompletionSupport.isIncludeCompletionEnabled(component.getDocument(), dot)) {
                if (TRACE) {
                    System.err.println("include completion will be shown on " + dot); // NOI18N
                }
                return COMPLETION_QUERY_TYPE;
            } else {
                if (TRACE) {
                    System.err.println("include completion will NOT be shown on " + dot); // NOI18N
                }
            }
        }
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (TRACE) {
            System.err.println("queryType = " + queryType); // NOI18N
        }
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            boolean all = (queryType == COMPLETION_ALL_QUERY_TYPE);
            int dot = component.getCaret().getDot();
            if (CompletionSupport.isIncludeCompletionEnabled(component.getDocument(), dot)) {
                if (TRACE) {
                    System.err.println("include completion task is created with offset " + dot); // NOI18N
                }
                return new AsyncCompletionTask(new Query(dot, all), component);
            } else {
                if (TRACE) {
                    System.err.println("include completion task is NOT created on " + dot); // NOI18N
                }
            }
        }
        return null;
    }

    // method for tests
    /*package*/ static Collection<CsmIncludeCompletionItem> getFilteredData(BaseDocument doc, int caretOffset, int queryType) {
        Query query = new Query(caretOffset, true);
        Collection<CsmIncludeCompletionItem> items = query.getItems(doc, caretOffset);
        items = query.getFilteredData(items, query.filterPrefix);
        if (TRACE) {
            System.err.println("Completion Items " + items.size());
            for (CsmIncludeCompletionItem completionItem : items) {
                System.err.println(completionItem.toString());
            }
        }
        return items;
    }

    private static final class Query extends AsyncCompletionQuery {

        private JTextComponent component;
        private Collection<CsmIncludeCompletionItem> results;
        private int creationCaretOffset;
        private int resultSetAnchorOffset;
        private int queryAnchorOffset;
        private int queryAnchorOffsetDelta; // sometimes we adjust anchor but we still must take real unchor into account during completion
        private String dirPrefix;
        private String filterPrefix;
        private Boolean usrInclude;
        private boolean showAll;
        private boolean caseSensitive;

        /*package*/ Query(int caretOffset, boolean showAll) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
            this.resultSetAnchorOffset = creationCaretOffset;
            this.showAll = showAll;
            this.caseSensitive = false;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            if (TRACE) {
                System.err.println("preQueryUpdate on " + caretOffset + " created on " + creationCaretOffset); // NOI18N
            }
            Document doc = component.getDocument();
            if (creationCaretOffset > 0 && caretOffset >= creationCaretOffset) {
                try {
                    if (isValidIncludeNamePart(doc.getText(creationCaretOffset, caretOffset - creationCaretOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (TRACE) {
                System.err.println("query on " + caretOffset + " anchor " + queryAnchorOffset); // NOI18N
            }
            Collection<CsmIncludeCompletionItem> items = getItems((BaseDocument) doc, caretOffset);
            if (this.queryAnchorOffset > 0) {
                if (items != null && items.size() > 0) {
                    this.caseSensitive = isCaseSensitive(component);
                    this.results = items;
                    items = getFilteredData(items, filterPrefix);
                    resultSet.estimateItems(items.size(), -1);
                    resultSet.addAllItems(items);
                    resultSet.setAnchorOffset(resultSetAnchorOffset);
                }
                resultSet.setHasAdditionalItems(true);
            }
            resultSet.finish();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            filterPrefix = null;
            String oldDir = dirPrefix;
            dirPrefix = "";
            if (queryAnchorOffset > -1 && caretOffset > queryAnchorOffset) {
                try {
                    String typedText = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                    if (isValidIncludeNamePart(typedText)) {
                        filterPrefix = typedText;
                    }
                } catch (BadLocationException e) {
                    // filterPrefix stays null -> no filtering
                }
            }
            fixFilter();
            if (TRACE) {
                System.err.println("canFilter INCINFO: usrInclude=" + usrInclude + // NOI18N
                        " anchorOffset=" + queryAnchorOffset + " oldDir=" + oldDir + // NOI18N
                        " dirPrefix=" + dirPrefix + " filterPrefix=" + filterPrefix); // NOI18N
            }
            return (filterPrefix != null) && oldDir.equals(dirPrefix);
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(resultSetAnchorOffset);
                Collection<? extends CsmIncludeCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(true);
            resultSet.finish();
        }

        private void fixFilter() {
            String pref = filterPrefix;
            if (pref != null) {
                int origSlash = pref.lastIndexOf(CsmIncludeCompletionItem.SLASH);
                resultSetAnchorOffset = queryAnchorOffset;
                if (origSlash != -1) {
                    resultSetAnchorOffset += origSlash + 1;
                }
                pref = trimIncludeSigns(pref);
                int slash = pref.lastIndexOf(CsmIncludeCompletionItem.SLASH);
                if (slash != -1) {
                    dirPrefix = pref.substring(0, slash + 1);
                    pref = pref.substring(slash + 1);
                }
                filterPrefix = pref;
            }
        }

        private Collection<CsmIncludeCompletionItem> getItems(BaseDocument doc, int caretOffset) {
            Collection<CsmIncludeCompletionItem> items = Collections.<CsmIncludeCompletionItem>emptyList();
            try {
                if (init(doc, caretOffset)) {
                    CsmIncludeCompletionQuery query = new CsmIncludeCompletionQuery(null);
                    items = query.query(doc, dirPrefix, queryAnchorOffset, queryAnchorOffsetDelta, usrInclude, showAll);
                }
            } catch (BadLocationException ex) {
            }
            return items;
        }

        private boolean init(final BaseDocument doc, final int caretOffset) throws BadLocationException {
            resultSetAnchorOffset = caretOffset;
            filterPrefix = null;
            queryAnchorOffset = -1;
            usrInclude = null;
            dirPrefix = "";
            if (doc != null) {
                doc.readLock();
                try {
                    TokenItem<TokenId> tok = CndTokenUtilities.getTokenCheckPrev(doc, caretOffset);
                    if (tok != null) {
                        TokenId id = tok.id();
                        if(id instanceof CppTokenId) {
                            switch ((CppTokenId)id) {
                                case PREPROCESSOR_SYS_INCLUDE:
                                    usrInclude = Boolean.FALSE;
                                    queryAnchorOffset = tok.offset();
                                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                    break;
                                case PREPROCESSOR_USER_INCLUDE:
                                    usrInclude = Boolean.TRUE;
                                    queryAnchorOffset = tok.offset();
                                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                    if (tok.partType() == PartType.COMPLETE || tok.partType() == PartType.START) {
                                        queryAnchorOffset++;
                                        queryAnchorOffsetDelta = -1; // old queryAnchorOffset - new queryAnchorOffset
                                    }
                                    break;
                                case PREPROCESSOR_IDENTIFIER:
                                    usrInclude = Boolean.TRUE;
                                    queryAnchorOffset = tok.offset();
                                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                    break;
                            }
                        }
                        if (queryAnchorOffset < 0 && CppTokenId.WHITESPACE_CATEGORY.equals(tok.id().primaryCategory())) { // not inside "" or <>
                            tok = CndTokenUtilities.getFirstNonWhiteBwd(doc, caretOffset);
                            if (tok != null) {
                                TokenId id2 = tok.id();
                                if(id2 instanceof CppTokenId) {
                                    switch ((CppTokenId)id2) {
                                        case PREPROCESSOR_INCLUDE:
                                        case PREPROCESSOR_INCLUDE_NEXT:
                                            // after #include or #include_next => init query offset
                                            usrInclude = null;
                                            queryAnchorOffset = caretOffset;
                                            filterPrefix = null;
                                            break;
                                    }
                                }
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    // skip
                } finally {
                    doc.readUnlock();
                }
            }
            fixFilter();
            if (TRACE) {
                System.err.println("INCINFO: usrInclude=" + usrInclude + // NOI18N
                        " anchorOffset=" + queryAnchorOffset + // NOI18N
                        " dirPrefix=" + dirPrefix + " filterPrefix=" + filterPrefix); // NOI18N
            }
            return this.queryAnchorOffset > 0;
        }

        private boolean isCaseSensitive(JTextComponent component) {
            return CsmCompletionUtils.isCaseSensitive(CsmCompletionUtils.getMimeType(component));
        }

        private String trimIncludeSigns(String str) {
            if (str.startsWith(CsmIncludeCompletionItem.QUOTE) ||
                    str.startsWith(CsmIncludeCompletionItem.SYS_OPEN)) {
                str = str.substring(1);
            }
            if (str.endsWith(CsmIncludeCompletionItem.QUOTE) ||
                    str.endsWith(CsmIncludeCompletionItem.SYS_CLOSE)) {
                str = str.substring(0, str.length() - 1);
            }
            return str;
        }

        private boolean isValidIncludeNamePart(String text) {
            if (text == null || text.length() == 0) {
                // nothing
                return true;
            }
            if (text.length() == 1) {
                if (text.startsWith(CsmIncludeCompletionItem.QUOTE)) {
                    // opening "
                    return true;
                } else if (text.startsWith(CsmIncludeCompletionItem.SYS_OPEN)) {
                    // opening <, need to recalc data if not system was calculated before
                    return usrInclude == Boolean.FALSE;
                }
            }
            if (text.endsWith(CsmIncludeCompletionItem.QUOTE) ||
                    text.endsWith(CsmIncludeCompletionItem.SYS_CLOSE)) {
                // after include string
                return false;
            } else {
                return true;
            }
        }

        private Collection<CsmIncludeCompletionItem> getFilteredData(Collection<CsmIncludeCompletionItem> data, String prefix) {
            Collection<CsmIncludeCompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CsmIncludeCompletionItem> ret = new ArrayList<CsmIncludeCompletionItem>(data.size());
                for (CsmIncludeCompletionItem itm : data) {
                    if (matchPrefix(itm, prefix)) {
                        ret.add(itm);
                    }
                }
                out = ret;
            }
            return out;
        }

        private boolean matchPrefix(CsmIncludeCompletionItem itm, String prefix) {
            String item = itm.getItemText();
            if (!this.caseSensitive) {
                item = item.toLowerCase(Locale.getDefault());
                prefix = prefix.toLowerCase(Locale.getDefault());
            }
            return item.startsWith(prefix);
        }
    }
}
