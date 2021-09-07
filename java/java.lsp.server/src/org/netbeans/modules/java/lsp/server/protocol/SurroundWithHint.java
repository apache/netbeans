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

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
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
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 80)
public final class SurroundWithHint extends CodeActionsProvider {

    private static final String EXPRESSION = "EXPRESSION";
    private static final String CLASS_HEADER = "CLASS_HEADER";
    private static final String COMMAND_INSERT_SNIPPET = "editor.action.insertSnippet";
    private static final String DOTS = "...";
    private static final String SNIPPET = "snippet";
    private static final String SELECTION_VAR = "${selection}";
    private static final String SELECTED_TEXT_VAR = "${0:$TM_SELECTED_TEXT}";
    private static final Pattern SNIPPET_VAR_PATTERN = Pattern.compile("\\$\\{\\s*([-\\w]++)([^}]*)?}");
    private static final Pattern DEFAULT_VALUE_PATTERN = Pattern.compile("default\\s*=\\s*\\\"([^\\\"]*)\\\"");
    private static final Set<String> TO_FILTER = Collections.singleton("fcom");

    @Override
    @NbBundle.Messages({
        "DN_SurroundWith=Surround with ",
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
        Tree.Kind treeKindCtx = null;
        String stringCtx = null;
        TreeUtilities tu = info.getTreeUtilities();
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(info.getTokenHierarchy(), startOffset);
        int delta = ts.move(startOffset);
        if (delta == 0 || ts.moveNext() && ts.token().id() == JavaTokenId.WHITESPACE) {
            delta = ts.move(endOffset);
            if (delta == 0 || ts.moveNext() && ts.token().id() == JavaTokenId.WHITESPACE) {
                String selectedText = info.getText().substring(startOffset, endOffset).trim();
                SourcePositions[] sp = new SourcePositions[1];
                ExpressionTree expr = selectedText.length() > 0 ? tu.parseExpression(selectedText, sp) : null;
                if (expr != null && expr.getKind() != Tree.Kind.IDENTIFIER && !Utilities.containErrors(expr) && sp[0].getEndPosition(null, expr) >= selectedText.length()) {
                    stringCtx = EXPRESSION;
                }
            }
        }
        Tree tree = tu.pathFor(startOffset).getLeaf();
        if (tu.pathFor(endOffset).getLeaf() == tree) {
            treeKindCtx = tree.getKind();
            switch (treeKindCtx) {
                case CASE:
                    if (startOffset < info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), ((CaseTree)tree).getExpression())) {
                        treeKindCtx = null;
                    }
                    break;
                case CLASS:
                    SourcePositions sp = info.getTrees().getSourcePositions();
                    int startPos = (int)sp.getEndPosition(info.getCompilationUnit(), ((ClassTree)tree).getModifiers());
                    if (startPos <= 0) {
                        startPos = (int)sp.getStartPosition(info.getCompilationUnit(), tree);
                    }
                    String headerText = info.getText().substring(startPos, startOffset);
                    int idx = headerText.indexOf('{'); //NOI18N
                    if (idx < 0) {
                        treeKindCtx = null;
                        stringCtx = CLASS_HEADER;
                    }
                    break;
                case FOR_LOOP:
                case ENHANCED_FOR_LOOP:
                    if (!isRightParenthesisOfLoopPresent(info, startOffset)) {
                        treeKindCtx = null;
                    }
                    break;
                case PARENTHESIZED:
                    if (isPartOfWhileLoop(info, startOffset)) {
                        if (!isRightParenthesisOfLoopPresent(info, startOffset)) {
                            treeKindCtx = null;
                        }
                    }
                    break;
            }
        }
        List<CodeAction> codeActions = new ArrayList<>();
        for (CodeTemplate codeTemplate : CodeTemplateManager.get(doc).getCodeTemplates()) {
            String parametrizedText = codeTemplate.getParametrizedText();
            if (parametrizedText.toLowerCase().contains(SELECTION_VAR) && !TO_FILTER.contains(codeTemplate.getAbbreviation())) {
                if (codeTemplate.getContexts() == null || codeTemplate.getContexts().isEmpty() || accept(codeTemplate, treeKindCtx, stringCtx)) {
                    String label = html2text(codeTemplate.getDescription());
                    if (label == null) {
                        String text = codeTemplate.getParametrizedText();
                        int idx = text.indexOf('\n');
                        label = convert(idx < 0 ? text : text.substring(0, idx) + DOTS, true);
                    }
                    String snippet = convert(codeTemplate.getParametrizedText(), false);
                    codeActions.add(createCodeAction(Bundle.DN_SurroundWith() + label, CodeActionKind.RefactorRewrite, COMMAND_INSERT_SNIPPET, Collections.singletonMap(SNIPPET, snippet)));
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

    private static boolean isRightParenthesisOfLoopPresent(CompilationInfo info, int abbrevStartOffset) {
        TokenHierarchy<?> tokenHierarchy = info.getTokenHierarchy();
        TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(abbrevStartOffset);
        if (tokenSequence.moveNext()) {
            TokenId tokenId = skipNextWhitespaces(tokenSequence);
            return tokenId == null ? false : (tokenId == JavaTokenId.RPAREN);
        }
        return false;
    }

    private static TokenId skipNextWhitespaces(TokenSequence<?> tokenSequence) {
        TokenId tokenId = null;
        while (tokenSequence.moveNext()) {
            Token<?> token = tokenSequence.token();
            if (token != null) {
                tokenId = token.id();
            }
            if (tokenId != JavaTokenId.WHITESPACE) {
                break;
            }
        }
        return tokenId;
    }

    private static boolean isPartOfWhileLoop(CompilationInfo info, int abbrevStartOffset) {
        TreeUtilities treeUtilities = info.getTreeUtilities();
        TreePath currentPath = treeUtilities.pathFor(abbrevStartOffset);
        TreePath parentPath = treeUtilities.getPathElementOfKind(Tree.Kind.WHILE_LOOP, currentPath);
        return parentPath != null;
    }

    private static boolean accept(CodeTemplate template, Tree.Kind treeKindCtx, String stringCtx) {
        if (treeKindCtx == null && stringCtx == null) {
            return false;
        }
        EnumSet<Tree.Kind> treeKindContexts = EnumSet.noneOf(Tree.Kind.class);
        HashSet stringContexts = new HashSet();
        getTemplateContexts(template, treeKindContexts, stringContexts);
        return treeKindContexts.isEmpty() && stringContexts.isEmpty() && treeKindCtx != Tree.Kind.STRING_LITERAL || treeKindContexts.contains(treeKindCtx) || stringContexts.contains(stringCtx);
    }

    private static void getTemplateContexts(CodeTemplate template, EnumSet<Tree.Kind> treeKindContexts, HashSet<String> stringContexts) {
        List<String> contexts = template.getContexts();
        if (contexts != null) {
            for(String context : contexts) {
                try {
                    treeKindContexts.add(Tree.Kind.valueOf(context));
                } catch (IllegalArgumentException iae) {
                    stringContexts.add(context);
                }
            }
        }
    }

    private static String convert(String s, boolean label) {
        StringBuilder sb = new StringBuilder();
        Matcher varMatcher = SNIPPET_VAR_PATTERN.matcher(s);
        int idx = 0;
        Map<String, Integer> placeholders = new HashMap<>();
        AtomicInteger last = new AtomicInteger();
        while (varMatcher.find(idx)) {
            int start = varMatcher.start();
            sb.append(s.substring(idx, start));
            String name = varMatcher.group(1);
            switch (name) {
                case CodeTemplateParameter.CURSOR_PARAMETER_NAME:
                case CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME:
                case CodeTemplateParameter.NO_INDENT_PARAMETER_NAME:
                    break;
                case CodeTemplateParameter.SELECTION_PARAMETER_NAME:
                    sb.append(label ? DOTS : SELECTED_TEXT_VAR);
                    break;
                default:
                    Integer placeholder = placeholders.computeIfAbsent(name, n -> last.incrementAndGet());
                    String params = varMatcher.groupCount() > 1 ? varMatcher.group(2) : null;
                    if (params == null) {
                        if (!label) {
                            sb.append('$').append(placeholder);
                        }
                    } else {
                        Matcher defaultValueMatcher = DEFAULT_VALUE_PATTERN.matcher(params);
                        if (defaultValueMatcher.find()) {
                            if (label) {
                                sb.append(defaultValueMatcher.group(1));
                            } else {
                                sb.append("${").append(placeholder).append(':').append(defaultValueMatcher.group(1)).append('}');
                            }
                        } else if (!label) {
                            sb.append('$').append(placeholder);
                        }
                    }
            }
            idx = varMatcher.end();
        }
        String tail = s.substring(idx);
        return sb.append(tail.endsWith("\n") ? tail.substring(0, tail.length() - 1) : tail).toString();
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
