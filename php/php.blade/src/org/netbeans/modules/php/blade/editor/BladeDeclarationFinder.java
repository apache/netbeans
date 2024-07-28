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
package org.netbeans.modules.php.blade.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStreams;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.NamedElement;
import org.netbeans.modules.php.blade.csl.elements.PathElement;
import org.netbeans.modules.php.blade.csl.elements.PhpFunctionElement;
import org.netbeans.modules.php.blade.editor.declaration.ComponentDeclarationService;
import org.netbeans.modules.php.blade.editor.declaration.VitePathDeclarationService;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives.CustomDirective;
import org.netbeans.modules.php.blade.editor.indexing.BladeIndex;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexFunctionResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.editor.indexing.QueryUtils;
import org.netbeans.modules.php.blade.editor.lexer.BladeLexerUtils;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult.FieldAccessReference;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult.Reference;
import org.netbeans.modules.php.blade.editor.path.BladePathUtils;
import org.netbeans.modules.php.blade.syntax.StringUtils;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;
import org.openide.filesystems.FileObject;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrUtils;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileUtil;

/**
 * focuses mainly on string inputs
 * 
 * ?? TODO implement a Extension module
 *
 * @author bhaidu
 */
public class BladeDeclarationFinder implements DeclarationFinder {

    private int currentTokenId;
    private String tokenText;

