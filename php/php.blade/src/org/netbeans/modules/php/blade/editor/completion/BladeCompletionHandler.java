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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.blade.csl.elements.DirectiveElement;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.BladeElement;
import org.netbeans.modules.php.blade.csl.elements.PathElement;
import org.netbeans.modules.php.blade.csl.elements.PhpFunctionElement;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives;
import org.netbeans.modules.php.blade.editor.indexing.BladeIndex;
import org.netbeans.modules.php.blade.editor.lexer.BladeLexerUtils;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.BLADE_DIRECTIVE_UNKNOWN;
import org.netbeans.modules.php.blade.editor.parser.BladeDirectiveScope;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;
import org.netbeans.modules.php.blade.editor.path.BladePathUtils;
import org.netbeans.modules.php.blade.project.AssetsBundlerSupport;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.netbeans.modules.php.blade.editor.directives.DirectivesList;
import org.netbeans.modules.php.blade.syntax.StringUtils;
import org.netbeans.modules.php.blade.syntax.ViewPathUtils;
import org.netbeans.modules.php.blade.syntax.annotation.Directive;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author bogdan
 */
public class BladeCompletionHandler implements CodeCompletionHandler2 {

    private static final Logger LOGGER = Logger.getLogger(BladeCompletionHandler.class.getName());
    private static final String AT = "@"; //NOI18N
    private static final String BLADE_LOOP_VAR = "$loop"; //NOI18N

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

        final TokenHierarchy<?> th = parserResult.getSnapshot().getTokenHierarchy();

        if (th == null) {
            return CodeCompletionResult.NONE;
        }

        TokenSequence<BladeTokenId> ts = BladeLexerUtils.getBladeTokenSequenceDoc(th, offset);

        if (ts == null) {
            return CodeCompletionResult.NONE;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return CodeCompletionResult.NONE;
        }

        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();

        final List<CompletionProposal> completionProposals = new ArrayList<>();

        OffsetRange phpExprRange = parserResult.getBladePhpExpressionOccurences()
                .findPhpExpressionLocation(offset);

        String contextPrefix = completionContext.getPrefix();

        if (contextPrefix.startsWith(AT) && phpExprRange == null) {
            completeDirectives(completionProposals, offset, fo, contextPrefix);
            return new DefaultCompletionResult(completionProposals, false);
        } else if (phpExprRange != null) {
            BladeParserResult.BladeStringReference reference = parserResult.getBladeReferenceIdsCollection()
                    .findOccuredRefrence(offset);

            if (reference != null) {
                completeBladeReference(completionProposals, fo, reference, offset);
            } else if (isNotPhpAssignmentExpr(contextPrefix)) { 
                BladeDirectiveScope scope = parserResult.getBladeScope().findScope(offset);

                if (scope != null) {
                    int anchorOffset = computeAnchorOffset(contextPrefix, offset);
                    for (String variableName : scope.getScopeVariables()) {
                        if (variableName.startsWith(contextPrefix)) {
                            BladeElement variableElement = new BladeElement(variableName, fo, ElementType.VARIABLE);
                            completionProposals.add(new BladeCompletionProposal.VariableItem(variableElement, anchorOffset, variableName));
                        }
                    }

                    if (scope.getScopeType() == D_FOREACH && BLADE_LOOP_VAR.startsWith(contextPrefix)) {  //NOI18N
                        BladeElement variableElement = new BladeElement(BLADE_LOOP_VAR, fo, ElementType.VARIABLE);
                        completionProposals.add(new BladeCompletionProposal.VariableItem(variableElement, anchorOffset, BLADE_LOOP_VAR));
                    }
                }
            } else {
                CharSequence snapshotExpr = completionContext.getParserResult().getSnapshot().getText().subSequence(phpExprRange.getStart(), phpExprRange.getEnd());
                PhpCodeCompletionService.completePhpCode(completionProposals,
                        snapshotExpr.toString(), phpExprRange.getStart(), offset, fo);
            }
        }

        if (completionProposals.isEmpty()) {
            return CodeCompletionResult.NONE;
        }

