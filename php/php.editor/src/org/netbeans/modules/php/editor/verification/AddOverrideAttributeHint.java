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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import static org.netbeans.modules.php.editor.PredefinedSymbols.Attributes.OVERRIDE;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;

/**
 * Add/Remove #[\Override] Attributes.
 */
public class AddOverrideAttributeHint extends HintRule {

    private static final String HINT_ID = "Add.Override.Attribute"; // NOI18N

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages({
        "AddOverrideAttributeSuggestion.description=Add #[\\Override] Attribute"
    })
    public String getDescription() {
        return Bundle.AddOverrideAttributeSuggestion_description();
    }

    @Override
    @NbBundle.Messages({
        "AddOverrideAttributeSuggestion.displayName=Add #[\\Override] Attribute"
    })
    public String getDisplayName() {
        return Bundle.AddOverrideAttributeSuggestion_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final BaseDocument document = context.doc;
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null && hasOverrideAttribute(fileObject)) {
            FileScope fileScope = context.fileScope;
            ElementQuery.Index index = context.getIndex();
            Map<String, Set<String>> types = getTypesAndInheritedMethods(index, fileScope);
            final CheckVisitor checkVisitor = new CheckVisitor(types, fileScope);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            TokenHierarchy<?> tokenHierarchy = phpParseResult.getSnapshot().getTokenHierarchy();
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(tokenHierarchy, 0);
            addAddOverrideHints(checkVisitor, hints, document, fileObject, ts);
            addRemoveOverrideHints(checkVisitor, hints, document, fileObject, ts);
        }
    }

    private Map<String, Set<String>> getTypesAndInheritedMethods(ElementQuery.Index index, FileScope fileScope) {
        List<TypeScope> declaredTypes = getDeclaredTypes(fileScope);
        Map<String, Set<String>> typesAndInheritedMethods = new HashMap<>();
        for (TypeScope typeScope : declaredTypes) {
            if (CancelSupport.getDefault().isCancelled()) {
                Collections.emptyMap();
            }
            Set<String> inheritedMethods = toNames(getInheritedMethods(typeScope, index));
            typesAndInheritedMethods.put(typeScope.getName(), inheritedMethods);
        }
        return typesAndInheritedMethods;
    }

    private List<TypeScope> getDeclaredTypes(FileScope fileScope) {
        List<TypeScope> declaredTypes = new ArrayList<>();
        declaredTypes.addAll(ModelUtils.getDeclaredClasses(fileScope));
        declaredTypes.addAll(ModelUtils.getDeclaredInterfaces(fileScope));
        declaredTypes.addAll(ModelUtils.getDeclaredEnums(fileScope));
        return declaredTypes;
    }

    private void addAddOverrideHints(CheckVisitor checkVisitor, List<Hint> hints, BaseDocument document, FileObject fileObject, TokenSequence<PHPTokenId> ts) {
        for (MethodDeclaration method : checkVisitor.getMissingOverrideAttributeMethods()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Identifier methodName = method.getFunction().getFunctionName();
            AddOverrideFix fix = new AddOverrideFix(document, method, ts);
            hints.add(new Hint(AddOverrideAttributeHint.this,
                    fix.getDescription(),
                    fileObject,
                    new OffsetRange(methodName.getStartOffset(), methodName.getEndOffset()),
                    Collections.<HintFix>singletonList(fix), 500));
        }
    }

    private void addRemoveOverrideHints(CheckVisitor checkVisitor, List<Hint> hints, BaseDocument document, FileObject fileObject, TokenSequence<PHPTokenId> ts) {
        for (InvalidOverrideAttributeMethod invalidOverrideMethod : checkVisitor.getInvalidOverrideAttributeMethods()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            AttributeDeclaration overrideDeclaration = invalidOverrideMethod.getOverrideAttributeDeclaration();
            Expression attributeName = overrideDeclaration.getAttributeName();
            RemoveOverrideFix fix = new RemoveOverrideFix(document, invalidOverrideMethod, ts);
            hints.add(new Hint(AddOverrideAttributeHint.this,
                    fix.getDescription(),
                    fileObject,
                    new OffsetRange(attributeName.getStartOffset(), attributeName.getEndOffset()),
                    Collections.<HintFix>singletonList(fix), 500));
        }
    }

    protected boolean hasOverrideAttribute(FileObject fileObject) {
        PhpVersion phpVersion = CodeUtils.getPhpVersion(fileObject);
        return phpVersion.hasOverrideAttribute();
    }

    private Set<MethodElement> getInheritedMethods(final TypeScope typeScope, final ElementQuery.Index index) {
        Set<MethodElement> inheritedMethods = new HashSet<>();
        Set<MethodElement> accessibleSuperMethods = new HashSet<>();
        addAccessibleClassMethods(typeScope, index, accessibleSuperMethods);
        addAccessibleInterfaceMethods(typeScope, index, accessibleSuperMethods);
        addAccessibleAbstractTraitMethods(typeScope, index, accessibleSuperMethods);
        // if some trait methods have #[\Override], also check wheter the parent class has them
        // however, attributes are not indexed at the moment
        // so, we should improve the indexer
        // e.g.
        // trait T {
        //     #[\Override]
        //     public function traitMethod(): void {}
        // }
        // class C {
        //     use T;
        // }
        inheritedMethods.addAll(accessibleSuperMethods);
        return inheritedMethods;
    }

    private void addAccessibleClassMethods(TypeScope typeScope, ElementQuery.Index index, Set<MethodElement> accessibleSuperMethods) {
        Collection<? extends ClassScope> superClasses = getSuperClasses(typeScope);
        for (ClassScope cls : superClasses) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Set<MethodElement> accessibleMethods = index.getAccessibleMethods(cls, typeScope);
            accessibleSuperMethods.addAll(accessibleMethods);
        }
    }

    private void addAccessibleInterfaceMethods(TypeScope typeScope, ElementQuery.Index index, Set<MethodElement> accessibleSuperMethods) {
        Collection<? extends InterfaceScope> superInterface = typeScope.getSuperInterfaceScopes();
        for (InterfaceScope interfaceScope : superInterface) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            accessibleSuperMethods.addAll(index.getAccessibleMethods(interfaceScope, typeScope));
        }
    }

    private void addAccessibleAbstractTraitMethods(TypeScope typeScope, ElementQuery.Index index, Set<MethodElement> accessibleSuperMethods) {
        Collection<? extends TraitScope> traits = getTraits(typeScope);
        for (TraitScope traitScope : traits) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ElementFilter abstractMethodFilter = new ElementFilter() {
                @Override
                public boolean isAccepted(PhpElement element) {
                    if (element instanceof MethodElement) {
                        MethodElement method = (MethodElement) element;
                        return method.isAbstract();
                    }
                    return false;
                }
            };
            // the following case is a fatal error because a trait is not a parent:
            // "C::traitMethod() has #[\Override] attribute, but no matching parent method"
            // trait T {
            //     public function traitMethod(): void {}
            // }
            // class C {
            //     use T;
            //     #[\Override] // fatal error
            //     public function traitMethod(): void {}
            // }
            Set<MethodElement> accessibleTraitMethods = index.getAccessibleMethods(traitScope, typeScope);
            accessibleSuperMethods.addAll(abstractMethodFilter.filter(accessibleTraitMethods));
        }
    }

    private Collection<? extends ClassScope> getSuperClasses(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return ((ClassScope) typeScope).getSuperClasses();
        }
        return Collections.emptyList();
    }

    private Collection<? extends TraitScope> getTraits(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return ((ClassScope) typeScope).getTraits();
        } else if (typeScope instanceof TraitScope) {
            return ((TraitScope) typeScope).getTraits();
        } else if (typeScope instanceof EnumScope) {
            return ((EnumScope) typeScope).getTraits();
        }
        return Collections.emptyList();
    }

    private static Set<String> toNames(Set<? extends PhpElement> elements) {
        Set<String> names = new HashSet<>();
        for (PhpElement elem : elements) {
            String name = elem.getName();
            if (!StringUtils.isEmpty(name)) {
                names.add(name);
            }
        }
        return names;
    }

    //~ inner classes
    private static class CheckVisitor extends DefaultVisitor {

        private final Map<String, Set<String>> typesAndInheritedMethods;
        private final Set<MethodDeclaration> missingOverrideAttributeMethods = new HashSet<>();
        private final Set<InvalidOverrideAttributeMethod> invalidOverrideAttributeMethods = new HashSet<>();
        private String currentTypeName = null;
        private NamespaceDeclaration currentNamespace = null;
        private final FileScope fileScope;

        public CheckVisitor(Map<String, Set<String>> typesAndInheritedMethods, FileScope fileScope) {
            this.typesAndInheritedMethods = new HashMap<>(typesAndInheritedMethods);
            this.fileScope = fileScope;
        }

        @Override
        public void visit(NamespaceDeclaration namespace) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            currentNamespace = namespace;
            super.visit(namespace);
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // anonymous class can be nested
            // so, keep the currernt type name to the variable temporally
            // e.g.
            // class C implements I {
            //     public function interfaceMethod1(): void {}
            //     public function method(): void {
            //         $anon = new class() implements I {
            //             public function interfaceMethod1(): void {}
            //             public function nestedAnonymousClass(): void {
            //                 $nestedAnon = new class() implements I {
            //                     public function interfaceMethod1(): void {}
            //                 };
            //             }
            //             public function interfaceMethod2(): void {}
            //         };
            //     }
            //     public function interfaceMethod2(): void {}
            // }
            String originalCurrentTypeName = currentTypeName;
            currentTypeName = CodeUtils.extractClassName(node);
            super.visit(node);
            currentTypeName = originalCurrentTypeName;
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            currentTypeName = node.getName().getName();
            super.visit(node);
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            currentTypeName = node.getName().getName();
            super.visit(node);
        }

        @Override
        public void visit(EnumDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            currentTypeName = node.getName().getName();
            super.visit(node);
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // don't check traits
            currentTypeName = null;
            super.visit(node);
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            String methodName = node.getFunction().getFunctionName().getName();
            if (currentTypeName != null) {
                Set<String> inheritedMethods = typesAndInheritedMethods.get(currentTypeName);
                if (inheritedMethods == null) {
                    return;
                }
                Pair<Attribute, AttributeDeclaration> overrideAttribute = getOverrideAttribute(node);
                boolean hasOverride = overrideAttribute != null;
                boolean isInheritedMethod = inheritedMethods.contains(methodName);
                if (isInheritedMethod && !hasOverride) {
                    missingOverrideAttributeMethods.add(node);
                } else if (!isInheritedMethod && hasOverride) {
                    invalidOverrideAttributeMethods.add(new InvalidOverrideAttributeMethod(methodName, overrideAttribute));
                }
            }
            super.visit(node);
        }

        @CheckForNull
        private Pair<Attribute, AttributeDeclaration> getOverrideAttribute(MethodDeclaration method) {
            for (Attribute attribute : method.getAttributes()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return null;
                }
                for (AttributeDeclaration attributeDeclaration : attribute.getAttributeDeclarations()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return null;
                    }
                    Expression attributeNameExpression = attributeDeclaration.getAttributeName();
                    String attributeName = CodeUtils.extractQualifiedName(attributeNameExpression);
                    if (isOverrideAttibute(attributeName, attributeNameExpression.getStartOffset())) {
                        return Pair.of(attribute, attributeDeclaration);
                    }
                }
            }
            return null;
        }

        public Set<InvalidOverrideAttributeMethod> getInvalidOverrideAttributeMethods() {
            return Collections.unmodifiableSet(invalidOverrideAttributeMethods);
        }

        public Set<MethodDeclaration> getMissingOverrideAttributeMethods() {
            return Collections.unmodifiableSet(missingOverrideAttributeMethods);
        }

        private boolean isOverrideAttibute(String attributeName, int offset) {
            if (OVERRIDE.getFqName().equals(attributeName)) {
                return true;
            }
            if (OVERRIDE.getName().equals(attributeName)) {
                Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
                if (isGlobalNamespace()) {
                    return true;
                }
                NamespaceName name = currentNamespace.getName(); // null is checked in isGlobalNamespace()
                QualifiedName currentNamespaceName = QualifiedName.create(name);
                NamespaceScope namespaceScope = null;
                for (NamespaceScope declaredNamespace : declaredNamespaces) {
                    QualifiedName namespaceName = declaredNamespace.getNamespaceName();
                    if (currentNamespaceName.equals(namespaceName)) {
                        namespaceScope = declaredNamespace;
                        break;
                    }
                }
                if (namespaceScope != null) {
                    // check FQ name because there may be `use \Override;`
                    QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(OVERRIDE.getName()), offset, namespaceScope);
                    if (OVERRIDE.getFqName().equals(fullyQualifiedName.toString())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isGlobalNamespace() {
            return currentNamespace == null
                    || currentNamespace.getName() == null;
        }
    }

    private static boolean isComma(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN
                && TokenUtilities.textEquals(token.text(), ","); // NOI18N
    }

    private static boolean isWhitespace(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.WHITESPACE;
    }

    //~ inner classes
    private static final class InvalidOverrideAttributeMethod {

        private final String methodName;
        private final Attribute attribute;
        private final AttributeDeclaration overrideAttributeDeclaration;

        public InvalidOverrideAttributeMethod(String methodName, Pair<Attribute, AttributeDeclaration> attribute) {
            this.methodName = methodName;
            this.attribute = attribute.first();
            this.overrideAttributeDeclaration = attribute.second();
        }

        public String getMethodName() {
            return methodName;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public AttributeDeclaration getOverrideAttributeDeclaration() {
            return overrideAttributeDeclaration;
        }
    }

    private static class AddOverrideFix implements HintFix {

        private static final List<PHPTokenId> COMMENT_AND_WS_TOKEN_IDS = Arrays.asList(
                PHPTokenId.WHITESPACE,
                PHPTokenId.PHP_LINE_COMMENT,
                PHPTokenId.PHPDOC_COMMENT_START,
                PHPTokenId.PHPDOC_COMMENT,
                PHPTokenId.PHPDOC_COMMENT_END,
                PHPTokenId.PHP_COMMENT_START,
                PHPTokenId.PHP_COMMENT,
                PHPTokenId.PHP_COMMENT_END
        );

        private final BaseDocument document;
        private final MethodDeclaration methodDeclaration;
        private final TokenSequence<PHPTokenId> ts;

        public AddOverrideFix(BaseDocument document, MethodDeclaration methodDeclaration, TokenSequence<PHPTokenId> ts) {
            this.document = document;
            this.methodDeclaration = methodDeclaration;
            this.ts = ts;
        }

        @Override
        @Messages({
            "AddOverrideFix_description=Add \"#[\\Override]\" Attribute"
        })
        public String getDescription() {
            return Bundle.AddOverrideFix_description();
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            int offset = methodDeclaration.getStartOffset();
            List<Attribute> attributes = methodDeclaration.getAttributes();
            ts.move(offset);
            CharSequence indent = CodeUtils.EMPTY_STRING;
            if (ts.movePrevious()) {
                if (isWhitespace(ts.token())) {
                    CharSequence text = ts.token().text();
                    int lastIndex = TokenUtilities.lastIndexOf(text, CodeUtils.NEW_LINE);
                    if (lastIndex != -1) {
                        indent = text.subSequence(lastIndex + 1, text.length());
                    }
                }
            }
            if (attributes.isEmpty()) {
                edits.replace(offset, 0, OVERRIDE.asAttributeExpression() + CodeUtils.NEW_LINE + indent, false, 0);
            } else {
                // #[Attr] // comment
                // #[\Override]
                // public function example(): void {}
                Attribute lastAttribute = attributes.get(attributes.size() - 1);
                offset = lastAttribute.getEndOffset();
                ts.move(offset);
                if (ts.moveNext()) {
                    Token<? extends PHPTokenId> findNext = LexUtilities.findNext(ts, COMMENT_AND_WS_TOKEN_IDS);
                    if (findNext != null) {
                        offset = ts.offset();
                    }
                }
                edits.replace(offset, 0, OVERRIDE.asAttributeExpression() + CodeUtils.NEW_LINE + indent, false, 0);
            }
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static class RemoveOverrideFix implements HintFix {

        private final BaseDocument document;
        private final InvalidOverrideAttributeMethod invalidOverrideMethod;
        private final TokenSequence<PHPTokenId> ts;

        public RemoveOverrideFix(BaseDocument document, InvalidOverrideAttributeMethod invalidOverrideMethod, TokenSequence<PHPTokenId> ts) {
            this.document = document;
            this.invalidOverrideMethod = invalidOverrideMethod;
            this.ts = ts;
        }

        @Override
        @Messages({
            "# {0} - method name",
            "RemoveOverrideFix_description=Remove \"#[\\Override]\" Attribute from \"{0}\" method"
        })
        public String getDescription() {
            String methodName = invalidOverrideMethod.getMethodName();
            return Bundle.RemoveOverrideFix_description(methodName);
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            OffsetRange removalRange = getRemovalRange(invalidOverrideMethod);
            if (removalRange != OffsetRange.NONE) {
                edits.replace(removalRange.getStart(), removalRange.getLength(), CodeUtils.EMPTY_STRING, false, 0);
                edits.apply();
            }
        }

        private OffsetRange getRemovalRange(InvalidOverrideAttributeMethod invalidAttributedMethod) {
            Attribute attribute = invalidAttributedMethod.getAttribute();
            if (attribute.getAttributeDeclarations().size() == 1) {
                return getRemovalRange(attribute);
            }

            AttributeDeclaration overrideDeclaration = invalidAttributedMethod.getOverrideAttributeDeclaration();
            // #[\Override, Attribute1], #[Attribute1, \Override], #[Attribute1, \Override, Attribute2]
            int startOffset = overrideDeclaration.getStartOffset();
            AttributeDeclaration previousDeclaration = null;
            for (Iterator<AttributeDeclaration> iterator = attribute.getAttributeDeclarations().iterator(); iterator.hasNext();) {
                AttributeDeclaration attributeDeclaration = iterator.next();
                if (attributeDeclaration.getStartOffset() == startOffset) {
                    int endOffset;
                    if (!iterator.hasNext()) {
                        // e.g. #[Atrr, \Override]
                        endOffset = attributeDeclaration.getEndOffset();
                        ts.move(startOffset);
                        if (previousDeclaration != null) {
                            startOffset = previousDeclaration.getEndOffset();
                        }
                        int commentEnd = getPreviousCommentEnd(startOffset);
                        if (startOffset != commentEnd) {
                            // e.g. #[Atrr, /* comment */ \Override]
                            startOffset = commentEnd;
                            // check comma
                            // e.g.
                            // #[
                            //     Attr, // comment
                            //     \Override,
                            // ]
                            endOffset = getCommaEnd(endOffset, attribute);
                        }
                    } else {
                        // e.g. #[\Override, Atrr]
                        endOffset = iterator.next().getStartOffset();
                        endOffset = getNextCommentStart(overrideDeclaration, endOffset);
                    }
                    return new OffsetRange(startOffset, endOffset);
                }
                previousDeclaration = attributeDeclaration;
            }
            return OffsetRange.NONE;
        }

        private int getNextCommentStart(AttributeDeclaration overrideDeclaration, int endOffset) {
            int end = endOffset;
            ts.move(overrideDeclaration.getEndOffset());
            while (ts.moveNext() && ts.offset() < endOffset) {
                if (isComma(ts.token()) || isWhitespace(ts.token())) {
                    continue;
                }
                end = ts.offset();
                break;
            }
            return end;
        }

        private int getPreviousCommentEnd(int startOffset) {
            // check comments
            int start = startOffset;
            while (ts.movePrevious() && ts.offset() >= startOffset) {
                if (isComma(ts.token()) || isWhitespace(ts.token())) {
                    continue;
                }
                start = ts.offset() + ts.token().length();
                if (TokenUtilities.endsWith(ts.token().text(), CodeUtils.NEW_LINE)) {
                    // line comment may have a new line
                    start--;
                }
                break;
            }
            return start;
        }

        private int getCommaEnd(int endOffset, Attribute attribute) {
            int end = endOffset;
            ts.move(end);
            while (ts.moveNext() && ts.offset() < attribute.getEndOffset()) {
                if (isComma(ts.token())) {
                    end = ts.offset() + ts.token().length();
                    break;
                } else {
                    if (isWhitespace(ts.token())) {
                        continue;
                    }
                    break;
                }
            }
            return end;
        }

        private OffsetRange getRemovalRange(Attribute attribute) {
            // #[\Override]
            int endOffset = attribute.getEndOffset();
            ts.move(endOffset);
            if (ts.moveNext()) {
                if (isWhitespace(ts.token())) {
                    endOffset = ts.offset() + ts.token().text().length();
                }
            }
            return new OffsetRange(attribute.getStartOffset(), endOffset);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }
}
