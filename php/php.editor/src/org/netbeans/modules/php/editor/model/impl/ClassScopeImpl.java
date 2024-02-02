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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
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
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassInstanceCreationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class ClassScopeImpl extends TypeScopeImpl implements ClassScope, VariableNameFactory {
    private final Collection<QualifiedName> possibleFQSuperClassNames;
    // @GuardedBy("this")
    private final Collection<QualifiedName> mixinClassNames = new LinkedHashSet<>();
    private final Collection<QualifiedName> usedTraits = new HashSet<>();
    private final Set<? super TypeScope> superRecursionDetection = new HashSet<>();
    private final Set<? super TypeScope> subRecursionDetection = new HashSet<>();
    private final Union2<String, List<ClassScopeImpl>> superClass;
    private final boolean isAttributeClass;

    @Override
    void addElement(ModelElementImpl element) {
        assert element instanceof TypeScope || element instanceof VariableName
                || element instanceof MethodScope || element instanceof FieldElement
                || element instanceof CaseElement // allowed by parser although class can't have cases
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

    //new contructors
    ClassScopeImpl(Scope inScope, ClassDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, isDeprecated);
        Expression superId = nodeInfo.getSuperClass();
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(inScope);
        isAttributeClass = isAttributeClass(nodeInfo, namespaceScope);
        if (superId != null) {
            QualifiedName superClassName = QualifiedName.create(superId);
            if (namespaceScope == null) {
                this.possibleFQSuperClassNames = Collections.emptyList();
            } else {
                this.possibleFQSuperClassNames = VariousUtils.getPossibleFQN(superClassName, nodeInfo.getSuperClass().getStartOffset(), namespaceScope);
            }
            if (superClassName != null) {
                this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superClassName.toString());
            } else {
                this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(null);
            }
        } else {
            this.possibleFQSuperClassNames = Collections.emptyList();
            this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(null);
        }
        for (QualifiedName usedTrait : nodeInfo.getUsedTraits()) {
            if (!usedTrait.getName().isEmpty()) {
                // GH-6634
                // avoid getting traits from the index with an empty string
                usedTraits.add(VariousUtils.getFullyQualifiedName(usedTrait, nodeInfo.getOriginalNode().getStartOffset(), inScope));
            }
        }
    }

    ClassScopeImpl(Scope inScope, ClassInstanceCreationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, isDeprecated);
        Expression superId = nodeInfo.getSuperClass();
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(inScope);
        isAttributeClass = false; // ignore anonymous classes
        if (superId != null) {
            QualifiedName superClassName = QualifiedName.create(superId);
            if (namespaceScope == null) {
                this.possibleFQSuperClassNames = Collections.emptyList();
            } else {
                this.possibleFQSuperClassNames = VariousUtils.getPossibleFQN(superClassName, nodeInfo.getSuperClass().getStartOffset(), namespaceScope);
            }
            if (superClassName != null) {
                this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superClassName.toString());
            } else {
                this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(null);
            }
        } else {
            this.possibleFQSuperClassNames = Collections.emptyList();
            this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(null);
        }
        for (QualifiedName usedTrait : nodeInfo.getUsedTraits()) {
            QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(usedTrait, nodeInfo.getOriginalNode().getStartOffset(), inScope);
            if (!fullyQualifiedName.getName().isEmpty()) {
                // GH-6634
                // avoid getting traits from the index with an empty string
                usedTraits.add(fullyQualifiedName);
            }
        }
    }

    ClassScopeImpl(IndexScope inScope, ClassElement indexedClass) {
        super(inScope, indexedClass);
        final QualifiedName superClassName = indexedClass.getSuperClassName();
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superClassName != null ? superClassName.toString() : null);
        this.possibleFQSuperClassNames = indexedClass.getPossibleFQSuperClassNames();
        usedTraits.addAll(indexedClass.getUsedTraits());
        this.mixinClassNames.addAll(indexedClass.getFQMixinClassNames());
        isAttributeClass = indexedClass.isAttribute();
    }
    //old contructors

    private boolean isAttributeClass(ClassDeclarationInfo nodeInfo, NamespaceScope namespaceScope) {
        String className = nodeInfo.getName();
        int offset = nodeInfo.getOriginalNode().getStartOffset();
        if (isPredefinedAttribute(className, offset, namespaceScope)) {
            return true;
        }
        for (Attribute attribute : nodeInfo.getAttributes()) {
            for (AttributeDeclaration attributeDeclaration : attribute.getAttributeDeclarations()) {
                Expression attributeNameExpression = attributeDeclaration.getAttributeName();
                String attributeName = CodeUtils.extractQualifiedName(attributeNameExpression);
                if (isAttributeAttribute(attributeName, attributeNameExpression.getStartOffset(), namespaceScope)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAttributeAttribute(String attributeName, int offset, NamespaceScope namespaceScope) {
        if (PredefinedSymbols.Attributes.ATTRIBUTE.getFqName().equals(attributeName)) {
            return true;
        }
        return PredefinedSymbols.Attributes.ATTRIBUTE.getName().equals(attributeName)
                && isPredefinedAttribute(attributeName, offset, namespaceScope);
    }

    private boolean isPredefinedAttribute(String attributeName, int offset, NamespaceScope namespaceScope) {
        if (PredefinedSymbols.ATTRIBUTE_FQ_NAMES.contains(attributeName)) {
            return true;
        }
        if (PredefinedSymbols.ATTRIBUTE_NAMES.contains(attributeName)) {
            if (namespaceScope != null) {
                // check FQ name because there may be use statement (e.g. `use Attribute;`)
                QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(attributeName), offset, namespaceScope);
                if (PredefinedSymbols.ATTRIBUTE_FQ_NAMES.contains(fullyQualifiedName.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method returns possible FGNames of the super class that are counted
     * according the same algorithm as in php runtime. Usually it can be one or two
     * FQN.
     * @return possible fully qualified names, that are guess during parsing.
     */
    @Override
    public Collection<QualifiedName> getPossibleFQSuperClassNames() {
        return Collections.unmodifiableCollection(this.possibleFQSuperClassNames);
    }

    /**
     * Add fully qualified names for @mixin tag.
     *
     * @param names fully qualified names
     */
    synchronized void addFQMixinClassNames(Collection<QualifiedName> names) {
        mixinClassNames.addAll(names);
    }

    @Override
    public synchronized Collection<QualifiedName> getFQMixinClassNames() {
        return Collections.unmodifiableCollection(mixinClassNames);
    }

    @NonNull
    @Override
    public Collection<? extends ClassScope> getSuperClasses() {
        List<ClassScope> retval = null;
        if (superClass.hasSecond() && superClass.second() != null) {
            return superClass.second();
        }

        assert superClass.hasFirst();
        String superClasName = superClass.first();
        if (possibleFQSuperClassNames != null && !possibleFQSuperClassNames.isEmpty()) {
            retval = new ArrayList<>();
            for (QualifiedName qualifiedName : possibleFQSuperClassNames) {
                retval.addAll(IndexScopeImpl.getClasses(qualifiedName, this));
            }
        }

        if (retval == null && superClasName != null) {
            return IndexScopeImpl.getClasses(QualifiedName.create(superClasName), this);
        }
        return retval != null ? retval : Collections.<ClassScopeImpl>emptyList();
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
        QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) {
            sb.append(" extends  "); //NOI18N
            sb.append(superClassName.getName());
        }
        Set<QualifiedName> superIfaces = getSuperInterfaces();
        if (!superIfaces.isEmpty()) {
            sb.append(" implements "); //NOI18N
        }
        StringBuilder ifacesBuffer = new StringBuilder();
        for (QualifiedName qualifiedName : superIfaces) {
            if (ifacesBuffer.length() > 0) {
                ifacesBuffer.append(", "); //NOI18N
            }
            ifacesBuffer.append(qualifiedName.getName());
        }
        sb.append(ifacesBuffer);
    }

    /**
     * This method returns declared fields. <b>Note:</b>Contains annotation
     * fileds. e.g. @property int $a, @param string $b.
     *
     * @return declared fields
     */
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
    public Collection<? extends FieldElement> getFields() {
        Set<FieldElement> allFields = new HashSet<>();
        allFields.addAll(getDeclaredFields());
        allFields.addAll(getInheritedFields());
        return allFields;
    }

    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<ClassScope> superClasses = new HashSet<>(getSuperClasses());
        for (ClassScope clz : superClasses) {
            Set<MethodElement> indexedFunctions =
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(clz));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                TypeElement type = indexedFunction.getType();
                if (type.isInterface()) {
                    allMethods.add(new MethodScopeImpl(new InterfaceScopeImpl(indexScope, (InterfaceElement) type), indexedFunction));
                } else if (type.isTrait()) {
                    allMethods.add(new MethodScopeImpl(new TraitScopeImpl(indexScope, (TraitElement) type), indexedFunction));
                } else {
                    allMethods.add(new MethodScopeImpl(new ClassScopeImpl(indexScope, (ClassElement) type), indexedFunction));
                }
            }
        }
        Set<InterfaceScope> interfaceScopes = new HashSet<>(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Set<MethodElement> indexedFunctions =
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(iface));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                TypeElement type = indexedFunction.getType();
                if (type.isInterface()) {
                    allMethods.add(new MethodScopeImpl(new InterfaceScopeImpl(indexScope, (InterfaceElement) type), indexedFunction));
                } else {
                    allMethods.add(new MethodScopeImpl(new ClassScopeImpl(indexScope, (ClassElement) type), indexedFunction));
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
                    allMethods.add(new MethodScopeImpl(new ClassScopeImpl(indexScope, (ClassElement) type), methodElement));
                }
            }
        }
        return allMethods;
    }

    @Override
    public Collection<? extends FieldElement> getInheritedFields() {
        Set<FieldElement> allFields = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        org.netbeans.modules.php.editor.api.elements.ElementFilter filterForPrivate = org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false);
        Set<ClassScope> superClasses = new HashSet<>(getSuperClasses());
        for (ClassScope classScope : superClasses) {
            Set<org.netbeans.modules.php.editor.api.elements.FieldElement> indexedFields = filterForPrivate.filter(index.getAlllFields(classScope));
            for (org.netbeans.modules.php.editor.api.elements.FieldElement field : indexedFields) {
                allFields.add(new FieldElementImpl(classScope, field));
            }
        }
        for (TraitScope traitScope : new HashSet<>(getTraits())) {
            Set<org.netbeans.modules.php.editor.api.elements.FieldElement> indexedFields = index.getAlllFields(traitScope);
            for (org.netbeans.modules.php.editor.api.elements.FieldElement field : indexedFields) {
                allFields.add(new FieldElementImpl(traitScope, field));
            }
        }
        // GH-4683 get fields of mixin
        Set<TypeMemberElement> mixinTypeMembers = index.getAccessibleMixinTypeMembers(this, this);
        for (TypeMemberElement mixinTypeMember : mixinTypeMembers) {
            if (mixinTypeMember instanceof org.netbeans.modules.php.editor.api.elements.FieldElement) {
                allFields.add((new FieldElementImpl(this, (org.netbeans.modules.php.editor.api.elements.FieldElement) mixinTypeMember)));
            }
        }
        return allFields;
    }

    @Override
    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        // show items in Navigator Window
        Set<ClassConstantElement> allConstants = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        org.netbeans.modules.php.editor.api.elements.ElementFilter filterForPrivate = org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false);
        Set<ClassScope> superClasses = new HashSet<>(getSuperClasses());
        for (ClassScope classScope : superClasses) {
            Set<TypeConstantElement> indexedConstants = filterForPrivate.filter(index.getAllTypeConstants(classScope));
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement constant = classMember;
                TypeElement type = constant.getType();
                Scope inScope = classScope;
                if (type instanceof TraitElement) {
                    inScope = new TraitScopeImpl(indexScope, (TraitElement) type);
                }
                allConstants.add(new ClassConstantElementImpl(inScope, constant));
            }
        }
        Set<InterfaceScope> interfaceScopes = new HashSet<>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<TypeConstantElement> indexedConstants = filterForPrivate.filter(index.getInheritedTypeConstants(iface));
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement constant = classMember;
                allConstants.add(new ClassConstantElementImpl(iface, constant));
            }
        }

        // [GH-4725] PHP 8.2 Support: Constants in Traits
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
    public Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public String getNormalizedName() {
        return super.getNormalizedName() + (getSuperClassName() != null ? getSuperClassName() : ""); //NOI18N
    }



    @CheckForNull
    @Override
    public QualifiedName getSuperClassName() {
        if (superClass != null) {
            List<? extends ClassScope> retval = superClass.hasSecond() ? superClass.second() : null; //this
            if (retval == null) {
                assert superClass.hasFirst();
                String superClasName = superClass.first();
                if (superClasName != null) {
                    return QualifiedName.create(superClasName);

                }
            } else if (!retval.isEmpty()) {
                ClassScope cls = ModelUtils.getFirst(retval);
                if (cls != null) {
                    return QualifiedName.create(cls.getName());
                }
            }
        }
        return null;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_CLASS, getIndexSignature(), true, true);
        if (isAttributeClass) {
            indexDocument.addPair(PHPIndexer.FIELD_ATTRIBUTE_CLASS, getIndexSignature(), true, true);
        }
        final NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) {
            final String name = superClassName.getName();
            final String namespaceName = VariousUtils.getFullyQualifiedName(
                    superClassName,
                    getOffset(),
                    namespaceScope).getNamespaceName();
            indexDocument.addPair(PHPIndexer.FIELD_SUPER_CLASS, String.format("%s;%s;%s", name.toLowerCase(), name, namespaceName), true, true); //NOI18N
        }
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
        for (FieldElement fieldElement : getDeclaredFields()) {
            fieldElement.addSelfToIndex(indexDocument);
        }
        for (ClassConstantElement constantElement : getDeclaredConstants()) {
            constantElement.addSelfToIndex(indexDocument);
        }
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase(Locale.ROOT)).append(Signature.ITEM_DELIMITER); // 0: lowercase class name
        sb.append(getName()).append(Signature.ITEM_DELIMITER); // 1. class name
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER); // 2. offset
        final QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) { // 3. super class name
            sb.append(superClassName.toString());
            sb.append(Type.SEPARATOR);
            boolean first = true;
            for (QualifiedName qualifiedName : possibleFQSuperClassNames) {
                if (!first) {
                    sb.append(',');
                } else {
                    first = true;
                }
                assert !qualifiedName.getName().isEmpty();
                sb.append(qualifiedName.toString());
            }
        }
        sb.append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope != null ? namespaceScope.getQualifiedName() : QualifiedName.create("");
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER); // 4. qualified name
        List<? extends String> superInterfaceNames = getSuperInterfaceNames(); // 5. fully qualified super class names
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
                    fqIfaceSb.append(","); //NOI18N
                }
                fqIfaceSb.append(fQSuperInterfaceName.toString());
            }
            sb.append(fqIfaceSb);
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER); // 6. flags
        if (!usedTraits.isEmpty()) { // 7. used traits
            StringBuilder traitSb = new StringBuilder();
            for (QualifiedName usedTrait : usedTraits) {
                if (traitSb.length() > 0) {
                    traitSb.append(","); //NOI18N
                }
                assert !usedTrait.getName().isEmpty();
                traitSb.append(usedTrait.toString());
            }
            sb.append(traitSb);
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER); // 8. isDeprecated
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER); // 9. filename url
        // mixin
        StringBuilder mixinSb = new StringBuilder();
        for (QualifiedName mixinClassName : mixinClassNames) { // 10. mixin class names
            if (mixinSb.length() > 0) {
                mixinSb.append(","); // NOI18N
            }
            assert !mixinClassName.getName().isEmpty();
            mixinSb.append(mixinClassName.toString());
        }
        sb.append(mixinSb);
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(isAttributeClass ? 1 : 0); // 11. isAttributeClass
        sb.append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    @Override
    public Collection<? extends MethodScope> getDeclaredConstructors() {
        return ModelUtils.filter(getDeclaredMethods(), new ModelUtils.ElementFilter<MethodScope>() {
            @Override
            public boolean isAccepted(MethodScope methodScope) {
                return methodScope.isConstructor();
            }
        });
    }

    @Override
    public String getDefaultConstructorIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(BodyDeclaration.Modifier.PUBLIC).append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        assert namespaceScope != null;
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        sb.append(Signature.ITEM_DELIMITER); // mixin

        return sb.toString();

    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof ClassElement) {
            ClassElement indexedClass = (ClassElement) indexedElement;
            return indexedClass.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    @Override
    public Collection<? extends String> getSuperClassNames() {
        List<String> retval = new ArrayList<>();
        if (superClass != null) {
            String supeClsName = superClass.hasFirst() ? superClass.first() : null;
            if (supeClsName != null) {
                return Collections.singletonList(supeClsName);
            }
            List<ClassScopeImpl> supeClasses = Collections.emptyList();
            if (superClass.hasSecond()) {
                supeClasses = superClass.second();
            }
            for (ClassScopeImpl cls : supeClasses) {
                retval.add(cls.getName());
            }
        }
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

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }

    @Override
    public boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }

    @Override
    public boolean isReadonly() {
        return getPhpModifiers().isReadonly();
    }

    @Override
    public boolean isAnonymous() {
        return CodeUtils.isSyntheticTypeName(getName());
    }

    @Override
    public boolean isAttribute() {
        return isAttributeClass;
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
            if (subType.isClass()) {
                assert (subType instanceof ClassScope);
                for (ClassScope classScope : ((ClassScope) subType).getSuperClasses()) {
                    if (classScope.equals(this)) {
                        result = true;
                    } else {
                        result = isSuperTypeOf(classScope);
                    }
                    if (result) {
                        break;
                    }
                }
            } else if (subType.isTrait()) {
                result = false;
            } else {
                result = super.isSuperTypeOf(subType);
            }
        }
        return result;
    }

    @Override
    public boolean isSubTypeOf(final TypeScope superType) {
        boolean result = false;
        if (subRecursionDetection.add(superType)) {
            if (superType.isClass()) {
                for (ClassScope classScope : getSuperClasses()) {
                    if (classScope.equals(superType)) {
                        result = true;
                    } else {
                        result = classScope.isSubTypeOf(superType);
                    }
                    if (result) {
                        break;
                    }
                }
            } else if (superType.isTrait()) {
                for (ClassScope classScope : getSuperClasses()) {
                    result = classScope.isSubTypeOf(superType);
                    if (result) {
                        break;
                    }
                }
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
            } else {
                result = super.isSubTypeOf(superType);
                if (!result) {
                    for (ClassScope classScope : getSuperClasses()) {
                        result = classScope.isSubTypeOf(superType);
                        if (result) {
                            break;
                        }
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
        Collection<? extends ClassScope> extendedClasses = getSuperClasses();
        ClassScope extClass = ModelUtils.getFirst(extendedClasses);
        if (extClass != null) {
            sb.append(" extends ").append(extClass.getName()); //NOI18N
        }
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaceScopes();
        if (!implementedInterfaces.isEmpty()) {
            sb.append(" implements "); //NOI18N
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        Collection<? extends TraitScope> traits = getTraits();
        if (!traits.isEmpty()) {
            sb.append(" uses "); //NOI18N
            for (TraitScope traitScope : traits) {
                sb.append(traitScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

}
