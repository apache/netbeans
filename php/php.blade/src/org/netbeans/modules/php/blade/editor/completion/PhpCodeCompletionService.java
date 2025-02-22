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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.blade.csl.elements.ClassElement;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.NamedElement;
import org.netbeans.modules.php.blade.csl.elements.PhpKeywordElement;
import org.netbeans.modules.php.blade.editor.EditorStringUtils;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexFunctionResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexUtils;
import org.netbeans.modules.php.blade.syntax.annotation.PhpKeyword;
import org.netbeans.modules.php.blade.syntax.antlr4.php.BladePhpAntlrLexer;
import org.netbeans.modules.php.blade.syntax.antlr4.php.BladePhpAntlrUtils;
import org.netbeans.modules.php.blade.syntax.antlr4.php.BladePhpSnippetParser;
import static org.netbeans.modules.php.blade.syntax.antlr4.php.BladePhpSnippetParser.PhpReferenceType.PHP_NAMESPACE;
import org.netbeans.modules.php.blade.syntax.php.PhpKeywordList;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class PhpCodeCompletionService {

    public static void completePhpCode(final List<CompletionProposal> completionProposals,
            String snapshotExpr, int exprStart, int offset, FileObject fo) {
        int referencedOffset = offset - exprStart - 1;

        org.antlr.v4.runtime.Token targetetToken = BladePhpAntlrUtils.getToken(snapshotExpr, referencedOffset);

        if (targetetToken == null) {
            return;
        }

        int completionOffset = exprStart + targetetToken.getStartIndex();

        String phpIdentifier = ""; // NOI18N

        if (targetetToken.getType() != BladePhpAntlrLexer.DOUBLE_COLON) {
            phpIdentifier = targetetToken.getText();
        } else {
            //double colon offset increase "::"
            completionOffset += targetetToken.getText().length();
        }

        BladePhpSnippetParser phpSnippetParser = new BladePhpSnippetParser(snapshotExpr, fo, exprStart);
        phpSnippetParser.parse();
        BladePhpSnippetParser.PhpReference phpRef = phpSnippetParser.findIdentifierReference(referencedOffset);
        BladePhpSnippetParser.FieldAcces fieldAccess = phpSnippetParser.findFieldAccessReference(referencedOffset);

        if (fieldAccess != null) {
            if (fieldAccess.owner.namespace == null) {
                completeClassConstants(completionProposals, phpIdentifier, fieldAccess.owner.identifier, completionOffset, fo);
            }
            completeClassMethods(completionProposals, phpIdentifier, fieldAccess, completionOffset, fo);
        } else if (phpRef != null) {
            if (phpRef.namespace != null){
                boolean globalNamespace = phpRef.namespace.startsWith(EditorStringUtils.NAMESPACE_SEPARATOR);
                String namespaceQuery = globalNamespace ? phpRef.namespace.substring(1) : phpRef.namespace;
                
                if (phpRef.identifier != null){
                    completeNamespaceClasses(completionProposals, phpRef.identifier, namespaceQuery, completionOffset, fo);
                    completeNamespace(completionProposals, namespaceQuery + EditorStringUtils.NAMESPACE_SEPARATOR + phpRef.identifier, completionOffset, fo);
                } else {
                    completeNamespace(completionProposals, namespaceQuery, completionOffset, fo);
                }
            } else if (phpRef.type.equals(PHP_NAMESPACE)){
                  completeAllRelativeNamespacesClasses(completionProposals, phpRef.identifier, offset, fo);
            }
        } else if (targetetToken.getType() == BladePhpAntlrLexer.IDENTIFIER) {
            //no context but with identifier
            completePhpKeywords(completionProposals, phpIdentifier, completionOffset);
            completePhpFunctions(completionProposals, phpIdentifier, completionOffset, fo);
            completePhpClasses(completionProposals, phpIdentifier, completionOffset, fo);
            completeNamespace(completionProposals, phpIdentifier, completionOffset + 1, fo);
        }
        //add variable flow

    }

    private static void completePhpKeywords(final List<CompletionProposal> completionProposals,
            String prefix, int caretOffset) {
        PhpKeywordList keywordList = new PhpKeywordList();
        for (PhpKeyword keyword : keywordList.getKeywords()) {
            if (keyword.name().startsWith(prefix)) {
                PhpKeywordElement keywordEl = new PhpKeywordElement(keyword.name());
                completionProposals.add(new PhpKeywordProposal(keywordEl, caretOffset));
            }
        }
    }

    private static void completePhpClasses(final List<CompletionProposal> completionProposals,
            String prefix, int offset, FileObject fo) {

        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryClass(fo, prefix);

        if (indexClassResults.isEmpty()) {
            return;
        }

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ClassItem(
                    classElement(indexResult), offset, indexResult.name, true));
        }
    }

    private static void completePhpFunctions(final List<CompletionProposal> completionProposals,
            String prefix, int offset, FileObject fo) {
        Collection<PhpIndexFunctionResult> indexedFunctions = PhpIndexUtils.queryFunctions(
                fo, prefix);
        if (indexedFunctions.isEmpty()) {
            return;
        }

        for (PhpIndexFunctionResult indexResult : indexedFunctions) {
            String preview = indexResult.name + indexResult.getParamsAsString();
            completionProposals.add(new BladeCompletionProposal.FunctionItem(
                    functionElement(indexResult),
                    offset,
                    preview)
            );
        }
    }

    private static void completeNamespace(final List<CompletionProposal> completionProposals,
            String prefix, int offset, FileObject fo) {

        int substringOffset = prefix.startsWith(EditorStringUtils.NAMESPACE_SEPARATOR) ? 1 : 0;
        
        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryNamespace(
                fo, prefix.substring(substringOffset)
        );

        if (indexClassResults.isEmpty()) {
            return;
        }

        int firstSeparator = Math.max(0, prefix.lastIndexOf(EditorStringUtils.NAMESPACE_SEPARATOR));
        int anchorOffset = offset - firstSeparator - 1;
        for (PhpIndexResult indexResult : indexClassResults) {
            if (!indexResult.name.startsWith(prefix)){
                continue;
            }
            completionProposals.add(new BladeCompletionProposal.NamespaceItem(
                    namespaceElement(indexResult), anchorOffset, indexResult.name));
        }
    }
    
    private static void completeAllRelativeNamespacesClasses(final List<CompletionProposal> completionProposals,
            String prefix, int offset, FileObject fo) {

        int substringOffset = prefix.startsWith(EditorStringUtils.NAMESPACE_SEPARATOR) ? 1 : 0;
        
        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryAllNamespaceClasses(
                fo, prefix.substring(substringOffset)
        );

        if (indexClassResults.isEmpty()) {
            return;
        }

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ClassItem(
                    classElement(indexResult), offset, indexResult.name, false));
        }
    }
    
    private static void completeNamespaceClasses(final List<CompletionProposal> completionProposals,
            String prefix, String namespace, int offset, FileObject fo) {

        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryNamespaceClassesName(fo, prefix, namespace) ;

        if (indexClassResults.isEmpty()) {
            return;
        }

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ClassItem(
                    classElement(indexResult), offset, indexResult.name, false));
        }
    }

    private static void completeClassConstants(final List<CompletionProposal> completionProposals,
            String prefix, String ownerClass, int offset, FileObject fo) {

        Collection<PhpIndexResult> indexClassResults = PhpIndexUtils.queryClassConstants(
                fo, prefix, ownerClass);

        //treat only uppercase strings
        if (prefix.length() > 0 && !Character.isUpperCase(prefix.charAt(0))) {
            return;
        }

        if (indexClassResults.isEmpty()) {
            return;
        }

        for (PhpIndexResult indexResult : indexClassResults) {
            completionProposals.add(new BladeCompletionProposal.ConstantItem(
                    constantElement(indexResult), offset, indexResult.name));
        }
    }

    /**
     * warning this doesn't check visibility : public, private, protected
     *
     * @param completionProposals
     * @param prefix
     * @param fieldAccessReference
     * @param offset
     * @param fo
     */
    private static void completeClassMethods(final List<CompletionProposal> completionProposals,
            String prefix, BladePhpSnippetParser.FieldAcces fieldAccessReference,
            int offset, FileObject fo) {
        Collection<PhpIndexFunctionResult> indexedFunctions = PhpIndexUtils.queryClassMethods(
                fo, prefix, fieldAccessReference.owner.identifier,
                fieldAccessReference.owner.namespace, fieldAccessReference.type);

        if (indexedFunctions.isEmpty()) {
            return;
        }

        for (PhpIndexFunctionResult indexResult : indexedFunctions) {
            String preview = indexResult.name + indexResult.getParamsAsString();
            completionProposals.add(new BladeCompletionProposal.FunctionItem(
                    functionElement(indexResult),
                    offset,
                    indexResult.getClassNamespace(),
                    preview)
            );
        }
    }

    private static ClassElement classElement(PhpIndexResult indexResult) {
        return new ClassElement(indexResult.name,
                indexResult.namespace,
                indexResult.declarationFile);
    }

    private static NamedElement namespaceElement(PhpIndexResult indexResult) {
        return namedElement(indexResult, ElementType.PHP_NAMESPACE);
    }

    private static NamedElement functionElement(PhpIndexResult indexResult) {
        String inputString = indexResult.name + "()"; // NOI18N
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

    public static int computeAnchorOffset(@NonNull String prefix, int offset) {
        return offset - prefix.length();
    }

    public abstract static class PhpCompletionProposal implements CompletionProposal {

        private final ElementHandle element;
        private final int anchorOffset;
        @NullAllowed
        private final String description;

        public PhpCompletionProposal(ElementHandle element, int anchorOffset, String description) {
            this.element = element;
            this.anchorOffset = anchorOffset;
            this.description = description;
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public String getSortText() {
            return getName();
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName();
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }

        @Override
        public String getInsertPrefix() {
            return getName();

        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return ""; // NOI18N
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        public String getDescription() {
            return description;
        }

    }

    public static class PhpKeywordProposal extends PhpCompletionProposal {

        public PhpKeywordProposal(ElementHandle element, int anchorOffset) {
            super(element, anchorOffset, null);
        }

    }
}
