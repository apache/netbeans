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
package org.netbeans.modules.php.blade.editor.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.blade.csl.elements.DirectiveElement;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.NamedElement;
import org.netbeans.modules.php.blade.csl.elements.PhpFunctionElement;
import org.netbeans.modules.php.blade.csl.elements.TagElement;
import org.netbeans.modules.php.blade.editor.completion.BladeCompletionProposal.CompletionRequest;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;
import org.netbeans.modules.php.blade.editor.preferences.ModulePreferences;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.netbeans.modules.php.blade.syntax.BladeTags;
import org.netbeans.modules.php.blade.syntax.annotation.Directive;
import org.netbeans.modules.php.blade.syntax.annotation.Tag;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrUtils;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser.CONTENT_TAG_OPEN;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class BladeCompletionHandler implements CodeCompletionHandler2 {

    private static final Logger LOGGER = Logger.getLogger(BladeCompletionHandler.class.getName());

    @Override
    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
        if (CancelSupport.getDefault().isCancelled()) {
            return CodeCompletionResult.NONE;
        }
        long startTime = System.currentTimeMillis();
        BaseDocument doc = (BaseDocument) completionContext.getParserResult().getSnapshot().getSource().getDocument(false);

        if (doc == null) {
            return CodeCompletionResult.NONE;
        }

        int offset = completionContext.getCaretOffset();

        if (offset < 1) {
            return CodeCompletionResult.NONE;
        }

        BladeParserResult parserResult = (BladeParserResult) completionContext.getParserResult();

        final List<CompletionProposal> completionProposals = new ArrayList<>();

        Token currentToken = BladeAntlrUtils.getToken(doc, offset - 1);

        if (currentToken == null) {
            return CodeCompletionResult.NONE;
        }

        String prefix = currentToken.getText();

        if (prefix == null) {
            return CodeCompletionResult.NONE;
        }

        String tokenText = currentToken.getText();
        FileObject fo = completionContext.getParserResult().getSnapshot().getSource().getFileObject();
        //D_UNKNOWN_ATTR_ENC hack to fix completion not triggered in html embedded text
        if (tokenText.startsWith("@") && currentToken.getType() != D_UNKNOWN_ATTR_ENC) {
            completeDirectives(completionProposals, completionContext, fo, currentToken);
        } else {
            if (prefix.length() == 1) {
                return CodeCompletionResult.NONE;
            }
            switch (currentToken.getType()) {
                case PHP_IDENTIFIER:
                case PHP_NAMESPACE_PATH:
                    PhpCodeCompletionService.completePhpCode(completionProposals, parserResult, offset, prefix);
                    break;
                case PHP_EXPRESSION:
                    completePhpSnippet(completionProposals, offset, currentToken);
                    break;
                case PHP_VARIABLE:
                    completeScopedVariables(completionProposals, completionContext, parserResult, currentToken);
                    break;
                case CONTENT_TAG_OPEN:
                case RAW_TAG_OPEN:
                    //{{ | {!!
                    if (!ModulePreferences.isAutoTagCompletionEnabled()) {
                        completeBladeTags(completionProposals, completionContext, currentToken);
                    }
                    break;
            }
        }

        if (completionProposals.isEmpty()) {
            return CodeCompletionResult.NONE;
        }

        long time = System.currentTimeMillis() - startTime;
        if (time > 2000){
            LOGGER.info(String.format("complete() with results took %d ms", time));
        }
        return new DefaultCompletionResult(completionProposals, false);
    }


    /**
     * proxy completion using the original php code completion service
     *
     * @param completionProposals
     * @param offset
     * @param currentToken
     */
    private void completePhpSnippet(final List<CompletionProposal> completionProposals,
            int offset, Token currentToken) {
        PhpCodeCompletionService phpCodeCompletion = new PhpCodeCompletionService();
        for (CompletionProposal proposal : phpCodeCompletion.getCompletionProposal(offset, currentToken)) {
            String proposalPrefix = proposal.getInsertPrefix();
            if (proposalPrefix.startsWith(phpCodeCompletion.prefix)) {
                completionProposals.add(proposal);
            }
        }
    }

    private void completeScopedVariables(final List<CompletionProposal> completionProposals,
            CodeCompletionContext completionContext, BladeParserResult parserResult, Token currentToken) {
        String variablePrefix = currentToken.getText();
        Set<String> scopedVariables = parserResult.findLoopVariablesForScope(completionContext.getCaretOffset());
        FileObject fo = completionContext.getParserResult().getSnapshot().getSource().getFileObject();

        if (scopedVariables != null && !scopedVariables.isEmpty()) {
            CompletionRequest request = new CompletionRequest();
            request.anchorOffset = completionContext.getCaretOffset() - variablePrefix.length();
            request.carretOffset = completionContext.getCaretOffset();
            request.prefix = variablePrefix;
            if ("$loop".startsWith(variablePrefix)) {
                String variableName = "$loop";
                NamedElement variableElement = new NamedElement(variableName, fo, ElementType.VARIABLE);
                completionProposals.add(new BladeCompletionProposal.BladeVariableItem(variableElement, request, variableName));
            }
            for (String variableName : scopedVariables) {
                if (variableName.startsWith(variablePrefix)) {
                    NamedElement variableElement = new NamedElement(variableName, fo, ElementType.VARIABLE);
                    completionProposals.add(new BladeCompletionProposal.VariableItem(variableElement, request, variableName));
                }
            }
        }
    }

    /**
     * BLADES
     *
     * @param completionProposals
     * @param completionContext
     * @param parserResult
     * @param currentToken
     */
    private void completeBladeTags(final List<CompletionProposal> completionProposals,
            CodeCompletionContext completionContext, Token currentToken) {
        String tagStart = currentToken.getText();

        CompletionRequest request = completionRequest(tagStart, completionContext.getCaretOffset());
        BladeTags tagsContainer = new BladeTags();
        Tag[] tags = tagsContainer.getTags();
        for (Tag tag : tags) {
            if (tag.openTag().startsWith(tagStart)) {
                TagElement tagElement = new TagElement(tag.closeTag());
                completionProposals.add(new BladeCompletionProposal.BladeTag(tagElement, request, tag));
            }
        }
    }

    private void completeDirectives(final List<CompletionProposal> completionProposals,
            CodeCompletionContext completionContext, FileObject fo, Token currentToken) {
        String prefix = currentToken.getText();
        DirectiveCompletionList completionList = new DirectiveCompletionList();

        CompletionRequest request = completionRequest(prefix, completionContext.getCaretOffset());

        for (Directive directive : completionList.getDirectives()) {
            String directiveName = directive.name();
            if (directiveName.startsWith(prefix)) {
                DirectiveElement directiveEl = new DirectiveElement(directiveName, fo);

                if (directive.params()) {
                    completionProposals.add(new BladeCompletionProposal.DirectiveWithArg(directiveEl, request, directive));
                    if (!directive.endtag().isEmpty()) {
                        completionProposals.add(new BladeCompletionProposal.BlockDirectiveWithArg(directiveEl, request, directive));
                    }
                } else {

                    completionProposals.add(new BladeCompletionProposal.InlineDirective(directiveEl, request, directive));
                    if (!directive.endtag().isEmpty()) {
                        completionProposals.add(new BladeCompletionProposal.BlockDirective(directiveEl, request, directive));
                    }
                }
            }
        }

        Project project = ProjectUtils.getMainOwner(fo);
        CustomDirectives.getInstance(project).filterAction(new CustomDirectives.FilterCallback() {
            @Override
            public void filterDirectiveName(CustomDirectives.CustomDirective directive, FileObject file) {
                DirectiveElement directiveEl = new DirectiveElement(directive.name, file);
                if (directive.name.startsWith(prefix)) {
                    CompletionRequest request = completionRequest(prefix, completionContext.getCaretOffset());
                    completionProposals.add(
                            new BladeCompletionProposal.CustomDirective(
                                    directiveEl,
                                    request,
                                    directive.name
                            ));
                }
            }
        });
    }

    @Override
    public String document(ParserResult pr, ElementHandle eh) {
        return null;
    }

    @Override
    public ElementHandle resolveLink(String string, ElementHandle eh) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int offset, boolean upToOffset) {
        return null;
    }

    @Override
    public CodeCompletionHandler.QueryType getAutoQuery(JTextComponent component, String typedText) {
        if (typedText.length() == 0) {
            return CodeCompletionHandler.QueryType.NONE;
        }

        if (typedText.startsWith("@")) {
            return CodeCompletionHandler.QueryType.ALL_COMPLETION;
        }

        char lastChar = typedText.charAt(typedText.length() - 1);

        switch (lastChar) {
            case '\n':
                return CodeCompletionHandler.QueryType.STOP;
            default:
                return CodeCompletionHandler.QueryType.ALL_COMPLETION;
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String resolveTemplateVariable(String string, ParserResult pr, int i, String string1, Map map) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document dcmnt, int i, int i1) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult pr, int i, CompletionProposal cp) {
        return new ParameterInfo(new ArrayList<>(), 0, 0);
    }

    /**
     * used also for tooltip in blade mime context
     *
     * @param parserResult
     * @param elementHandle
     * @param cancel
     * @return
     */
    @Override
    public Documentation documentElement(ParserResult parserResult, ElementHandle elementHandle, Callable<Boolean> cancel) {
        Documentation result = null;
        if (elementHandle instanceof PhpFunctionElement) {
            return TooltipDoc.generateFunctionDoc((PhpFunctionElement) elementHandle);
        } else if (elementHandle instanceof DirectiveElement) {
            return result;
        } else if (elementHandle instanceof NamedElement) {
            return TooltipDoc.generateDoc((NamedElement) elementHandle);
        }
        return result;
    }

    public static CompletionRequest completionRequest(String prefix, int offset) {
        CompletionRequest request = new CompletionRequest();
        request.anchorOffset = offset - prefix.length();
        request.carretOffset = offset;
        request.prefix = prefix;

        return request;
    }
}
