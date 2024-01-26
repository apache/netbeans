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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
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
    private static final String COMMAND_SURROUND_WITH = "nbls.surround.with";
    private static final String DOTS = "...";
    private static final String SNIPPET = "snippet";
    private static final String SELECTION_VAR = "${selection}";
    private static final String SELECTED_TEXT_VAR = "${0:$TM_SELECTED_TEXT}";
    private static final Pattern SNIPPET_VAR_PATTERN = Pattern.compile("\\$\\{\\s*([-\\w]++)((?:\\s*[-\\w]++(?:\\s*=\\s*(?:\\\"[^\\\"]*\\\"|[-\\w]++))?)*\\s*)?}");
    private static final Pattern SNIPPET_HINT_PATTERN = Pattern.compile("([-\\w]++)(?:\\s*=\\s*(\\\"([^\\\"]*)\\\"|[-\\w]++))?");
    private static final String UNCAUGHT_EXCEPTION_CATCH_STATEMENTS = "uncaughtExceptionCatchStatements";
    private static final Set<String> TO_FILTER = Collections.singleton("fcom");
    private static final Set<String> TO_SHOW = Collections.unmodifiableSet(new HashSet(Arrays.asList("bcom", "dowhile", "iff", "fore", "sy", "trycatch", "whilexp")));

    @Override
    @NbBundle.Messages({
        "DN_SurroundWith=Surround with {0}",
        "DN_SurroundWithAll=Surround with ...",
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.RESOLVED);
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
        List<QuickPickItem> items = new ArrayList<>();
        for (CodeTemplate codeTemplate : CodeTemplateManager.get(doc).getCodeTemplates()) {
            String parametrizedText = codeTemplate.getParametrizedText();
            if (parametrizedText.toLowerCase().contains(SELECTION_VAR) && !TO_FILTER.contains(codeTemplate.getAbbreviation())) {
                if (codeTemplate.getContexts() == null || codeTemplate.getContexts().isEmpty() || accept(codeTemplate, filters)) {
                    String text = convert(codeTemplate.getParametrizedText(), null);
                    String label = html2text(codeTemplate.getDescription());
                    if (label == null) {
                        int idx = text.indexOf('\n');
                        label = idx < 0 ? text : text.substring(0, idx) + DOTS;
                    }
                    StringBuilder sb = new StringBuilder();
                    AtomicInteger last = new AtomicInteger();
                    List<TextEdit> edits = updateSelection(info, startOffset, endOffset, text, sb, last);
                    String snippet = convert(codeTemplate.getParametrizedText(), last);
                    if (sb.length() > 0) {
                        snippet = sb.append(snippet).toString();
                    }
                    int idx = label.indexOf(' ');
                    CodeAction codeAction = createCodeAction(client, Bundle.DN_SurroundWith(idx < 0 ? label : label.substring(0, idx)), CodeActionKind.RefactorRewrite, null, COMMAND_INSERT_SNIPPET, Collections.singletonMap(SNIPPET, snippet));
                    if (!edits.isEmpty()) {
                        codeAction.setEdit(new WorkspaceEdit(Collections.singletonMap(params.getTextDocument().getUri(), edits)));
                    }
                    if (TO_SHOW.contains(codeTemplate.getAbbreviation())) {
                        codeActions.add(codeAction);
                    }
                    items.add(new QuickPickItem(label, null, text, false, codeAction));
                }
            }
        }
        if (items.size() > codeActions.size()) {
            codeActions.add(createCodeAction(client, Bundle.DN_SurroundWithAll(), CodeActionKind.RefactorRewrite, null, COMMAND_SURROUND_WITH, items));
        }
        return codeActions;
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

    private static String convert(String s, AtomicInteger last) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = SNIPPET_VAR_PATTERN.matcher(s);
        int idx = 0;
        Map<String, Integer> placeholders = new HashMap<>();
        Map<String, String> values = new HashMap<>();
        Set<String> nonEditables = new HashSet<>();
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
                    if (last == null) {
                        sb.append(' ');
                    } else {
                        sb.append(SELECTED_TEXT_VAR);
                    }
                    break;
                default:
                    String params = matcher.groupCount() > 1 ? matcher.group(2) : null;
                    if (params == null || params.isEmpty()) {
                        if (last == null || nonEditables.contains(name)) {
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
                            if (last == null) {
                                sb.append(defaultValue);
                            } else {
                                sb.append("catch (${").append(last.incrementAndGet()).append(":Exception} ${").append(last.incrementAndGet()).append(":e}) {\n}");
                            }
                        } else if ("false".equalsIgnoreCase(hints.get(CodeTemplateParameter.EDITABLE_HINT_NAME))) {
                            nonEditables.add(name);
                            sb.append(values.getOrDefault(name, ""));
                        } else {
                            if (defaultValue != null) {
                                if (last == null) {
                                    sb.append(defaultValue);
                                } else {
                                    sb.append("${").append(placeholders.computeIfAbsent(name, n -> last.incrementAndGet())).append(':').append(defaultValue).append('}');
                                }
                            } else if (last == null) {
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
            String insideString = matcher.groupCount() > 2 ? matcher.group(3) : null;
            String value = matcher.groupCount() > 1 ? matcher.group(2) : null;
            hint2Values.put(matcher.group(1), insideString!= null ? insideString : value);
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

    private static List<TextEdit> updateSelection(CompilationInfo info, int startOffset, int endOffset, String templateText, StringBuilder sb, AtomicInteger last) {
        List<TextEdit> edits = new ArrayList<>();
        TreeUtilities tu = info.getTreeUtilities();
        StatementTree stat = tu.parseStatement(templateText, null);
        EnumSet<Tree.Kind> kinds = EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.DO_WHILE_LOOP,
                Tree.Kind.ENHANCED_FOR_LOOP, Tree.Kind.FOR_LOOP, Tree.Kind.IF, Tree.Kind.SYNCHRONIZED,
                Tree.Kind.TRY, Tree.Kind.WHILE_LOOP);
        if (stat != null && kinds.contains(stat.getKind())) {
            TreePath treePath = tu.pathFor(startOffset);
            Tree tree = treePath.getLeaf();
            if (tree.getKind() == Tree.Kind.BLOCK && tree == tu.pathFor(endOffset).getLeaf()) {
                Trees trees = info.getTrees();
                SourcePositions sp = trees.getSourcePositions();
                Map<VariableElement, VariableTree> vars = new HashMap<>();
                LinkedList<VariableTree> varList = new LinkedList<>();
                ErrorAwareTreePathScanner scanner = new ErrorAwareTreePathScanner() {
                    private int cnt = 0;
                    @Override
                    public Object visitIdentifier(IdentifierTree node, Object p) {
                        Element e = trees.getElement(getCurrentPath());
                        VariableTree var;
                        if (e != null && (var = vars.remove(e)) != null) {
                            sb.append(var.getType()).append(' ').append(var.getName());
                            TypeMirror tm = ((VariableElement)e).asType();
                            switch(tm.getKind()) {
                                case ARRAY:
                                case DECLARED:
                                    sb.append(" = ${").append(last.incrementAndGet()).append(':').append("\"null\"}");
                                    break;
                                case BOOLEAN:
                                    sb.append(" = ${").append(last.incrementAndGet()).append(':').append("\"false\"}");
                                    break;
                                case BYTE:
                                case CHAR:
                                case DOUBLE:
                                case FLOAT:
                                case INT:
                                case LONG:
                                case SHORT:
                                    sb.append(" = ${").append(last.incrementAndGet()).append(':').append("\"0\"}");
                                    break;
                            }
                            sb.append(";\n"); //NOI18N
                        }
                        return null;
                    }
                };
                for (StatementTree st : ((BlockTree)tree).getStatements()) {
                    if (sp.getStartPosition(info.getCompilationUnit(), st) >= startOffset) {
                        if (sp.getEndPosition(info.getCompilationUnit(), st) <= endOffset) {
                            if (st.getKind() == Tree.Kind.VARIABLE) {
                                Element e = trees.getElement(new TreePath(treePath, st));
                                if (e != null && e.getKind() == ElementKind.LOCAL_VARIABLE) {
                                    vars.put((VariableElement)e, (VariableTree)st);
                                    varList.addFirst((VariableTree)st);
                                }
                            }
                        } else {
                            scanner.scan(new TreePath(treePath, st), null);
                        }
                    }
                }
                Collection<VariableTree> vals = vars.values();
                for (VariableTree var : varList) {
                    if (!vals.contains(var)) {
                        int start = (int) sp.getStartPosition(info.getCompilationUnit(), var.getType());
                        int[] span = tu.findNameSpan(var);
                        int end = span != null ? span[0] : (int) sp.getEndPosition(info.getCompilationUnit(), var.getType());
                        edits.add(new TextEdit(new Range(Utils.createPosition(info.getCompilationUnit().getLineMap(), start), Utils.createPosition(info.getCompilationUnit().getLineMap(), end)), ""));
                    }
                }
            }
        }
        return edits;
    }
}
