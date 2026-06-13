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
package org.netbeans.modules.php.blade.editor.declaration;

import java.util.Collection;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.BladeElement;
import org.netbeans.modules.php.blade.csl.elements.PathElement;
import org.netbeans.modules.php.blade.csl.elements.PhpFunctionElement;
import org.netbeans.modules.php.blade.editor.components.ComponentsQueryService;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives.CustomDirective;
import org.netbeans.modules.php.blade.editor.indexing.BladeIndex;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexFunctionResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.editor.indexing.QueryUtils;
import org.netbeans.modules.php.blade.editor.lexer.BladeLexerUtils;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.BLADE_CUSTOM_DIRECTIVE;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.BLADE_PAREN;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.HTML;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.PHP_BLADE_ECHO_EXPR;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.PHP_BLADE_EXPRESSION;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.PHP_BLADE_INLINE_CODE;
import org.netbeans.modules.php.blade.editor.parser.BladeCustomDirectiveOccurences.CustomDirectiveOccurence;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult.BladeStringReference;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult.Reference;
import org.netbeans.modules.php.blade.editor.path.BladePathUtils;
import org.netbeans.modules.php.blade.project.ComponentsSupport;
import static org.netbeans.modules.php.blade.project.ComponentsSupport.COMPONENT_TAG_NAME_PREFIX;
import org.netbeans.modules.php.blade.syntax.StringUtils;
import static org.netbeans.modules.php.blade.syntax.antlr4.php.BladePhpAntlrParser.IDENTIFIER;
import org.netbeans.modules.php.blade.syntax.antlr4.php.BladePhpAntlrUtils;
import org.netbeans.modules.php.blade.editor.parser.BladePhpSnippetParser.PhpReference;
import org.openide.filesystems.FileObject;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import static org.netbeans.modules.php.blade.editor.parser.BladePhpSnippetParser.PhpReferenceType.PHP_FUNCTION;
import org.netbeans.modules.php.blade.syntax.antlr4.utils.BladeAntlrLexerUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.project.ui.support.ProjectConvertors;

/**
 * declaration finder for : 
 * - include paths
 * - section related yields
 * - stack locations
 * - vite assets path
 * - custom directive definitions
 * - components class origin
 *
 *
 * @author bhaidu
 */
public class BladeDeclarationFinder implements DeclarationFinder {

    @Override
    public OffsetRange getReferenceSpan(Document document, int caretOffset) {
        OffsetRange offsetRange = OffsetRange.NONE;
        AbstractDocument adoc = (AbstractDocument) document;

        try {
            adoc.readLock();
            TokenSequence<BladeTokenId> ts = BladeLexerUtils.getTokenSequence(document, caretOffset);

            if (ts == null) {
                return offsetRange;
            }

            ts.move(caretOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return offsetRange;
            }

            Token<BladeTokenId> token = ts.token();
            String caretTokenText = token.text().toString();
            BladeTokenId id = token.id();
            int tokenStart = ts.offset();

            switch (id) {
                case BLADE_CUSTOM_DIRECTIVE: {
                    return new OffsetRange(tokenStart, tokenStart + caretTokenText.length());
                }
                case PHP_BLADE_EXPRESSION: {
                    return getReferenceSpanInsidePhpExpr(document, ts, token, caretOffset);
                }
                case PHP_BLADE_ECHO_EXPR:
                case PHP_BLADE_INLINE_CODE: {
                    return getPhpReferenceSpan(caretTokenText, caretOffset, tokenStart);
                }
                case PHP_INLINE: {
                    TokenSequence<? extends PHPTokenId> tsPhp = BladeLexerUtils.getPhpTokenSequence(document, caretOffset);
                    if (tsPhp == null) {
                        return offsetRange;
                    }
                    tsPhp.move(caretOffset);

                    if (!tsPhp.moveNext() && !tsPhp.movePrevious()) {
                        return offsetRange;
                    }

                    Token<? extends PHPTokenId> phpToken = tsPhp.token();
                    PHPTokenId phpTokenId = phpToken.id();
                    if (phpTokenId.equals(PHPTokenId.PHP_STRING)) {
                        return new OffsetRange(tsPhp.offset(), tsPhp.offset() + phpToken.length());
                    }
                    break;
                }
                case HTML: {
                    TokenHierarchy<?> th = TokenHierarchy.get(document);
                    Token<? extends HTMLTokenId> htmlToken = BladeLexerUtils.getHtmlToken(th, caretOffset);

                    if (htmlToken == null) {
                        return offsetRange;
                    }

                    HTMLTokenId htmlTokenId = htmlToken.id();
                    int tokenOffset = htmlToken.offset(th);

                    if (htmlTokenId.equals(HTMLTokenId.TAG_OPEN)) {
                        String tag = htmlToken.text().toString();
                        if (tag.startsWith(COMPONENT_TAG_NAME_PREFIX)) { // NOI18N
                            return new OffsetRange(tokenOffset, tokenOffset + htmlToken.length());
                        }
                    }

                    break;
                }

            }
        } finally {
            adoc.readUnlock();
        }

        return offsetRange;
    }

