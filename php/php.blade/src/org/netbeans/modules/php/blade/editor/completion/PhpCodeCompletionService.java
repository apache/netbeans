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
import java.util.Collection;
import java.util.List;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.blade.csl.elements.ClassElement;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.NamedElement;
import org.netbeans.modules.php.blade.editor.EditorStringUtils;
import static org.netbeans.modules.php.blade.editor.completion.BladeCompletionHandler.completionRequest;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexFunctionResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;
import static org.netbeans.modules.php.blade.editor.parser.BladeParserResult.ReferenceType.PHP_CLASS;
import static org.netbeans.modules.php.blade.editor.parser.BladeParserResult.ReferenceType.PHP_CONSTANT;
import static org.netbeans.modules.php.blade.editor.parser.BladeParserResult.ReferenceType.PHP_NAMESPACE_PATH_TYPE;
import org.netbeans.modules.php.blade.editor.parser.ParsingUtils;
import org.netbeans.modules.php.editor.csl.PHPLanguage;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class PhpCodeCompletionService {

    public String prefix = "";

    public List<CompletionProposal> getCompletionProposal(int offset, Token currentToken) {
        List<CompletionProposal> proposals = new ArrayList<>();
        String phpSnippet = currentToken.getText();
        String phpStart = "<?php ";
        if (phpSnippet.length() < 1 || currentToken.getStartIndex() < phpStart.length()) {
            return proposals;
        }
        int previousSpace = currentToken.getStartIndex() - phpStart.length();
        ParsingUtils parsingUtils = new ParsingUtils();
        String whitespaceFill = new String(new char[previousSpace]).replace("\0", " ");
        String phpSnippetText = whitespaceFill + phpStart + currentToken.getText();
        parsingUtils.parsePhpText(phpSnippetText);
        ParserResult phpParserResult = parsingUtils.getParserResult();
        if (phpParserResult == null) {
            return proposals;
        }
        CodeCompletionHandler cc = (new PHPLanguage()).getCompletionHandler();
        prefix = cc.getPrefix(phpParserResult, offset, true);

        if (prefix == null) {
            return proposals;
        }

        if (prefix.length() == 0) {
            prefix = cc.getPrefix(phpParserResult, offset - 1, true);
        }

        if (prefix == null || prefix.length() == 0) {
            return proposals;
        }

        String phpPrefix = prefix;

        CodeCompletionContext context = PhpCodeCompletionContext.completionContext(offset,
                phpParserResult, phpPrefix);

        CodeCompletionResult completionResult = cc.complete(context);
        return completionResult.getItems();
    }

    public static void completePhpCode(final List<CompletionProposal> completionProposals,
            BladeParserResult parserResult,
            int offset, String prefix) {
        BladeParserResult.FieldAccessReference fieldAccessReference = parserResult.findFieldAccessRefrence(offset);

        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        
        if (fieldAccessReference != null) {
            completeClassConstants(prefix, fieldAccessReference.ownerClass.identifier, offset, completionProposals, fo);
            completeClassMethods(prefix, fieldAccessReference, offset, completionProposals, fo);
            return;
        }

        //based on ParserResult implementation
        //inside tag {{ }} and inside directive expression differences
        BladeParserResult.Reference elementReference = parserResult.findOccuredRefrence(offset);

        if (elementReference == null) {
            completeNamespace(prefix, offset, completionProposals, fo);
            completePhpClasses(prefix, offset, completionProposals, fo);
            completePhpFunctions(prefix, offset, completionProposals, fo);
            completeConstants(prefix, offset, completionProposals, fo);
            return;
        }

        switch (elementReference.type) {
            case PHP_CONSTANT:
            case PHP_CLASS:
                completeNamespace(prefix, offset, completionProposals, fo);
                completePhpClasses(prefix, offset, completionProposals, fo);
                completeConstants(prefix, offset, completionProposals, fo);
                break;
            case PHP_NAMESPACE_PATH_TYPE:
                String prefixNamespace = elementReference.namespace != null ? elementReference.namespace + prefix : prefix;
                completeNamespace(prefixNamespace, offset, completionProposals, fo);

                //we are after '\[a-z]'
                if (elementReference.namespace != null) {
                    String classQuery = prefix;
                    String namespace = elementReference.namespace;
                    int substringStartOffset = namespace.startsWith("\\") ? 1 : 0;
                    String namespacePath = namespace.substring(substringStartOffset) + classQuery;
                    Collection<PhpIndexResult> indexedNamespaces = PhpIndexUtils.queryNamespaces(
                            fo, namespacePath, QuerySupport.Kind.PREFIX
                    );
                    BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset);

                    for (PhpIndexResult indexResult : indexedNamespaces) {
                        completionProposals.add(new BladeCompletionProposal.NamespaceItem(
                                namespaceElement(indexResult), request, indexResult.name));
                    }
                } else if (prefix.endsWith("\\")) {
                    //the identifier is the namespace
                    int substringOffset = elementReference.identifier.startsWith("\\") ? 1 : 0;
                    String namespacePath = elementReference.identifier.substring(substringOffset, elementReference.identifier.length() - 1);
                    Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryAllNamespaceClasses(fo, namespacePath);
                    BladeCompletionProposal.CompletionRequest request = completionRequest(namespacePath, offset + namespacePath.length());
                    for (PhpIndexResult indexResult : indexClassResults) {
                        completionProposals.add(new BladeCompletionProposal.ClassItem(classElement(indexResult), request, indexResult.name));
                    }
                    //completeNamespacedPhpClasses("", namespace, offset, completionProposals, parserResult);
                }
                break;
        }
    }

    private static void completePhpClasses(String prefix, int offset, 
            final List<CompletionProposal> completionProposals,
            FileObject fo) 
    {

        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryClass(fo, prefix);

        if (indexClassResults.isEmpty()) {
            return;
        }
        BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset);

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ClassItem(
                    classElement(indexResult), request, indexResult.name));
        }
    }

    private static void completePhpFunctions(String prefix, int offset,
            final List<CompletionProposal> completionProposals,
            FileObject fo) {
        Collection<PhpIndexFunctionResult> indexedFunctions = PhpIndexUtils.queryFunctions(
                fo, prefix);
        if (indexedFunctions.isEmpty()) {
            return;
        }
        BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset);
        for (PhpIndexFunctionResult indexResult : indexedFunctions) {
            //to be completed
            //might add syntax completion cursor
            String preview = indexResult.name + indexResult.getParamsAsString();
            completionProposals.add(new BladeCompletionProposal.FunctionItem(
                    functionElement(indexResult),
                    request,
                    preview)
            );
        }
    }

    /**
     * Warning this doesn't check access permission (private, protected)
     *
     * @param prefix
     * @param fieldAccessReference
     * @param offset
     * @param completionProposals
     * @param fo
     */
    private static void completeClassMethods(String prefix, BladeParserResult.FieldAccessReference fieldAccessReference,
            int offset,
            final List<CompletionProposal> completionProposals,
            FileObject fo) {
        Collection<PhpIndexFunctionResult> indexedFunctions = PhpIndexUtils.queryClassMethods(
                fo, prefix, fieldAccessReference.ownerClass.identifier,
                fieldAccessReference.ownerClass.namespace);
        if (indexedFunctions.isEmpty()) {
            return;
        }

        BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset);

        for (PhpIndexFunctionResult indexResult : indexedFunctions) {
            //to be completed
            //might add syntax completion cursor
            String preview = indexResult.name + indexResult.getParamsAsString();
            completionProposals.add(new BladeCompletionProposal.FunctionItem(
                    functionElement(indexResult),
                    request,
                    indexResult.getClassNamespace(),
                    preview)
            );
        }
    }

    private static void completeNamespace(String prefix, int offset,
            final List<CompletionProposal> completionProposals,
            FileObject fo) {

        if (!prefix.startsWith(EditorStringUtils.NAMESPACE_SEPARATOR) && !Character.isUpperCase(prefix.charAt(0))) {
            //skip lowercase string from namespce search
            return;
        }

        //TODO check if this really matters
        Project projectOwner = ProjectConvertors.getNonConvertorOwner(fo);
        if (projectOwner == null) {
            return;
        }
        int substringOffset = prefix.startsWith(EditorStringUtils.NAMESPACE_SEPARATOR) ? 1 : 0;
        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryNamespace(
                projectOwner.getProjectDirectory(), prefix.substring(substringOffset)
        );
        if (indexClassResults.isEmpty()) {
            return;
        }

        BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset + substringOffset);

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.NamespaceItem(
                    namespaceElement(indexResult), request, indexResult.name));
        }
    }

    private static void completeConstants(String prefix, int offset,
            final List<CompletionProposal> completionProposals,
            FileObject fo) 
    {
        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryConstants(fo, prefix);

        //treat only uppercase strings
        if (!Character.isUpperCase(prefix.charAt(0))){
            return;
        }
        
        if (indexClassResults.isEmpty()) {
            return;
        }

        BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset);

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ConstantItem(
                    constantElement(indexResult), request, indexResult.name));
        }
    }

    private static void completeClassConstants(String prefix, String ownerClass, int offset,
            final List<CompletionProposal> completionProposals,
            FileObject fo) {

        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryClassConstants(
                fo, prefix, ownerClass);

        //treat only uppercase strings
        if (!Character.isUpperCase(prefix.charAt(0))){
            return;
        }
        
        if (indexClassResults.isEmpty()) {
            return;
        }

        BladeCompletionProposal.CompletionRequest request = completionRequest(prefix, offset);

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ConstantItem(
                    constantElement(indexResult), request, indexResult.name));
        }
    }

    //TODO might move in a factory for NamedElement

    private static ClassElement classElement(PhpIndexResult indexResult) {
        return new ClassElement(indexResult.name,
                indexResult.namespace,
                indexResult.declarationFile);
    }
    
    private static NamedElement namespaceElement(PhpIndexResult indexResult) {
        return namedElement(indexResult, ElementType.PHP_NAMESPACE);
    }

    private static NamedElement functionElement(PhpIndexResult indexResult) {
        String inputString = indexResult.name + "()";
        return namedElement(inputString, indexResult, ElementType.PHP_FUNCTION);
    }

    private static NamedElement constantElement(PhpIndexResult indexResult) {
        return namedElement(indexResult, ElementType.PHP_CONSTANT);
    }
    
    private static NamedElement namedElement(PhpIndexResult indexResult, ElementType type) {
        return new NamedElement(indexResult.name, indexResult.declarationFile, type);
    }
    
    private static NamedElement namedElement(String preview, PhpIndexResult indexResult, ElementType type) {
        return new NamedElement(preview, indexResult.declarationFile, type);
    }
}
