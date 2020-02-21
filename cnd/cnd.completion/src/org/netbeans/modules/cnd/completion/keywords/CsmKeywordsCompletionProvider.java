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
package org.netbeans.modules.cnd.completion.keywords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_ALL_QUERY_TYPE;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 *
 */
public class CsmKeywordsCompletionProvider implements CompletionProvider {
    private static final boolean TRACE  = false;

    public CsmKeywordsCompletionProvider() {
        // default constructor to be created as lookup service
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            int dot = component.getCaret().getDot();
            if (showKeywordCompletion(component.getDocument(), dot)) {
                return new AsyncCompletionTask(new Query(dot, queryType), component);
            }
        }
        return null;
    }

    private boolean showKeywordCompletion(final Document doc, final int offset) {
        final AtomicBoolean out = new AtomicBoolean(false);
        doc.render(new Runnable() {

            @Override
            public void run() {
                out.set(showKeywordCompletionImpl(doc, offset));
            }
        });
        return out.get();
    }

    private boolean showKeywordCompletionImpl(Document doc, int offset) {
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, false, true);
        if (ts == null) {
            return false;
        }
        if (ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<TokenId> embedded = (TokenSequence<TokenId>) ts.embedded();
            if (CndTokenUtilities.moveToPreprocKeyword(embedded)) {
                TokenId id = embedded.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case PREPROCESSOR_DEFINE:
                            return (embedded.offset() + embedded.token().length()) < offset;
                        default:
                            return false;
                    }
                }
            }
        } else {
            if (CndTokenUtilities.shiftToNonWhite(ts, true)) {
                TokenId id = ts.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case NAMESPACE:
                            // next name
                            return false;
                        case GOTO:
                            // next label
                            return false;
                        case NEW:
                        case SCOPE:
                        case ARROW:
                        case ARROWMBR:
                        case DOT:
                        case DOTMBR:
                            // auto completion
                            return false;
                        case IF:
                        case WHILE:
                        case FOR:
                        case SWITCH:
                        case CATCH:
                        case ALIGNAS:
                        case ALIGNOF:
                        case DECLTYPE:
                            // next "("
                            return false;
                        case STATIC_CAST:
                        case CONST_CAST:
                        case REINTERPRET_CAST:
                        case TEMPLATE:
                            // next "<"
                            return false;
                        default:
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private static final List<String> predefinedVariables;
    private static final List<CppTokenId> keywordsAll;
    private static final List<CppTokenId> keywordsFirst;

    static {
        predefinedVariables = Arrays.asList(
            "__VA_ARGS__" //NOI18N
        );
        keywordsFirst = Arrays.asList(
            CppTokenId.ALIGNOF,
//            CppTokenId.ASM,
//            CppTokenId.AUTO,
//            CppTokenId.BIT,
//            CppTokenId.BOOL,
//            CppTokenId.BREAK,
//            CppTokenId.CASE,
//            CppTokenId.CATCH,
//            CppTokenId.CHAR,
//            CppTokenId.CLASS,
//            CppTokenId.CONST,
            CppTokenId.CONST_CAST,
            CppTokenId.CONTINUE,
            CppTokenId.DEFAULT,
            CppTokenId.DELETE,
            CppTokenId.DOUBLE,
            CppTokenId.DYNAMIC_CAST,
//            CppTokenId.ELSE,
//            CppTokenId.ENUM,
            CppTokenId.EXPLICIT,
            CppTokenId.EXPORT,
            CppTokenId.EXTERN,
            CppTokenId.FINALLY,
//            CppTokenId.FLOAT,
//            CppTokenId.FOR,
            CppTokenId.FRIEND,
//            CppTokenId.GOTO,
            CppTokenId.INLINE,
//            CppTokenId.INT,
//            CppTokenId.LONG,
            CppTokenId.MUTABLE,
            CppTokenId.NAMESPACE,
//            CppTokenId.NEW,
            CppTokenId.OPERATOR,
//            CppTokenId.PASCAL,
            CppTokenId.PRIVATE,
            CppTokenId.PROTECTED,
            CppTokenId.PUBLIC,
            CppTokenId.REGISTER,
            CppTokenId.REINTERPRET_CAST,
            CppTokenId.RESTRICT,
            CppTokenId.RETURN,
//            CppTokenId.SHORT,
            CppTokenId.SIGNED,
            CppTokenId.SIZEOF,
            CppTokenId.STATIC,
            CppTokenId.STATIC_CAST,
            CppTokenId.STRUCT,
            CppTokenId.SWITCH,
            CppTokenId.TEMPLATE,
//            CppTokenId.THIS,
//            CppTokenId.THROW,
//            CppTokenId.TRY,
            CppTokenId.TYPEDEF,
            CppTokenId.TYPEID,
            CppTokenId.TYPENAME,
            CppTokenId.TYPEOF,
//            CppTokenId.UNION,
            CppTokenId.UNSIGNED,
//            CppTokenId.USING,
            CppTokenId.VIRTUAL,
//            CppTokenId.VOID,
            CppTokenId.VOLATILE,
            CppTokenId.WCHAR_T,
//            CppTokenId.WHILE,
//            CppTokenId.FINAL,
            CppTokenId.OVERRIDE,
            CppTokenId.CONSTEXPR,
            CppTokenId.DECLTYPE,
            CppTokenId.NULLPTR,
            CppTokenId.THREAD_LOCAL,
            CppTokenId.STATIC_ASSERT,
            CppTokenId.ALIGNAS,
            CppTokenId.CHAR16_T,
            CppTokenId.CHAR32_T,
            CppTokenId.NOEXCEPT
        );
        keywordsAll = new ArrayList<CppTokenId>();
        for(CppTokenId token : CppTokenId.values()) {
            if (CppTokenId.KEYWORD_CATEGORY.equals(token.primaryCategory()) ||
                CppTokenId.KEYWORD_DIRECTIVE_CATEGORY.equals(token.primaryCategory())) {
                final String text = token.fixedText();
                if (text != null && text.length() > 2) {
                    keywordsAll.add(token);
                }
            }
        }
    }

    private static final class Query extends AsyncCompletionQuery {

        private Collection<CsmKeywordCompletionItem> results;
        private int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;
        private final int queryType;
        private boolean caseSensitive = false;
        private boolean inDefine = false;

        /*package*/ Query(int caretOffset, int queryType) {
            if(TRACE)System.err.println("KW Query("+caretOffset+","+queryType+")");
            this.queryType = queryType;
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            String mimeType = CsmCompletionUtils.getMimeType(component);
            caseSensitive = mimeType != null ? CsmCompletionUtils.isCaseSensitive(mimeType) : false;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if(TRACE)System.err.println("KW query("+caretOffset+")");
            Collection<CsmKeywordCompletionItem> items = getItems((BaseDocument) doc, caretOffset);
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
            if(TRACE)System.err.println("KW canFilter()");
            int caretOffset = component.getCaretPosition();
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= queryAnchorOffset) {
                Document doc = component.getDocument();
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                    if (results == null || !isCppIdentifierPart(filterPrefix)) {
                        filterPrefix = null;
                    }
                } catch (BadLocationException ex) {
                }
            }
            return filterPrefix != null;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if(TRACE)System.err.println("KW filter()");
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CsmKeywordCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(false);
            resultSet.finish();
        }

        private Filter<CppTokenId> getLanguageFilter(BaseDocument doc) {
            Language<?> language = (Language<?>) doc.getProperty(Language.class);
            if (language != null) {
                InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
                if (lexerAttrs != null) {
                    return (Filter<CppTokenId>) lexerAttrs.getValue(LanguagePath.get(language), CndLexerUtilities.LEXER_FILTER);
                }
            }
            return null;
        }

        private Collection<CsmKeywordCompletionItem> getItems(final BaseDocument doc, final int caretOffset) {
            Collection<CsmKeywordCompletionItem> items = new ArrayList<CsmKeywordCompletionItem>();
            try {
                if (init(doc, caretOffset)) {
                    Filter<CppTokenId> languageFilter = getLanguageFilter(doc);
                    for (CppTokenId id : keywordsAll) {
                        if (languageFilter != null) {
                            if (languageFilter.check(id.fixedText()) == null) {
                                continue;
                            }
                        }
                        items.add(CsmKeywordCompletionItem.createItem(queryAnchorOffset, caretOffset, id.fixedText(), keywordsFirst.contains(id)));
                    }
                    if (inDefine) {
                        for (String id : predefinedVariables) {
                            items.add(CsmKeywordCompletionItem.createItem(queryAnchorOffset, caretOffset, id, false));
                        }
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
            doc.readLock();
            try {
                TokenSequence<TokenId> ppTs = CndLexerUtilities.getCppTokenSequence(doc, caretOffset, true, true);
                if (ppTs == null || ppTs.token() == null) {
                    return false;
                }
                final TokenId id = ppTs.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case DOXYGEN_COMMENT:
                        case BLOCK_COMMENT:
                        case NEW_LINE:
                        case WHITESPACE:
                            // use caret offset
                            queryAnchorOffset = caretOffset;
                            break;
                        default:
                            // use start of token
                            if (isCppIdentifierPart(ppTs.token().text().toString())) {
                                queryAnchorOffset = ppTs.offset();
                            } else {
                                queryAnchorOffset = caretOffset;
                            }
                            inDefine =ppTs.token().id() == CppTokenId.PREPROCESSOR_IDENTIFIER;
                            break;
                    }
                }
                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
            } catch (BadLocationException ex) {
                // skip
            } finally {
                doc.readUnlock();
            }
            if(TRACE)System.err.println("KW init("+caretOffset+")->"+filterPrefix);
            return this.queryAnchorOffset >= 0;
        }

        private boolean isCppIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(CndLexerUtilities.isCppIdentifierPart(text.charAt(i)))) {
                    return false;
                }
            }
            return true;
        }

        private Collection<CsmKeywordCompletionItem> getFilteredData(Collection<CsmKeywordCompletionItem> data, String prefix) {
            Collection<CsmKeywordCompletionItem> out;
            if (prefix == null || prefix.isEmpty()) {
                if (queryType == COMPLETION_ALL_QUERY_TYPE) {
                    out = data;
                } else {
                    out = new ArrayList<CsmKeywordCompletionItem>(data.size());
                    for (CsmKeywordCompletionItem itm : data) {
                        if (itm.isFistCompletion()) {
                            out.add(itm);
                        }
                    }
                }
            } else {
                out = new ArrayList<CsmKeywordCompletionItem>(data.size());
                for (CsmKeywordCompletionItem itm : data) {
                    if (matchPrefix(itm.getItemText(), prefix, caseSensitive)) {
                        out.add(itm);
                    }
                }
            }
            return out;
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
    }
}