    public OffsetRange getReferenceSpanInsidePhpExpr(Document document,
            TokenSequence<BladeTokenId> ts,
            Token<BladeTokenId> token, int caretOffset) {

        OffsetRange offsetRange = OffsetRange.NONE;
        int phpExprStart = ts.offset();
        ts.movePrevious();

        boolean prevTokenIsParenthesis = ts.token().id().equals(BLADE_PAREN);

        if (prevTokenIsParenthesis && !ts.movePrevious()) {
            //move before the parenthesis
            return offsetRange;
        }

        Token<BladeTokenId> prevToken = ts.token();
        int start = ts.offset();
        int snippetLength = prevToken.length() + token.length() + 2;
        int end = ts.offset() + snippetLength;

        boolean isBetween = start <= caretOffset && caretOffset <= end;

        if (!isBetween) {
            return offsetRange;
        }

        if (snippetLength > document.getLength()) {
            return offsetRange;
        }

        try {
            String snippet = document.getText(start, snippetLength);
            int parenPos = snippet.indexOf("("); // NOI18N
            String directive = snippet.startsWith("@") && parenPos != -1 ? snippet.substring(0, parenPos) : null; // NOI18N
            int referencedOffset = caretOffset - start;

            if (directive != null) {//we can filter for identifiable directives here
                org.antlr.v4.runtime.Token targetetToken = BladeAntlrLexerUtils.getToken(snippet, referencedOffset);
                if (targetetToken != null && targetetToken.getType() == IDENTIFIABLE_STRING) {
                    offsetRange = new OffsetRange(targetetToken.getStartIndex() + start, start + targetetToken.getStopIndex() + 1);
                    return offsetRange;
                }
            }
            //php context
            return getPhpReferenceSpan(token.text().toString(), caretOffset, phpExprStart);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return offsetRange;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        DeclarationLocation location = DeclarationLocation.NONE;
        final TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();

        if (th == null) {
            return location;
        }

        TokenSequence<BladeTokenId> ts = BladeLexerUtils.getBladeTokenSequenceDoc(th, caretOffset);

        if (ts == null) {
            return location;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return location;
        }

        Token<BladeTokenId> token = ts.token();
        BladeTokenId id = token.id();

        BladeParserResult parserResult = (BladeParserResult) info;
        FileObject currentFile = parserResult.getFileObject();

        switch (id) {
            case BLADE_CUSTOM_DIRECTIVE: {
                return findCustomDirectiveDeclaration(parserResult, caretOffset, currentFile, location);
            }
            case HTML: {
                return findComponentClassDeclaration(th, caretOffset, currentFile, location);
            }
        }
        //we can have string or php reference
        BladeStringReference bladeReference = parserResult.getBladeReferenceIdsCollection().findOccuredRefrence(caretOffset);
        OffsetRange caretRange;

        if (bladeReference != null) {
            String referenceIdentifier = bladeReference.identifier;

            switch (bladeReference.antlrTokentype) {
                case D_SECTION:
                case D_HAS_SECTION:
                case D_SECTION_MISSING: {
                    String yieldId = referenceIdentifier;
                    List<BladeIndex.IndexedReference> yields = QueryUtils.findYieldReferences(yieldId, currentFile);
                    if (yields == null) {
                        return DeclarationLocation.NONE;
                    }

                    for (BladeIndex.IndexedReference yieldReference : yields) {
                        location = buildDeclarationLocationFromIndex(location, yieldReference, ElementType.YIELD_ID);
                    }

                    return location;
                }
                case D_EXTENDS:
                case D_INCLUDE:
                case D_INCLUDE_IF:
                case D_INCLUDE_WHEN:
                case D_INCLUDE_UNLESS:
                case D_INCLUDE_FIRST:
                case D_EACH: {
                    String viewPath = referenceIdentifier;
                    List<FileObject> includedFiles = BladePathUtils.findFileObjectsForBladeViewPath(currentFile, viewPath);

                    if (includedFiles.isEmpty()) {
                        return DeclarationLocation.NONE;
                    }

                    for (FileObject includedFile : includedFiles) {
                        PathElement elHandle = new PathElement(referenceIdentifier, includedFile);
                        DeclarationLocation dln = new DeclarationFinder.DeclarationLocation(includedFile, 0, elHandle);
                        if (location.equals(DeclarationLocation.NONE)) {
                            location = dln;
                        }
                        location.addAlternative(new AlternativeLocationImpl(dln));
                    }
                    return location;
                }
                case D_PUSH:
                case D_PUSH_IF:
                case D_PREPEND: {
                    String stackId = referenceIdentifier;
                    List<BladeIndex.IndexedReference> stacks = QueryUtils.findStacksReferences(stackId, currentFile);

                    if (stacks == null) {
                        return DeclarationLocation.NONE;
                    }

                    for (BladeIndex.IndexedReference stackReference : stacks) {
                        String stackReferenceId = stackReference.getReference().identifier;
                        BladeElement yieldIdHandle = new BladeElement(stackReferenceId, stackReference.getOriginFile(), ElementType.STACK_ID);
                        int startOccurence = stackReference.getReference().defOffset.getStart();
                        DeclarationLocation dlstack = new DeclarationFinder.DeclarationLocation(stackReference.getOriginFile(), startOccurence, yieldIdHandle);
                        if (location.equals(DeclarationLocation.NONE)) {
                            location = dlstack;
                        }
                        location.addAlternative(new AlternativeLocationImpl(dlstack));
                    }

                    return location;
                }
                case D_VITE: {
                    String vitePath = referenceIdentifier;
                    Project projectOwner = ProjectConvertors.getNonConvertorOwner(currentFile);

                    if (projectOwner == null || projectOwner.getProjectDirectory() == null) {
                        return location;
                    }

                    FileObject assetFile = projectOwner.getProjectDirectory().getFileObject(vitePath);

                    if (assetFile != null) {
                        BladeElement resultHandle = new BladeElement(referenceIdentifier, assetFile, ElementType.ASSET_FILE);
                        DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(assetFile, 0, resultHandle);
                        if (location.equals(DeclarationLocation.NONE)) {
                            location = constantLocation;
                        }
                        location.addAlternative(new AlternativeLocationImpl(constantLocation));
                        return location;
                    }
                }
            }
        } else if ((caretRange = parserResult.getBladePhpExpressionOccurences().findPhpExpressionLocation(caretOffset)) != null) {
            int referenceOffset = caretOffset - caretRange.getStart();
            PhpElementsDeclarationService phpDeclService = new PhpElementsDeclarationService();
            PhpReference phpRef = phpDeclService.findReferenceAtCaret(info, caretRange, referenceOffset, currentFile);

            if (phpRef == null) {
                return location;
            }

            switch (phpRef.type) {
                case PHP_FUNCTION: {
                    Collection<PhpIndexFunctionResult> functionResults = PhpIndexUtils.queryExactFunctions(currentFile, phpRef.identifier);
                    return phpDeclService.buildFunctionDeclLocation(phpRef, functionResults);
                }
                case PHP_CLASS: {
                    Collection<PhpIndexResult> results;
                    if (phpRef.namespace != null) {
                        results = PhpIndexUtils.queryExactNamespaceClasses(phpRef.identifier, phpRef.namespace, currentFile);
                    } else {
                        results = PhpIndexUtils.queryExactClass(phpRef.identifier, currentFile);
                    }

                    if (results != null && !results.isEmpty()) {
                        return phpDeclService.buildDeclLocation(phpRef.identifier, ElementType.PHP_CLASS, results);
                    }

                    return location;
                }
                case PHP_METHOD: {
                    String queryNamespace = phpRef.namespace;

                    if (phpRef.ownerClass == null){
                        return location;
                    }
                    
                    Collection<PhpIndexFunctionResult> indexMethodResults = PhpIndexUtils.queryExactClassMethods(currentFile,
                            phpRef.identifier, phpRef.ownerClass.identifier, queryNamespace);

                    for (PhpIndexFunctionResult indexResult : indexMethodResults) {
                        PhpFunctionElement resultHandle = new PhpFunctionElement(
                                phpRef.identifier,
                                indexResult.declarationFile,
                                ElementType.PHP_FUNCTION,
                                indexResult.getClassNamespace(),
                                indexResult.getParams()
                        );
                        DeclarationLocation functionLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                        if (location.equals(DeclarationLocation.NONE)) {
                            location = functionLocation;
                        }
                        location.addAlternative(new AlternativeLocationImpl(functionLocation));
                    }
                    return location;
                }
                case PHP_CLASS_CONSTANT: {
                    Collection<PhpIndexResult> results = null;
                    if (phpRef.ownerClass != null) {
                        results = PhpIndexUtils.queryExactClassConstants(phpRef.identifier, phpRef.ownerClass.identifier, currentFile);
                    }
                    if (results != null && !results.isEmpty()) {
                        return phpDeclService.buildDeclLocation(phpRef.identifier, ElementType.PHP_CONSTANT, results);
                    }

                    return location;
                }
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findCustomDirectiveDeclaration(
            BladeParserResult parserResult, int caretOffset, FileObject currentFile,
            DeclarationLocation location
    ) {
        Project projectOwner = ProjectConvertors.getNonConvertorOwner(currentFile);
        if (projectOwner == null) {
            return location;
        }

        CustomDirectiveOccurence customDirectiveOccurence = parserResult.getBladeCustomDirectiveOccurences().findCustomDirectiveOccurence(caretOffset);

        if (customDirectiveOccurence == null) {
            return location;
        }

        CustomDirectives.getInstance(projectOwner).filterAction(new CustomDirectives.FilterCallbackDeclaration(location) {
            @Override
            public void filterDirectiveName(CustomDirective directive, FileObject file) {
                if (directive.getName().equals(customDirectiveOccurence.directiveName)) {
                    BladeElement customDirectiveHandle = new BladeElement(customDirectiveOccurence.directiveName, file, ElementType.CUSTOM_DIRECTIVE);
                    DeclarationFinder.DeclarationLocation newLoc = new DeclarationFinder.DeclarationLocation(file, directive.getOffset(), customDirectiveHandle);
                    this.getLocation().addAlternative(new AlternativeLocationImpl(newLoc));
                }
            }
        });

        if (!location.getAlternativeLocations().isEmpty()) {
            for (AlternativeLocation loc : location.getAlternativeLocations()) {
                location = loc.getLocation();
            }
        }
        return location;
    }

    private DeclarationLocation findComponentClassDeclaration(
            TokenHierarchy<?> th, int caretOffset, FileObject currentFile, DeclarationLocation location) {
        Project projectOwner = ProjectConvertors.getNonConvertorOwner(currentFile);
        if (projectOwner == null) {
            return location;
        }

        Token<? extends HTMLTokenId> htmlToken = BladeLexerUtils.getHtmlToken(th, caretOffset);

        if (htmlToken == null) {
            return location;
        }

        HTMLTokenId htmlTokenId = htmlToken.id();

        if (htmlTokenId.equals(HTMLTokenId.TAG_OPEN)) {
            String tag = htmlToken.text().toString();
            if (!tag.startsWith(COMPONENT_TAG_NAME_PREFIX)) {
                return location;
            }
            ComponentsQueryService componentComplervice = new ComponentsQueryService();
            String className = StringUtils.kebabToCamel(tag.substring(COMPONENT_TAG_NAME_PREFIX.length()));

            Collection<PhpIndexResult> indexedReferences = componentComplervice.findComponentClass(className, currentFile);
            ComponentsSupport componentSupport = ComponentsSupport.getInstance(projectOwner);

            if (componentSupport == null) {
                return location;
            }

            for (PhpIndexResult indexReference : indexedReferences) {
                BladeElement resultHandle = new BladeElement(className, indexReference.declarationFile, ElementType.LARAVEL_COMPONENT);
                DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(indexReference.declarationFile, indexReference.getStartOffset(), resultHandle);
                if (location.equals(DeclarationLocation.NONE)) {
                    location = constantLocation;
                }
                location.addAlternative(new AlternativeLocationImpl(constantLocation));

                if (!location.equals(DeclarationLocation.NONE)) {
                    FileObject resource = componentComplervice.getComponentResourceFile(tag, indexReference.name, currentFile);
                    if (resource != null) {
                        PathElement resourceHandle = new PathElement(tag, resource);
                        DeclarationLocation resourceLocation = new DeclarationFinder.DeclarationLocation(resource, indexReference.getStartOffset(), resourceHandle);
                        location.addAlternative(new AlternativeLocationImpl(resourceLocation));
                    }
                }
            }
        }
        return location;
    }

    public static class AlternativeLocationImpl implements AlternativeLocation {

        private final DeclarationLocation location;

        public AlternativeLocationImpl(DeclarationLocation location) {
            this.location = location;
        }

        @Override
        public ElementHandle getElement() {
            return getLocation().getElement();
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            ElementHandle el = getLocation().getElement();
            if (el != null) {
                formatter.appendText(el.getName());
                if (el.getFileObject() != null) {
                    formatter.appendText(" in "); // NOI18N
                    formatter.appendText(FileUtil.getFileDisplayName(el.getFileObject()));
                }
                return formatter.getText();
            }
            return getLocation().toString();
        }

        @Override
        public DeclarationFinder.DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(DeclarationFinder.AlternativeLocation o) {
            return 0;
        }

    }

    //php context
    public OffsetRange getPhpReferenceSpan(String phpExpr, int caretOffset, int phpExprStart) {
        OffsetRange offsetRange = OffsetRange.NONE;

        int referencedOffset = caretOffset - phpExprStart;
        org.antlr.v4.runtime.Token targetetToken = BladePhpAntlrUtils.getToken(phpExpr, referencedOffset);

        if (targetetToken == null) {
            return offsetRange;
        }

        if (targetetToken.getType() == IDENTIFIER) {
            offsetRange = new OffsetRange(targetetToken.getStartIndex() + phpExprStart, phpExprStart + targetetToken.getStopIndex() + 1);
            return offsetRange;
        }

        return offsetRange;
    }

    private DeclarationLocation buildDeclarationLocationFromIndex(DeclarationLocation location,
            BladeIndex.IndexedReference indexedReference, ElementType type) {
        Reference reference = indexedReference.getReference();
        String referenceId = reference.identifier;
        FileObject originFile = indexedReference.getOriginFile();
        BladeElement referenceHandle = new BladeElement(referenceId, originFile, type);
        int startOccurence = reference.defOffset.getStart();
        DeclarationLocation declItem = new DeclarationFinder.DeclarationLocation(originFile, startOccurence, referenceHandle);

        if (location.equals(DeclarationLocation.NONE)) {
            location = declItem;
        }

        location.addAlternative(new AlternativeLocationImpl(declItem));
        return location;
    }
}