        long time = System.currentTimeMillis() - startTime;
        if (time > 2000) {
            LOGGER.info(String.format("complete() with results took %d ms", time)); //NOI18N
        }
        return new DefaultCompletionResult(completionProposals, false);
    }
    
    private boolean isNotPhpAssignmentExpr(String contextPrefix){
        return contextPrefix.startsWith("$") && !contextPrefix.contains("="); //NOI18N
    }

    private void completeBladeReference(final List<CompletionProposal> completionProposals,
            FileObject fo, BladeParserResult.BladeStringReference reference, int offset) {
        switch (reference.antlrTokentype) {
            case D_EXTENDS, D_INCLUDE, D_INCLUDE_IF, D_INCLUDE_WHEN, D_INCLUDE_UNLESS, D_INCLUDE_FIRST, D_EACH -> {
                completeViewPath(completionProposals, reference.identifier, fo, offset);
            }
            case D_SECTION, D_HAS_SECTION, D_SECTION_MISSING -> {
                String yieldId = reference.identifier;
                completeYieldIdFromIndex(completionProposals, yieldId, fo, offset);
            }
            case D_PUSH, D_PUSH_IF, D_PUSH_ONCE, D_PREPEND -> {
                String stackId = reference.identifier;
                completeStackIdFromIndex(completionProposals, stackId, fo, offset);
            }
            case D_VITE -> {
                String assetPath = reference.identifier;
                completeResourcePath(completionProposals, assetPath, fo, offset);
            }
        }
    }

    private void completeViewPath(final List<CompletionProposal> completionProposals,
            String pathName, FileObject fo, int offset) {
        int pathOffset = ViewPathUtils.getViewPathSeparatorOffset(pathName, offset);
        List<FileObject> childrenFiles = BladePathUtils.getParentChildrenFromPrefixPath(fo, pathName);
        for (FileObject file : childrenFiles) {
            String pathFileName = file.getName();
            boolean isFolder = file.isFolder();
            if (!isFolder) {
                pathFileName = pathFileName.replace(BladeLanguage.FILE_EXTENSION_SUFFIX, ""); // NOI18N
            }
            PathElement pathEl = new PathElement(pathFileName, file);
            completionProposals.add(new BladeCompletionProposal.ViewPathProposal(pathEl, pathOffset, pathFileName, isFolder));
        }
    }

    private void completeYieldIdFromIndex(final List<CompletionProposal> completionProposals,
            String prefixIdentifier, FileObject fo, int offset) {
        BladeIndex bladeIndex;
        Project project = ProjectUtils.getMainOwner(fo);
        int anchorOffset = computeAnchorOffset(prefixIdentifier, offset);

        try {
            bladeIndex = BladeIndex.get(project);
            List<BladeIndex.IndexedReferenceId> indexedReferences = bladeIndex.queryYieldIds(prefixIdentifier);
            for (BladeIndex.IndexedReferenceId indexReference : indexedReferences) {
                BladeElement yieldIdEl = new BladeElement(indexReference.getIdenfiier(), fo, ElementType.YIELD_ID);
                completionProposals.add(new BladeCompletionProposal.LayoutIdentifierProposal(yieldIdEl, anchorOffset, indexReference.getIdenfiier()));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void completeStackIdFromIndex(final List<CompletionProposal> completionProposals,
            String prefixIdentifier, FileObject fo, int offset) {
        BladeIndex bladeIndex;
        Project project = ProjectUtils.getMainOwner(fo);
        int anchorOffset = computeAnchorOffset(prefixIdentifier, offset);

        try {
            bladeIndex = BladeIndex.get(project);
            List<BladeIndex.IndexedReferenceId> indexedReferences = bladeIndex.queryStacksIndexedReferences(prefixIdentifier);
            for (BladeIndex.IndexedReferenceId indexReference : indexedReferences) {
                BladeElement yieldIdEl = new BladeElement(indexReference.getIdenfiier(), fo, ElementType.STACK_ID);
                completionProposals.add(new BladeCompletionProposal.LayoutIdentifierProposal(yieldIdEl, anchorOffset, indexReference.getIdenfiier()));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void completeResourcePath(final List<CompletionProposal> completionProposals,
            String pathPrefix, FileObject fo, int offset) {

        FileObject projectDir = ProjectUtils.getProjectDirectory(fo);
        
        if (projectDir == null) {
            return;
        }

        FileObject resourceDir = projectDir.getFileObject(AssetsBundlerSupport.RESOURCE_ROOT);

        if (resourceDir == null) {
            return;
        }

        int firstSlash = pathPrefix.indexOf(StringUtils.FORWARD_SLASH);
        int lastSlash = pathPrefix.lastIndexOf(StringUtils.FORWARD_SLASH);

        if (firstSlash == -1 && AssetsBundlerSupport.RESOURCE_ROOT.startsWith(pathPrefix)) {

            int anchorOffset = computeAnchorOffset(pathPrefix, offset);
            for (FileObject file : resourceDir.getChildren()) {
                if (!file.isFolder() || file.getName().equals(BladePathUtils.LARAVEL_VIEW_FOLDER)) {
                    continue;
                }

                String proposedFolder = AssetsBundlerSupport.RESOURCE_ROOT + StringUtils.FORWARD_SLASH + file.getName();
                PathElement pathEl = new PathElement(proposedFolder, file);
                completionProposals.add(new BladeCompletionProposal.FilePathProposal(pathEl, anchorOffset, proposedFolder, true));
            }
        } else {
            int anchorOffset = computeAnchorOffset(pathPrefix, offset) + lastSlash + 1;
            List<FileObject> fileList = BladePathUtils.filterFilesFromRootFolder(new FileObject[]{projectDir}, pathPrefix, lastSlash);
            for (FileObject file : fileList) {
                String proposedPath = file.getNameExt();
                PathElement pathEl = new PathElement(proposedPath, file);
                completionProposals.add(new BladeCompletionProposal.FilePathProposal(pathEl, anchorOffset, proposedPath, file.isFolder()));
            }
        }
    }

    private void completeDirectives(final List<CompletionProposal> completionProposals,
            int caretOffset, FileObject fo, String prefix) {
        DirectivesList listClass = new DirectivesList();

        int anchorOffset = computeAnchorOffset(prefix, caretOffset);

        for (Directive directive : listClass.getDirectives()) {
            String directiveName = directive.name();
            if (directiveName.startsWith(prefix)) {
                DirectiveElement directiveEl = new DirectiveElement(directiveName, fo);

                if (directive.params()) {
                    completionProposals.add(new BladeCompletionProposal.DirectiveWithArg(directiveEl, anchorOffset, directive));
                    if (!directive.endtag().isEmpty()) {
                        completionProposals.add(new BladeCompletionProposal.BlockDirectiveWithArg(directiveEl, anchorOffset, directive));
                    }
                } else {
                    completionProposals.add(new BladeCompletionProposal.InlineDirective(directiveEl, anchorOffset, directive));
                    if (!directive.endtag().isEmpty()) {
                        completionProposals.add(new BladeCompletionProposal.BlockDirective(directiveEl, anchorOffset, directive));
                    }
                }
            }
        }

        Project project = ProjectUtils.getMainOwner(fo);
        CustomDirectives.getInstance(project).filterAction(new CustomDirectives.FilterCallback() {
            @Override
            public void filterDirectiveName(CustomDirectives.CustomDirective directive, FileObject file) {
                DirectiveElement directiveEl = new DirectiveElement(directive.getName(), file);
                if (directive.getName().startsWith(prefix)) {
                    int anchorOffset = computeAnchorOffset(prefix, caretOffset);
                    completionProposals.add(
                            new BladeCompletionProposal.CustomDirective(
                                    directiveEl,
                                    anchorOffset,
                                    directive.getName()
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

        BaseDocument document = (BaseDocument) info.getSnapshot().getSource().getDocument(false);

        if (document == null) {
            return null;
        }
        try {
            document.readLock();
            TokenSequence<BladeTokenId> ts = BladeLexerUtils.getTokenSequence(document, offset);

            if (ts == null) {
                return null;
            }

            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }
            org.netbeans.api.lexer.Token<BladeTokenId> token = ts.token();

            String tokenPrefix = token.text().toString().trim();
            BladeTokenId tokenId = token.id();
            if (tokenId.equals(BLADE_DIRECTIVE_UNKNOWN)) {
                //sanitize adiacent emebedding hack to trigger blade completion
                //ex: "@$caret" or @$caret>
                if (tokenPrefix.endsWith("\"") || tokenPrefix.endsWith(">")) { // NOI18N
                    return tokenPrefix.substring(0, tokenPrefix.length() - 1);
                }
            }
            return tokenPrefix;
        } finally {
            document.readUnlock();
        }
    }

    @Override
    public CodeCompletionHandler.QueryType getAutoQuery(JTextComponent component, String typedText) {
        if (typedText.length() == 0) {
            return CodeCompletionHandler.QueryType.NONE;
        }

        if (typedText.startsWith(AT)) {
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
        } else if (elementHandle instanceof BladeElement) {
            return TooltipDoc.generateDoc((BladeElement) elementHandle);
        }
        return result;
    }

    private int computeAnchorOffset(@NonNull String prefix, int offset) {
        return offset - prefix.length();
    }
}
