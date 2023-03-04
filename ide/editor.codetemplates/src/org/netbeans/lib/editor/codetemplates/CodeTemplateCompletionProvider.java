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

package org.netbeans.lib.editor.codetemplates;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 * Implemenation of the code template description.
 *
 * @author Miloslav Metelka
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 300) //NOI18N
public final class CodeTemplateCompletionProvider implements CompletionProvider {

    public CompletionTask createTask(int type, JTextComponent component) {
        return (type & COMPLETION_QUERY_TYPE) == 0 || isAbbrevDisabled(component) ? null : new AsyncCompletionTask(new Query(), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static boolean isAbbrevDisabled(JTextComponent component) {
        return org.netbeans.editor.Abbrev.isAbbrevDisabled(component);
    }

    @MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class)
    public static class Collector implements CompletionCollector {

        private static final String SELECTED_TEXT_VAR = "${0:$TM_SELECTED_TEXT}";
        private static final Pattern SNIPPET_VAR_PATTERN = Pattern.compile("\\$\\{\\s*([-\\w]++)((?:\\s*[-\\w]++(?:\\s*=\\s*(?:\\\"[^\\\"]*\\\"|[-\\w]++))?)*\\s*)?}");
        private static final Pattern SNIPPET_HINT_PATTERN = Pattern.compile("([-\\w]++)(?:\\s*=\\s*(\\\"([^\\\"]*)\\\"|[-\\w]++))?");
        private static final String UNCAUGHT_EXCEPTION_CATCH_STATEMENTS = "uncaughtExceptionCatchStatements";

        @Override
        public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
            new Query().query(doc, offset, (ct, abbrevBased) -> {
                String description = ct.getDescription();
                if (description == null) {
                    description = CodeTemplateApiPackageAccessor.get().getSingleLineText(ct);
                }
                String label = html2text(description.trim());
                String sortText = String.format("%04d%s", 1650, "fore".equals(ct.getAbbreviation()) ? label.substring(0, 3) : label);
                consumer.accept(CompletionCollector.newBuilder(label)
                        .sortText(sortText)
                        .documentation(() -> {
                            StringBuffer sb = new StringBuffer("<html><pre>");
                            ParametrizedTextParser.parseToHtml(sb, ct.getParametrizedText());
                            sb.append("</pre>");
                            return sb.toString();
                        }).insertText(convert(ct.getParametrizedText()))
                        .insertTextFormat(Completion.TextFormat.Snippet)
                        .build());
            });
            return true;
        }

        private static String convert(String s) {
            StringBuilder sb = new StringBuilder();
            Matcher matcher = SNIPPET_VAR_PATTERN.matcher(s);
            int idx = 0;
            Map<String, Integer> placeholders = new HashMap<>();
            Map<String, String> values = new HashMap<>();
            Set<String> nonEditables = new HashSet<>();
            AtomicInteger last = new AtomicInteger();
            while (matcher.find(idx)) {
                int start = matcher.start();
                sb.append(s.substring(idx, start));
                String name = matcher.group(1);
                switch (name) {
                    case CodeTemplateParameter.CURSOR_PARAMETER_NAME:
                        sb.append("$0");
                        break;
                    case CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME:
                    case CodeTemplateParameter.NO_INDENT_PARAMETER_NAME:
                        break;
                    case CodeTemplateParameter.SELECTION_PARAMETER_NAME:
                        sb.append(SELECTED_TEXT_VAR);
                        break;
                    default:
                        String params = matcher.groupCount() > 1 ? matcher.group(2) : null;
                        if (params == null || params.isEmpty()) {
                            if (nonEditables.contains(name)) {
                                sb.append(values.getOrDefault(name, ""));
                            } else {
                                sb.append('$').append(placeholders.computeIfAbsent(name, n -> last.incrementAndGet()));
                            }
                        } else {
                            Map<String, String> hints = getHints(params);
                            String defaultValue = hints.get(CodeTemplateParameter.DEFAULT_VALUE_HINT_NAME);
                            if (defaultValue != null) {
                                values.put(name, defaultValue);
                            }
                            if (hints.containsKey(UNCAUGHT_EXCEPTION_CATCH_STATEMENTS)) {
                                sb.append("catch (${").append(last.incrementAndGet()).append(":Exception} ${").append(last.incrementAndGet()).append(":e}) {\n}");
                            } else if ("false".equalsIgnoreCase(hints.get(CodeTemplateParameter.EDITABLE_HINT_NAME))) {
                                nonEditables.add(name);
                                sb.append(values.getOrDefault(name, ""));
                            } else {
                                if (defaultValue != null) {
                                    sb.append("${").append(placeholders.computeIfAbsent(name, n -> last.incrementAndGet())).append(':').append(defaultValue).append('}');
                                } else {
                                    sb.append('$').append(placeholders.computeIfAbsent(name, n -> last.incrementAndGet()));
                                }
                            }
                        }
                }
                idx = matcher.end();
            }
            String tail = s.substring(idx);
            return sb.append(tail.endsWith("\n") ? tail.substring(0, tail.length() - 1) : tail).toString();
        }

        private static Map<String, String> getHints(String text) {
            Map<String, String> hint2Values = new HashMap<>();
            Matcher matcher = SNIPPET_HINT_PATTERN.matcher(text);
            int idx = 0;
            while (matcher.find(idx)) {
                String insideString = matcher.groupCount() > 2 ? matcher.group(3) : null;
                String value = matcher.groupCount() > 1 ? matcher.group(2) : null;
                hint2Values.put(matcher.group(1), insideString!= null ? insideString : value);
                idx = matcher.end();
            }
            return hint2Values;
        }

        private static String html2text(String html) {
            try {
                StringBuilder sb = new StringBuilder();
                new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
                    @Override
                    public void handleText(char[] text, int pos) {
                        sb.append(text);
                    }
                }, Boolean.TRUE);
                return sb.toString();
            } catch (IOException ex) {
                return html;
            }
        }
    }

    private static final class Query extends AsyncCompletionQuery
    implements ChangeListener {

        private JTextComponent component;
        
        private int queryCaretOffset;
        private int queryAnchorOffset;
        private List<CodeTemplateCompletionItem> queryResult;
        
        private String filterPrefix;
        
        protected @Override void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected @Override boolean canFilter(JTextComponent component) {
            if (component.getCaret() == null) {
                return false;
            }
            int caretOffset = component.getSelectionStart();
            Document doc = component.getDocument();
            filterPrefix = null;
            if (caretOffset >= queryCaretOffset) {
                if (queryAnchorOffset < queryCaretOffset) {
                    try {
                        filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                        if (!isJavaIdentifierPart(filterPrefix)) {
                            filterPrefix = null;
                        }
                    } catch (BadLocationException e) {
                        // filterPrefix stays null -> no filtering
                    }
                }
            }
            return (filterPrefix != null);
        }
        
        protected @Override void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && queryResult != null) {
                resultSet.addAllItems(getFilteredData(queryResult, filterPrefix));
            }
            resultSet.finish();
        }
        
        private boolean isJavaIdentifierPart(CharSequence text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection<? extends CompletionItem> getFilteredData(
            Collection<? extends CompletionItem> data, 
            String prefix
        ) {
            List<CompletionItem> ret = new ArrayList<CompletionItem>();
            for (CompletionItem itm : data) {
                if (itm.getInsertPrefix().toString().startsWith(prefix)) {
                    ret.add(itm);
                }
            }
            return ret;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            query(doc, caretOffset, (ct, abbrevBased) -> {
                if (queryResult != null) {
                    queryResult.add(new CodeTemplateCompletionItem(ct, abbrevBased));
                }
            });
            if (queryResult != null) {
                resultSet.addAllItems(queryResult);
            }
            resultSet.setAnchorOffset(queryAnchorOffset);
            resultSet.finish();
        }

        private void query(Document doc, int caretOffset, BiConsumer<CodeTemplate, Boolean> consumer) {
            String langPath = null;
            String identifierBeforeCursor = null;
            if (doc instanceof AbstractDocument) {
                AbstractDocument adoc = (AbstractDocument)doc;
                adoc.readLock();
                try {
                    try {
                        if (adoc instanceof BaseDocument) {
                            identifierBeforeCursor = Utilities.getIdentifierBefore((BaseDocument)adoc, caretOffset);
                        }
                    } catch (BadLocationException e) {
                        // leave identifierBeforeCursor null
                    }
                    List<TokenSequence<?>> list = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, true);
                    if (list.size() > 1) {
                        langPath = list.get(list.size() - 1).languagePath().mimePath();
                    }
                } finally {
                    adoc.readUnlock();
                }
            }
            
            if (identifierBeforeCursor == null) {
                identifierBeforeCursor = ""; //NOI18N
            }
            
            if (langPath == null) {
                langPath = NbEditorUtilities.getMimeType(doc);
            }

            queryCaretOffset = caretOffset;
            queryAnchorOffset = caretOffset - identifierBeforeCursor.length();
            if (langPath != null) {
                String mimeType = DocumentUtilities.getMimeType(doc);
                MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.get(mimeType);
                Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                boolean ignoreCase = prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
                
                CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(MimePath.parse(langPath));
                op.waitLoaded();
                
                Collection<? extends CodeTemplate> ctsPT = op.findByParametrizedText(identifierBeforeCursor, ignoreCase);
                Collection<? extends CodeTemplate> ctsAb = op.findByAbbreviationPrefix(identifierBeforeCursor, ignoreCase);
                Collection<? extends CodeTemplateFilter> filters = CodeTemplateManagerOperation.getTemplateFilters(doc, queryAnchorOffset, queryAnchorOffset);
                
                queryResult = new ArrayList<CodeTemplateCompletionItem>(ctsPT.size() + ctsAb.size());
                Set<String> abbrevs = new HashSet<String>(ctsPT.size() + ctsAb.size());
                for (CodeTemplate ct : ctsPT) {
                    if (ct.getContexts() != null && ct.getContexts().size() > 0 && accept(ct, filters) && abbrevs.add(ct.getAbbreviation())) {
                        consumer.accept(ct, false);
                    }
                }
                for (CodeTemplate ct : ctsAb) {
                    if (ct.getContexts() != null && ct.getContexts().size() > 0 && accept(ct, filters) && abbrevs.add(ct.getAbbreviation())) {
                        consumer.accept(ct, true);
                    }
                }
            }
        }

        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                notify();
            }
        }
        
        private static boolean accept(CodeTemplate template, Collection/*<CodeTemplateFilter>*/ filters) {
            for(Iterator<CodeTemplateFilter> it = filters.iterator(); it.hasNext();) {
                CodeTemplateFilter filter = it.next();
                if (!filter.accept(template))
                    return false;                
            }
            return true;
        }
        
    }

}
