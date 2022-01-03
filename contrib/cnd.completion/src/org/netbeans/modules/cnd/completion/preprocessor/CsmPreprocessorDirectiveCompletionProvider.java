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
package org.netbeans.modules.cnd.completion.preprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
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
public class CsmPreprocessorDirectiveCompletionProvider implements CompletionProvider {

    public CsmPreprocessorDirectiveCompletionProvider() {
        // default constructor to be created as lookup service
    }

    private final static boolean TRACE = Boolean.getBoolean("cnd.completion.preproc.trace");

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (TRACE) {
            System.err.println("typed text " + typedText);
        }
        CompletionSupport sup = CompletionSupport.get(component);
        if (sup == null) {
            return 0;
        }
        int dot = component.getCaretPosition();
        if (CompletionSupport.isPreprocessorDirectiveCompletionEnabled(component.getDocument(), dot)) {
            if (TRACE) {
                System.err.println("preprocessor completion will be shown on " + dot); // NOI18N
            }
            return COMPLETION_QUERY_TYPE;
        } else {
            if (TRACE) {
                System.err.println("preprocessor completion will NOT be shown on " + dot); // NOI18N
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
            int dot = component.getCaret().getDot();
            if (CompletionSupport.isPreprocessorDirectiveCompletionEnabled(component.getDocument(), dot)) {
                if (TRACE) {
                    System.err.println("preprocessor completion task is created with offset " + dot); // NOI18N
                }
                return new AsyncCompletionTask(new Query(dot), component);
            } else {
                if (TRACE) {
                    System.err.println("preprocessor completion task is NOT created on " + dot); // NOI18N
                }
            }
        }
        return null;
    }

    // method for tests
    /*package*/ static Collection<CsmPreprocessorDirectiveCompletionItem> getFilteredData(BaseDocument doc, int caretOffset, int queryType) {
        Query query = new Query(caretOffset);
        Collection<CsmPreprocessorDirectiveCompletionItem> items = query.getItems(doc, caretOffset);
        if (TRACE) {
            System.err.println("Completion Items " + items.size());
            for (CsmPreprocessorDirectiveCompletionItem completionItem : items) {
                System.err.println(completionItem.toString());
            }
        }
        return items;
    }

    private static final String[] keywords = new String[] {
        "define", // NOI18N
        "elif", // NOI18N
        "else", // NOI18N
        "endif", // NOI18N
        "error", // NOI18N
        "ident", // NOI18N
        "if", // NOI18N
        "ifdef", // NOI18N
        "ifndef", // NOI18N
        "include", // NOI18N
        "include_next", // NOI18N
        "line", // NOI18N
        "pragma", // NOI18N
        "undef", // NOI18N
        "warning", // NOI18N
        "#if\n#endif", // NOI18N
        "#if\n#else\n#endif", // NOI18N
        "#ifdef\n#endif", // NOI18N
        "#ifdef\n#else\n#endif", // NOI18N
        "#ifndef\n#endif", // NOI18N
        "#ifndef\n#else\n#endif", // NOI18N
    };
    private static final class Query extends AsyncCompletionQuery {

        private Collection<CsmPreprocessorDirectiveCompletionItem> results;
        private int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;

        /*package*/ Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (TRACE) {
                System.err.println("query on " + caretOffset + " anchor " + queryAnchorOffset); // NOI18N
            }
            Collection<CsmPreprocessorDirectiveCompletionItem> items = getItems((BaseDocument) doc, caretOffset);
            if (this.queryAnchorOffset > 0) {
                if (!items.isEmpty()) {
                    this.results = items;
                    items = getFilteredData(items, this.filterPrefix);
                    resultSet.estimateItems(items.size(), -1);
                    resultSet.addAllItems(items);
                    resultSet.setAnchorOffset(queryAnchorOffset);
                }
                resultSet.setHasAdditionalItems(false);
            }
            resultSet.finish();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            if (TRACE) {
                System.err.println("canFilter on " + caretOffset + " anchor " + queryAnchorOffset); // NOI18N
            }
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= queryAnchorOffset) {
                Document doc = component.getDocument();
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                } catch (BadLocationException ex) {
                    Completion.get().hideCompletion();
                }
            } else {
                Completion.get().hideCompletion();
            }
            return filterPrefix != null;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CsmPreprocessorDirectiveCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(false);
            resultSet.finish();
        }

        private Collection<CsmPreprocessorDirectiveCompletionItem> getItems(final BaseDocument doc, final int caretOffset) {
            Collection<CsmPreprocessorDirectiveCompletionItem> items = new ArrayList<CsmPreprocessorDirectiveCompletionItem>();
            try {
                if (init(doc, caretOffset)) {
                    for (String string : keywords) {
                        items.add(CsmPreprocessorDirectiveCompletionItem.createItem(queryAnchorOffset, caretOffset, string));
                    }
                }
            } catch (BadLocationException ex) {
                // no completion
            }
            return items;
        }

        private boolean init(final BaseDocument doc, final int caretOffset) throws BadLocationException {
            filterPrefix = "";
            queryAnchorOffset = -1;
            if (CompletionSupport.isPreprocessorDirectiveCompletionEnabled(doc, caretOffset)) {
                doc.readLock();
                try {
                    TokenSequence<TokenId> ppTs = CndLexerUtilities.getCppTokenSequence(doc, caretOffset, true, true);
                    if (ppTs == null || ppTs.token() == null) {
                        return false;
                    }
                    final TokenId id = ppTs.token().id();
                    if(id instanceof CppTokenId) {
                        switch ((CppTokenId)id) {
                            case WHITESPACE:
                            case PREPROCESSOR_START:
                            case PREPROCESSOR_START_ALT:
                                // use caret offset
                                queryAnchorOffset = caretOffset;
                                break;
                            default:
                                // use start of token
                                queryAnchorOffset = ppTs.offset();
                                break;
                        }
                    }
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                } catch (BadLocationException ex) {
                    // skip
                } finally {
                    doc.readUnlock();
                }
            }
            if (TRACE) {
                System.err.println(" anchorOffset=" + queryAnchorOffset + // NOI18N
                        " filterPrefix=" + filterPrefix); // NOI18N
            }
            return this.queryAnchorOffset >= 0;
        }

        private Collection<CsmPreprocessorDirectiveCompletionItem> getFilteredData(Collection<CsmPreprocessorDirectiveCompletionItem> data, String prefix) {
            Collection<CsmPreprocessorDirectiveCompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CsmPreprocessorDirectiveCompletionItem> ret = new ArrayList<CsmPreprocessorDirectiveCompletionItem>(data.size());
                for (CsmPreprocessorDirectiveCompletionItem itm : data) {
                    if (matchPrefix(itm, prefix)) {
                        ret.add(itm);
                    }
                }
                out = ret;
            }
            return out;
        }

        private boolean matchPrefix(CsmPreprocessorDirectiveCompletionItem itm, String prefix) {
            return itm.getItemText().startsWith(prefix);
        }
    }
}
