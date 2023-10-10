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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TraitedScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.TraitDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class TraitScopeImpl extends TypeScopeImpl implements TraitScope, VariableNameFactory {
    private final Collection<QualifiedName> usedTraits;
    private final Set<? super TypeScope> superRecursionDetection = new HashSet<>();
    private final Set<? super TypeScope> subRecursionDetection = new HashSet<>();

    TraitScopeImpl(Scope inScope, TraitElement indexedTrait) {
        super(inScope, indexedTrait);
        usedTraits = indexedTrait.getUsedTraits();
    }

    TraitScopeImpl(Scope inScope, TraitDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, isDeprecated);
        usedTraits = nodeInfo.getUsedTraits();
    }

    @Override
    void addElement(ModelElementImpl element) {
        assert element instanceof TypeScope || element instanceof VariableName
                || element instanceof MethodScope || element instanceof FieldElement
                || element instanceof CaseElement // allowed by parser although trait can't have cases
                || element instanceof ClassConstantElement : element.getPhpElementKind();
        if (element instanceof TypeScope) {
            Scope inScope = getInScope();
            if (inScope instanceof ScopeImpl) {
                ((ScopeImpl) inScope).addElement(element);
            }
        } else {
            super.addElement(element);
        }
    }

    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<TraitScope> traitScopes = new HashSet<>(getTraits());
        for (TraitScope traitScope : traitScopes) {
            Set<MethodElement> indexedMethods = index.getAllMethods(traitScope);
            for (MethodElement methodElement : indexedMethods) {
                TypeElement type = methodElement.getType();
                if (type.isTrait()) {
                    allMethods.add(new MethodScopeImpl(new TraitScopeImpl(indexScope, (TraitElement) type), methodElement));
                }
            }
        }
        return allMethods;
    }

    @Override
    public Collection<? extends MethodScope> getMethods() {
        return getDeclaredMethods();
    }

    @Override
    public Collection<? extends ClassConstantElement> getInheritedConstants() {
        // show items in Navigator Window
        // [GH-4725] PHP 8.2 Support: Constatns in Traits
        Set<ClassConstantElement> allConstants = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<TraitScope> traits = new HashSet<>(getTraits());
        for (TraitScope trait : traits) {
            // do not filter private constants (private constants are available)
            Set<TypeConstantElement> indexedConstants = index.getAllTypeConstants(trait);
            for (TypeConstantElement constant : indexedConstants) {
                allConstants.add(new ClassConstantElementImpl(trait, constant));
            }
        }
        return allConstants;
    }

    @Override
    public String asString(PrintAs as) {
        return getName();
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_TRAIT, getIndexSignature(), true, true);
        indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
        for (QualifiedName qualifiedName : getUsedTraits()) {
            final String name = qualifiedName.getName();
            final String namespaceName = VariousUtils.getFullyQualifiedName(
                    qualifiedName,
                    getOffset(),
                    (NamespaceScope) getInScope()).getNamespaceName();
            indexDocument.addPair(PHPIndexer.FIELD_USED_TRAIT, String.format("%s;%s;%s", name.toLowerCase(), name, namespaceName), true, true); //NOI18N
        }
        for (MethodScope methodScope : getDeclaredMethods()) {
            if (methodScope instanceof LazyBuild) {
                LazyBuild lazyMethod = (LazyBuild) methodScope;
                if (!lazyMethod.isScanned()) {
                    lazyMethod.scan();
                }
            }
            methodScope.addSelfToIndex(indexDocument);
        }
        for (FieldElement fieldElement : getDeclaredFields()) {
            fieldElement.addSelfToIndex(indexDocument);
        }
        // [GH-4725] PHP 8.2 Support: Constants in Traits
        for (ClassConstantElement constantElement : getDeclaredConstants()) {
            constantElement.addSelfToIndex(indexDocument);
        }
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        // if inScope is IndexScope, namespaceScope is null
        QualifiedName qualifiedName = namespaceScope != null ? namespaceScope.getQualifiedName() : QualifiedName.create(""); // NOI18N
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        if (!usedTraits.isEmpty()) {
            StringBuilder traitSb = new StringBuilder();
            for (QualifiedName usedTrait : usedTraits) {
                if (traitSb.length() > 0) {
                    traitSb.append(","); //NOI18N
                }
                traitSb.append(usedTrait.toString());
            }
            sb.append(traitSb);
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof TraitElement) {
            TraitElement traitClass = (TraitElement) indexedElement;
            return traitClass.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    @Override
    public Collection<? extends FieldElement> getDeclaredFields() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl =  ModelUtils.getIndexScope(this);
            return indexScopeImpl.findFields(this);
        }
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.FIELD);
            }
        });
    }

    @Override
    public Collection<QualifiedName> getUsedTraits() {
        return Collections.unmodifiableCollection(usedTraits);
    }

    @Override
    public Collection<? extends TraitScope> getTraits() {
        Collection<TraitScope> result = new ArrayList<>();
        for (QualifiedName qualifiedName : getUsedTraits()) {
            result.addAll(IndexScopeImpl.getTraits(qualifiedName, this));
        }
        return result;
    }

    @Override
    public boolean isSuperTypeOf(final TypeScope subType) {
        boolean result = false;
        if (superRecursionDetection.add(subType)) {
            if (subType.isTraited()) {
                assert (subType instanceof TraitedScope);
                for (TraitScope traitScope : ((TraitedScope) subType).getTraits()) {
                    if (traitScope.equals(this)) {
                        result = true;
                    } else {
                        result = isSuperTypeOf(traitScope);
                    }
                    if (result) {
                        break;
                    }
                }
                if (!result && subType.isClass()) {
                    result = subType.isSubTypeOf(this);
                }
            }
        }
        return result;
    }

    @Override
    public boolean isSubTypeOf(final TypeScope superType) {
        boolean result = false;
        if (subRecursionDetection.add(superType)) {
            if (superType.isTrait()) {
                for (TraitScope traitScope : getTraits()) {
                    if (traitScope.equals(superType)) {
                        result = true;
                    } else {
                        result = traitScope.isSubTypeOf(superType);
                    }
                    if (result) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        Collection<? extends TraitScope> traits = getTraits();
        if (!traits.isEmpty()) {
            sb.append(" uses "); //NOI18N
            for (TraitScope traitScope : traits) {
                sb.append(traitScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                if (element instanceof MethodScope && ((MethodScope) element).isInitiator()
                        && element instanceof LazyBuild) {
                    LazyBuild scope = (LazyBuild) element;
                    if (!scope.isScanned()) {
                        scope.scan();
                    }
                }
                return element.getPhpElementKind().equals(PhpElementKind.VARIABLE);
            }
        });
    }

}
