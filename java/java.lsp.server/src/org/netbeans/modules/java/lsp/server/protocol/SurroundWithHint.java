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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 80)
public final class SurroundWithHint extends CodeActionsProvider {

    private static final String COMMAND_INSERT_SNIPPET = "editor.action.insertSnippet";
    private static final String DOTS = "...";
    private static final String SNIPPET = "snippet";
    private static final String SELECTION_VAR = "${selection}";
    private static final String SELECTED_TEXT_VAR = "${0:$TM_SELECTED_TEXT}";
    private static final Pattern SNIPPET_VAR_PATTERN = Pattern.compile("\\$\\{\\s*([-\\w]++)([^}]*)?}");
    private static final Pattern SNIPPET_HINT_PATTERN = Pattern.compile("([-\\w]++)(?:\\s*=\\s*(\\\"([^\\\"]*)\\\"|\\S*))?");
    private static final Set<String> TO_FILTER = Collections.singleton("fcom");

    @Override
    @NbBundle.Messages({
        "DN_SurroundWith=Surround with \"{0}\"",
    })
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        CompilationController info = CompilationController.get(resultIterator.getParserResult());
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        int startOffset = getOffset(info, params.getRange().getStart());
        int endOffset = getOffset(info, params.getRange().getEnd());
        if (startOffset >= endOffset) {
            return Collections.emptyList();
        }
        Document doc;
        try {
            doc = info.getDocument();
        } catch (IOException ex) {
            return Collections.emptyList();
        }
        Collection<? extends CodeTemplateFilter> filters = getTemplateFilters(doc, startOffset, endOffset);
        List<CodeAction> codeActions = new ArrayList<>();
        for (CodeTemplate codeTemplate : CodeTemplateManager.get(doc).getCodeTemplates()) {
            String parametrizedText = codeTemplate.getParametrizedText();
            if (parametrizedText.toLowerCase().contains(SELECTION_VAR) && !TO_FILTER.contains(codeTemplate.getAbbreviation())) {
                if (codeTemplate.getContexts() == null || codeTemplate.getContexts().isEmpty() || accept(codeTemplate, filters)) {
                    String label = html2text(codeTemplate.getDescription());
                    if (label == null) {
                        String text = codeTemplate.getParametrizedText();
                        int idx = text.indexOf('\n');
                        label = convert(idx < 0 ? text : text.substring(0, idx) + DOTS, true);
                    }
                    String snippet = convert(codeTemplate.getParametrizedText(), false);
                    codeActions.add(createCodeAction(Bundle.DN_SurroundWith(label), CodeActionKind.RefactorRewrite, COMMAND_INSERT_SNIPPET, Collections.singletonMap(SNIPPET, snippet)));
                }
            }
        }
        return codeActions;
    }

    @Override
    public Set<String> getCommands() {
        return Collections.emptySet();
    }

    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        return CompletableFuture.completedFuture(false);
    }

    private static Collection<? extends CodeTemplateFilter> getTemplateFilters(Document doc, int startOffset, int endOffset) {
        String mimeType = DocumentUtilities.getMimeType(doc);
        Collection<? extends CodeTemplateFilter.Factory> filterFactories = MimeLookup.getLookup(mimeType).lookupAll(CodeTemplateFilter.Factory.class);
        List<CodeTemplateFilter> result = new ArrayList<>(filterFactories.size());
        for (CodeTemplateFilter.Factory factory : filterFactories) {
            result.add(factory.createFilter(doc, startOffset, endOffset));
        }
        return result;
    }

    private static boolean accept(CodeTemplate template, Collection<? extends CodeTemplateFilter> filters) {
        for(CodeTemplateFilter filter : filters) {
            if (!filter.accept(template)) {
                return false;
            }
        }
        return true;
    }

    private static String convert(String s, boolean label) {
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
                case CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME:
                case CodeTemplateParameter.NO_INDENT_PARAMETER_NAME:
                    break;
                case CodeTemplateParameter.SELECTION_PARAMETER_NAME:
                    sb.append(label ? DOTS : SELECTED_TEXT_VAR);
                    break;
                default:
                    String params = matcher.groupCount() > 1 ? matcher.group(2) : null;
                    if (params == null || params.isEmpty()) {
                        if (label || nonEditables.contains(name)) {
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
                        if ("false".equalsIgnoreCase(hints.get(CodeTemplateParameter.EDITABLE_HINT_NAME))) {
                            nonEditables.add(name);
                            sb.append(values.getOrDefault(name, ""));
                        } else {
                            if (defaultValue != null) {
                                if (label) {
                                    sb.append(defaultValue);
                                } else {
                                    sb.append("${").append(placeholders.computeIfAbsent(name, n -> last.incrementAndGet())).append(':').append(defaultValue).append('}');
                                }
                            } else if (label) {
                                sb.append(values.getOrDefault(name, ""));
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
            hint2Values.put(matcher.group(1), matcher.groupCount() > 2 ? matcher.group(3) : matcher.groupCount() > 1 ? matcher.group(2) : null);
            idx = matcher.end();
        }
        return hint2Values;
    }

    private static String html2text(String html) throws IOException {
        if (html == null) {
            return html;
        }
        StringBuilder sb = new StringBuilder();
        new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
            @Override
            public void handleText(char[] text, int pos) {
                sb.append(text);
            }
        }, Boolean.TRUE);
        return sb.toString();
    }
}
