/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.EnumDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

class EnumScopeImpl extends TypeScopeImpl implements EnumScope, VariableNameFactory {

    private final Collection<QualifiedName> usedTraits = new HashSet<>();
    private final QualifiedName backingType;

    EnumScopeImpl(Scope inScope, EnumDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, isDeprecated);
        for (QualifiedName usedTrait : nodeInfo.getUsedTraits()) {
            usedTraits.add(VariousUtils.getFullyQualifiedName(usedTrait, nodeInfo.getOriginalNode().getStartOffset(), inScope));
        }
        this.backingType = nodeInfo.getBackingType();
    }

    EnumScopeImpl(IndexScope inScope, EnumElement indexedEnum) {
        super(inScope, indexedEnum);
        usedTraits.addAll(indexedEnum.getUsedTraits());
        this.backingType = indexedEnum.getBackingType();
    }

    @Override
    @CheckForNull
    public QualifiedName getBackingType() {
        return backingType;
    }

    @NonNull
    private String getBackingTypeName() {
        return backingType == null ? "" : backingType.toString(); // NOI18N
    }

    @Override
    void addElement(ModelElementImpl element) {
        assert element instanceof TypeScope
                || element instanceof VariableName
                || element instanceof MethodScope
                || element instanceof FieldElement // don't have field but allow having it
                || element instanceof ClassConstantElement
                || element instanceof CaseElement : element.getPhpElementKind();
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
    public String asString(PrintAs as) {
        StringBuilder retval = new StringBuilder();
        switch (as) {
            case NameAndSuperTypes:
                retval.append(getName());
                printAsSuperTypes(retval);
                break;
            case SuperTypes:
                printAsSuperTypes(retval);
                break;
            default:
                assert false : as;
        }
        return retval.toString();
    }

    private void printAsSuperTypes(StringBuilder sb) {
        Set<QualifiedName> superIfaces = getSuperInterfaces();
        if (!superIfaces.isEmpty()) {
            sb.append(" implements "); // NOI18N
        }
        StringBuilder ifacesBuffer = new StringBuilder();
        for (QualifiedName qualifiedName : superIfaces) {
            if (ifacesBuffer.length() > 0) {
                ifacesBuffer.append(", "); // NOI18N
            }
            ifacesBuffer.append(qualifiedName.getName());
        }
        sb.append(ifacesBuffer);
    }

    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<>(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Set<MethodElement> indexedFunctions
                    = org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(iface));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                TypeElement type = indexedFunction.getType();
                if (type.isInterface()) {
                    allMethods.add(new MethodScopeImpl(new InterfaceScopeImpl(indexScope, (InterfaceElement) type), indexedFunction));
                } else {
                    allMethods.add(new MethodScopeImpl(new EnumScopeImpl(indexScope, (EnumElement) type), indexedFunction));
                }
            }
        }
        Set<TraitScope> traitScopes = new HashSet<>(getTraits());
        for (TraitScope traitScope : traitScopes) {
            // don't filter private methods because it can be used in a class
            Set<MethodElement> indexedMethods = index.getAllMethods(traitScope);
            for (MethodElement methodElement : indexedMethods) {
                TypeElement type = methodElement.getType();
                if (type.isTrait()) {
                    allMethods.add(new MethodScopeImpl(new TraitScopeImpl(indexScope, (TraitElement) type), methodElement));
                } else {
                    allMethods.add(new MethodScopeImpl(new EnumScopeImpl(indexScope, (EnumElement) type), methodElement));
                }
            }
        }
        return allMethods;
    }

    @Override
    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        Set<ClassConstantElement> allConstants = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        org.netbeans.modules.php.editor.api.elements.ElementFilter filterForPrivate = org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false);
        Set<InterfaceScope> interfaceScopes = new HashSet<>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<TypeConstantElement> indexedConstants = filterForPrivate.filter(index.getInheritedTypeConstants(iface));
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement constant = classMember;
                allConstants.add(new ClassConstantElementImpl(iface, constant));
            }
        }
        return allConstants;
    }

    @Override
    public Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_ENUM, getIndexSignature(), true, true);
        final NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        Set<QualifiedName> superInterfaces = getSuperInterfaces();
        for (QualifiedName superIfaceName : superInterfaces) {
            final String name = superIfaceName.getName();
            final String namespaceName = VariousUtils.getFullyQualifiedName(
                    superIfaceName,
                    getOffset(),
                    namespaceScope).getNamespaceName();
            indexDocument.addPair(PHPIndexer.FIELD_SUPER_IFACE, String.format("%s;%s;%s", name.toLowerCase(), name, namespaceName), true, true); //NOI18N
        }
        for (QualifiedName qualifiedName : getUsedTraits()) {
            final String name = qualifiedName.getName();
            final String namespaceName = VariousUtils.getFullyQualifiedName(
                    qualifiedName,
                    getOffset(),
                    namespaceScope).getNamespaceName();
            indexDocument.addPair(PHPIndexer.FIELD_USED_TRAIT, String.format("%s;%s;%s", name.toLowerCase(), name, namespaceName), true, true); //NOI18N
        }
        indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);

        for (MethodScope methodScope : getDeclaredMethods()) {
            if (methodScope instanceof LazyBuild) {
                LazyBuild lazyMethod = (LazyBuild) methodScope;
                if (!lazyMethod.isScanned()) {
                    lazyMethod.scan();
                }
            }
            if (!StringUtils.isEmpty(methodScope.getName())) { // #257898
                methodScope.addSelfToIndex(indexDocument);
            }
        }
        for (ClassConstantElement constantElement : getDeclaredConstants()) {
            constantElement.addSelfToIndex(indexDocument);
        }
        for (CaseElement enumCase : getDeclaredEnumCases()) {
            enumCase.addSelfToIndex(indexDocument);
        }
    }

    @Override
    public final Collection<? extends CaseElement> getDeclaredEnumCases() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            return indexScopeImpl.findEnumCases(this);
        }
        return filter(getElements(), (ModelElement element) -> element.getPhpElementKind() == PhpElementKind.ENUM_CASE);
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER); // 0: lower case name
        sb.append(getName()).append(Signature.ITEM_DELIMITER); // 1: name
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER); // 2: offset
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope != null ? namespaceScope.getQualifiedName() : QualifiedName.create("");
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER); // 3: namespace name
        sb.append(getBackingTypeName()).append(Signature.ITEM_DELIMITER); // 4: backing type
        List<? extends String> superInterfaceNames = getSuperInterfaceNames();
        StringBuilder ifaceSb = new StringBuilder();
        for (String iface : superInterfaceNames) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(","); //NOI18N
            }
            ifaceSb.append(iface);
        }
        sb.append(ifaceSb);
        if (ifaceSb.length() > 0) {
            sb.append(Type.SEPARATOR);
            StringBuilder fqIfaceSb = new StringBuilder();
            Collection<QualifiedName> fQSuperInterfaceNames = getFQSuperInterfaceNames();
            for (QualifiedName fQSuperInterfaceName : fQSuperInterfaceNames) {
                if (fqIfaceSb.length() > 0) {
                    fqIfaceSb.append(","); // NOI18N
                }
                fqIfaceSb.append(fQSuperInterfaceName.toString());
            }
            sb.append(fqIfaceSb);
        }
        sb.append(Signature.ITEM_DELIMITER); // 5: interfaces
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER); // 6: modifiers
        if (!usedTraits.isEmpty()) {
            StringBuilder traitSb = new StringBuilder();
            for (QualifiedName usedTrait : usedTraits) {
                if (traitSb.length() > 0) {
                    traitSb.append(","); // NOI18N
                }
                traitSb.append(usedTrait.toString());
            }
            sb.append(traitSb); // 7: used traits
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER); // 8: deprecated
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER); // 9: file name url
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof EnumElement) {
            EnumElement indexedEnum = (EnumElement) indexedElement;
            return indexedEnum.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        return filter(getElements(), (ModelElement element) -> {
            if (element instanceof MethodScope
                    && ((MethodScope) element).isInitiator()
                    && element instanceof LazyBuild) {
                LazyBuild scope = (LazyBuild) element;
                if (!scope.isScanned()) {
                    scope.scan();
                }
            }
            return element.getPhpElementKind() == PhpElementKind.VARIABLE;
        });
    }

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
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
        // enum is final
        return false;
    }

    @Override
    public boolean isSubTypeOf(final TypeScope superType) {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaceScopes();
        if (!implementedInterfaces.isEmpty()) {
            sb.append(" implements "); // NOI18N
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" "); // NOI18N
            }
        }
        Collection<? extends TraitScope> traits = getTraits();
        if (!traits.isEmpty()) {
            sb.append(" uses "); // NOI18N
            for (TraitScope traitScope : traits) {
                sb.append(traitScope.getName()).append(" "); // NOI18N
            }
        }
        return sb.toString();
    }

}