    @Override
    public OffsetRange getReferenceSpan(Document document, int caretOffset) {
        BaseDocument baseDoc = (BaseDocument) document;

        //baseDoc.readLock();
        AntlrTokenSequence tokens = null;
        OffsetRange offsetRange = OffsetRange.NONE;
        tokenText = null;
        int lineOffset = caretOffset;
        try {
            try {
                String text = baseDoc.getText(0, baseDoc.getLength());
                tokens = new AntlrTokenSequence(new BladeAntlrLexer(CharStreams.fromString(text)));
            } catch (BadLocationException ex) {
                //Exceptions.printStackTrace(ex);
            }
        } finally {
            //baseDoc.readUnlock();
        }

        //inside php expression context ??
        if (tokens == null || tokens.isEmpty()) {
            return offsetRange;
        }

        tokens.seekTo(lineOffset);

        if (tokens.hasNext()) {
            org.antlr.v4.runtime.Token nt = tokens.next().get();

            switch (nt.getType()) {
                case D_CUSTOM:
                case PHP_IDENTIFIER:
                case PHP_NAMESPACE_PATH:
                    return new OffsetRange(nt.getStartIndex(), nt.getStopIndex() + 1);
                case HTML_COMPONENT_PREFIX:
                    //direct detection
                    currentTokenId = HTML_COMPONENT_PREFIX;
                    //remove '<x-'
                    tokenText = nt.getText().length() > 3 ? nt.getText() : null;
                    return new OffsetRange(nt.getStartIndex() + 1, nt.getStopIndex() + 1);
            }

            if (!tokens.hasPrevious()) {
                return offsetRange;
            }

            if (nt.getType() == BL_PARAM_STRING || nt.getType() == EXPR_STRING) {
                List<Integer> tokensToMatch = BladeLexerUtils.tokensWithIdentifiableParam();
                List<Integer> tokensStop = Arrays.asList(new Integer[]{HTML});
                org.antlr.v4.runtime.Token matchedToken = BladeAntlrUtils.findBackward(tokens, tokensToMatch, tokensStop);
                int offsetCorrection = caretOffset - lineOffset;
                if (matchedToken != null) {
                    offsetRange = new OffsetRange(nt.getStartIndex() + offsetCorrection, nt.getStopIndex() + offsetCorrection + 1);
                }
                return offsetRange;
            }
        }
        return offsetRange;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        BladeParserResult parserResult = (BladeParserResult) info;

        FileObject currentFile = parserResult.getFileObject();
        DeclarationLocation location = DeclarationLocation.NONE;

        if (tokenText != null && currentTokenId == HTML_COMPONENT_PREFIX) {
            String componentId = tokenText.substring(3);
            String className = StringUtils.kebabToCamel(componentId);
            ComponentDeclarationService componentComplervice = new ComponentDeclarationService();
            Collection<PhpIndexResult> indexedReferences = componentComplervice.queryComponents(className, currentFile);

            for (PhpIndexResult indexReference : indexedReferences) {
                NamedElement resultHandle = new NamedElement(className, indexReference.declarationFile, ElementType.LARAVEL_COMPONENT);
                DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(indexReference.declarationFile, indexReference.getStartOffset(), resultHandle);
                if (location.equals(DeclarationLocation.NONE)) {
                    location = constantLocation;
                }
                location.addAlternative(new AlternativeLocationImpl(constantLocation));

                if (!location.equals(DeclarationLocation.NONE)) {
                    FileObject resource = componentComplervice.getComponentResourceFile(componentId, indexReference.name, currentFile);
                    if (resource != null) {
                        PathElement resourceHandle = new PathElement(componentId, resource);
                        DeclarationLocation resourceLocation = new DeclarationFinder.DeclarationLocation(resource, indexReference.getStartOffset(), resourceHandle);
                        location.addAlternative(new AlternativeLocationImpl(resourceLocation));
                    }
                }
            }
            return location;
        }

        FieldAccessReference fieldAccessReference = parserResult.findFieldAccessRefrence(caretOffset);

        if (fieldAccessReference != null) {
            switch (fieldAccessReference.type) {
                case STATIC_FIELD_ACCESS:
                    switch (fieldAccessReference.fieldType) {
                        case CONSTANT:
                            Collection<PhpIndexResult> indexConstantsResults = PhpIndexUtils.queryExactClassConstants(currentFile, fieldAccessReference.fieldName, fieldAccessReference.ownerClass.identifier);
                            for (PhpIndexResult indexResult : indexConstantsResults) {
                                NamedElement resultHandle = new NamedElement(fieldAccessReference.fieldName, indexResult.declarationFile, ElementType.PHP_CONSTANT);
                                DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                                if (location.equals(DeclarationLocation.NONE)) {
                                    location = constantLocation;
                                }
                                location.addAlternative(new AlternativeLocationImpl(constantLocation));
                            }
                            return location;
                    }
            }
        }

        Reference reference = parserResult.findOccuredRefrence(caretOffset);

        if (reference == null) {
            return location;
        }

        Project projectOwner = ProjectConvertors.getNonConvertorOwner(currentFile);

        if (projectOwner == null) {
            return location;
        }

        FileObject sourceFolder = projectOwner.getProjectDirectory();
        String referenceIdentifier = reference.identifier;
        
        switch (reference.type) {
            case EXTENDS:
            case INCLUDE:
            case INCLUDE_IF:
            case EACH:
            case INCLUDE_COND:
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
            case SECTION:
            case HAS_SECTION:
            case SECTION_MISSING:
                String yieldId = referenceIdentifier;
                List<BladeIndex.IndexedReference> yields = QueryUtils.findYieldReferences(yieldId, currentFile);
                if (yields == null) {
                    return DeclarationLocation.NONE;
                }

                for (BladeIndex.IndexedReference yieldReference : yields) {
                    String yieldReferenceId = yieldReference.getReference().identifier;
                    NamedElement yieldIdHandle = new NamedElement(yieldReferenceId,
                            yieldReference.getOriginFile(), ElementType.YIELD_ID);
                    int startOccurence = yieldReference.getReference().defOffset.getStart();
                    DeclarationLocation dlyieldItem = new DeclarationFinder.DeclarationLocation(yieldReference.getOriginFile(), startOccurence, yieldIdHandle);
                    if (location.equals(DeclarationLocation.NONE)) {
                        location = dlyieldItem;
                    }
                    location.addAlternative(new AlternativeLocationImpl(dlyieldItem));
                }

                return location;
            case PUSH:
            case PUSH_IF:
            case PREPEND:
                String stackId = referenceIdentifier;
                List<BladeIndex.IndexedReference> stacks = QueryUtils.queryStacksReferences(stackId, currentFile);

                if (stacks == null) {
                    return DeclarationLocation.NONE;
                }

                for (BladeIndex.IndexedReference stackReference : stacks) {
                    String stackReferenceId = stackReference.getReference().identifier;
                    NamedElement yieldIdHandle = new NamedElement(stackReferenceId, stackReference.getOriginFile(), ElementType.STACK_ID);
                    int startOccurence = stackReference.getReference().defOffset.getStart();
                    DeclarationLocation dlstack = new DeclarationFinder.DeclarationLocation(stackReference.getOriginFile(), startOccurence, yieldIdHandle);
                    if (location.equals(DeclarationLocation.NONE)) {
                        location = dlstack;
                    }
                    location.addAlternative(new AlternativeLocationImpl(dlstack));
                }

                return location;
            case CUSTOM_DIRECTIVE:
                String directiveNameFound = reference.identifier;
                DeclarationLocation dlcustomDirective = DeclarationLocation.NONE;

                CustomDirectives.getInstance(projectOwner).filterAction(new CustomDirectives.FilterCallbackDeclaration(dlcustomDirective) {
                    @Override
                    public void filterDirectiveName(CustomDirective directive, FileObject file) {
                        if (directive.name.equals(directiveNameFound)) {
                            NamedElement customDirectiveHandle = new NamedElement(directiveNameFound, file, ElementType.CUSTOM_DIRECTIVE);
                            DeclarationFinder.DeclarationLocation newLoc = new DeclarationFinder.DeclarationLocation(file, directive.offset, customDirectiveHandle);
                            this.location.addAlternative(new AlternativeLocationImpl(newLoc));
                        }
                    }
                });

                if (!dlcustomDirective.getAlternativeLocations().isEmpty()) {
                    for (AlternativeLocation loc : dlcustomDirective.getAlternativeLocations()) {
                        dlcustomDirective = loc.getLocation();
                    }
                }
                return dlcustomDirective;
            case PHP_CLASS:
                Collection<PhpIndexResult> indexClassResults;
                String namespace = reference.namespace;

                if (namespace != null && reference.namespace.length() > 2) {
                    int subOffset = reference.namespace.startsWith("\\") ? 1 : 0;
                    indexClassResults = PhpIndexUtils.queryExactNamespaceClasses(reference.identifier,
                            reference.namespace.substring(subOffset, reference.namespace.length() - 1),
                            sourceFolder
                    );
                } else {
                    indexClassResults = PhpIndexUtils.queryExactClass(sourceFolder, reference.identifier);
                }

                for (PhpIndexResult indexResult : indexClassResults) {
                    NamedElement resultHandle = new NamedElement(referenceIdentifier, indexResult.declarationFile, ElementType.PHP_CLASS);
                    DeclarationLocation classLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                    if (location.equals(DeclarationLocation.NONE)) {
                        location = classLocation;
                    }
                    location.addAlternative(new AlternativeLocationImpl(classLocation));
                }
                return location;
            case PHP_METHOD:{
                if (reference.ownerClass == null) {
                    return location;
                }
                String queryNamespace = reference.namespace;
                if (queryNamespace != null && queryNamespace.length() > 2) {
                    int subOffset = queryNamespace.startsWith("\\") ? 1 : 0;
                    int endOffset = queryNamespace.endsWith("\\") ? queryNamespace.length() - 1 : queryNamespace.length();
                    queryNamespace = queryNamespace.substring(subOffset, endOffset);
                } else {
                    queryNamespace = null;
                }
                Collection<PhpIndexFunctionResult> indexMethodResults = PhpIndexUtils.queryExactClassMethods(sourceFolder,
                        referenceIdentifier, reference.ownerClass, queryNamespace);
                
                
                for (PhpIndexFunctionResult indexResult : indexMethodResults) {
                    PhpFunctionElement resultHandle = new PhpFunctionElement(
                            referenceIdentifier,
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
            case PHP_FUNCTION:
                Collection<PhpIndexFunctionResult> indexResults = PhpIndexUtils.queryExactFunctions(sourceFolder, reference.identifier);

                for (PhpIndexFunctionResult indexResult : indexResults) {
                    PhpFunctionElement resultHandle = new PhpFunctionElement(
                            referenceIdentifier,
                            indexResult.declarationFile,
                            ElementType.PHP_FUNCTION,
                            indexResult.getParams()
                    );
                    DeclarationLocation functionLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                    if (location.equals(DeclarationLocation.NONE)) {
                        location = functionLocation;
                    }
                    location.addAlternative(new AlternativeLocationImpl(functionLocation));
                }
                return location;
            case PHP_CONSTANT:
                Collection<PhpIndexResult> indexConstantsResults = PhpIndexUtils.queryExactConstants(sourceFolder, reference.identifier);

                for (PhpIndexResult indexResult : indexConstantsResults) {
                    NamedElement resultHandle = new NamedElement(referenceIdentifier, indexResult.declarationFile, ElementType.PHP_CONSTANT);
                    DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                    if (location.equals(DeclarationLocation.NONE)) {
                        location = constantLocation;
                    }
                    location.addAlternative(new AlternativeLocationImpl(constantLocation));
                }
                return location;
            case USE:
            case INJECT:
            case PHP_NAMESPACE_PATH_TYPE:{
                Collection<PhpIndexResult> indexNamespaceResults;
                if (reference.namespace != null) {
                    int subOffset = reference.namespace.startsWith("\\") ? 1 : 0;
                    String namespacePath = reference.namespace.substring(subOffset);
                    indexNamespaceResults = PhpIndexUtils.queryExactNamespaceClasses(reference.identifier,
                            namespacePath,
                            sourceFolder
                    );
                    for (PhpIndexResult indexResult : indexNamespaceResults) {
                        NamedElement resultHandle = new NamedElement(referenceIdentifier, indexResult.declarationFile, ElementType.PHP_CLASS);
                        DeclarationLocation classLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                        if (location.equals(DeclarationLocation.NONE)) {
                            location = classLocation;
                        }
                        location.addAlternative(new AlternativeLocationImpl(classLocation));
                    }
                } else {
                    indexNamespaceResults = PhpIndexUtils.queryNamespace(sourceFolder, reference.identifier);

                    for (PhpIndexResult indexResult : indexNamespaceResults) {
                        NamedElement resultHandle = new NamedElement(referenceIdentifier, indexResult.declarationFile, ElementType.PHP_NAMESPACE);
                        DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
                        if (location.equals(DeclarationLocation.NONE)) {
                            location = constantLocation;
                        }
                        location.addAlternative(new AlternativeLocationImpl(constantLocation));
                    }
                }
                return location;
            }
            case VITE_PATH:
                VitePathDeclarationService vitePathDeclService = new VitePathDeclarationService(sourceFolder);
                FileObject viteAssetFile = vitePathDeclService.findFileObject(referenceIdentifier);
                if (viteAssetFile == null || !viteAssetFile.isValid()){
                    return location;
                }
                NamedElement resultHandle = new NamedElement(referenceIdentifier, viteAssetFile, ElementType.ASSET_FILE);
                DeclarationLocation constantLocation = new DeclarationFinder.DeclarationLocation(viteAssetFile, 0, resultHandle);
                if (location.equals(DeclarationLocation.NONE)) {
                    location = constantLocation;
                }
                location.addAlternative(new AlternativeLocationImpl(constantLocation));
                return location;
        }

        return DeclarationLocation.NONE;
    }

    private static class AlternativeLocationImpl implements AlternativeLocation {

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
                    formatter.appendText(" in ");
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
}
