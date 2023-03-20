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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import static org.netbeans.modules.php.editor.api.elements.ElementFilter.forFiles;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.elements.IndexQueryImpl;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class IndexScopeImpl extends ScopeImpl implements IndexScope {

    private final ElementQuery.Index index;
    private final Model model;

    IndexScopeImpl(PHPParseResult info) {
        this(info, "index", PhpElementKind.INDEX); //NOI18N
    }

    private IndexScopeImpl(PHPParseResult info, String name, PhpElementKind kind) {
        super(
                null,
                name,
                Union2.<String, FileObject>createSecond(info != null ? info.getSnapshot().getSource().getFileObject() : null),
                new OffsetRange(0, 0),
                kind,
                false);
        assert info != null;
        this.model = info.getModel();
        this.index = IndexQueryImpl.create(QuerySupportFactory.get(info), this.model);
    }

    static Collection<? extends MethodScope> getMethods(TypeScope clsScope, String methodName, ModelElement elem, final int... modifiers) {
        Set<MethodScope> retval = new HashSet<>();
        retval.addAll(ModelUtils.filter(clsScope.getDeclaredMethods(), methodName));
        retval.addAll(ModelUtils.filter(clsScope.getInheritedMethods(), methodName));
        return retval;
    }

    static Collection<? extends FieldElement> getFields(TypeScope typeScope, String fieldName, ModelElement elem, final int... modifiers) {
        Set<FieldElement> retval = new HashSet<>();
        if (typeScope instanceof ClassScope) {
            ClassScope clsScope = (ClassScope) typeScope;
            retval.addAll(ModelUtils.filter(clsScope.getDeclaredFields(), fieldName));
            retval.addAll(ModelUtils.filter(clsScope.getInheritedFields(), fieldName));
        } else {
            //implemented just for ifaces having fields which isn't normally possible in php
            //but used in ZF framework - this code could be used probably also for classes having
            //fields (implemented in the block above) if properly tested
            IndexScope indexScope = ModelUtils.getIndexScope(typeScope);
            Index index = indexScope.getIndex();
            org.netbeans.modules.php.editor.api.elements.ElementFilter forName =
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forName(NameKind.exact(fieldName));
            for (org.netbeans.modules.php.editor.api.elements.FieldElement fieldElement : forName.filter(index.getAlllFields(typeScope))) {
                retval.add(new FieldElementImpl(typeScope, fieldElement));
            }
        }
        return retval;
    }

    static Collection<? extends TypeScope> getTypes(final QualifiedName typeName, final ModelElement elem) {
        final IndexScope indexScope = ModelUtils.getIndexScope(elem);
        return indexScope.findTypes(typeName);
    }

    static List<? extends ClassScope> getClasses(final QualifiedName  className, ModelElement elem) {
        final IndexScope indexScope = ModelUtils.getIndexScope(elem);
        return indexScope.findClasses(className);
    }

    static List<? extends EnumScope> getEnums(final QualifiedName  enumName, ModelElement elem) {
        final IndexScope indexScope = ModelUtils.getIndexScope(elem);
        return indexScope.findEnums(enumName);
    }

    static List<? extends InterfaceScope> getInterfaces(final QualifiedName  ifaceName, ModelElement elem) {
        final IndexScope indexScope = ModelUtils.getIndexScope(elem);
        return indexScope.findInterfaces(ifaceName);
    }

    static List<? extends TraitScope> getTraits(final QualifiedName  traitName, ModelElement elem) {
        final IndexScope indexScope = ModelUtils.getIndexScope(elem);
        return indexScope.findTraits(traitName);
    }

    static List<? extends ConstantElement> getConstants(final QualifiedName constantName, Scope scope) {
        final IndexScope indexScope = ModelUtils.getIndexScope(scope);
        return indexScope.findConstants(constantName);
    }

    static List<? extends CaseElement> getEnumCases(final QualifiedName enumCaseName, TypeScope scope) {
        final IndexScope indexScope = ModelUtils.getIndexScope(scope);
        return indexScope.findEnumCases(scope, enumCaseName.toString());
    }

    static Collection<? extends FunctionScope> getFunctions(final QualifiedName fncName, ModelElement elem) {
        final IndexScope indexScope = ModelUtils.getIndexScope(elem);
        return indexScope.findFunctions(fncName);
    }

    @Override
    void addElement(ModelElementImpl element) {
    }
    /**
     * @return the index
     */
    @Override
    public ElementQuery.Index getIndex() {
        return index;
    }

    @Override
    public List<? extends InterfaceScope> findInterfaces(final QualifiedName queryName) {
        List<InterfaceScope> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredInterfaces(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            Set<InterfaceElement> interfaces = getIndex().getInterfaces(NameKind.exact(queryName));
            for (InterfaceElement indexedInterface : forFiles(getFileObject()).prefer(interfaces)) {
                retval.add(new InterfaceScopeImpl(this, indexedInterface));
            }
        }
        return retval;
    }

    @Override
    public List<? extends TraitScope> findTraits(final QualifiedName queryName) {
        List<TraitScope> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredTraits(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            Set<TraitElement> traits = getIndex().getTraits(NameKind.exact(queryName));
            for (TraitElement indexedTrait : forFiles(getFileObject()).prefer(traits)) {
                retval.add(new TraitScopeImpl(this, indexedTrait));
            }
        }
        return retval;
    }

    @Override
    public List<? extends ClassScope> findClasses(final QualifiedName queryName) {
        List<ClassScope> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredClasses(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            Set<ClassElement> classes = getIndex().getClasses(NameKind.exact(queryName));
            for (ClassElement indexedClass : forFiles(getFileObject()).prefer(classes)) {
                retval.add(new ClassScopeImpl(this, indexedClass));
            }
        }
        return retval;
    }

    @Override
    public List<? extends EnumScope> findEnums(QualifiedName enumName) {
        List<EnumScope> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredEnums(model.getFileScope()), enumName));
        if (retval.isEmpty()) {
            Set<EnumElement> enums = getIndex().getEnums(NameKind.exact(enumName));
            for (EnumElement indexedEnum : forFiles(getFileObject()).prefer(enums)) {
                retval.add(new EnumScopeImpl(this, indexedEnum));
            }
        }
        return retval;
    }

    @Override
    public List<? extends TypeScope> findTypes(final QualifiedName queryName) {
        List<TypeScope> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredTypes(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            final Exact exact = NameKind.exact(queryName);
            Set<TypeElement> types = getIndex().getTypes(exact);
            for (TypeElement typeElement : forFiles(getFileObject()).prefer(types)) {
                    if (typeElement instanceof ClassElement) {
                        retval.add(new ClassScopeImpl(this, (ClassElement) typeElement));
                    } else if (typeElement instanceof InterfaceElement) {
                        retval.add(new InterfaceScopeImpl(this, (InterfaceElement) typeElement));
                    } else if (typeElement instanceof TraitElement) {
                        retval.add(new TraitScopeImpl(this, (TraitElement) typeElement));
                    } else if (typeElement instanceof EnumElement) {
                        retval.add(new EnumScopeImpl(this, (EnumElement) typeElement));
                    } else {
                        assert false : typeElement.getClass();
                    }
            }
        }
        return retval;
    }

    @Override
    public List<? extends FunctionScope> findFunctions(final QualifiedName queryName) {
        List<FunctionScope> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredFunctions(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            Set<FunctionElement> functions = getIndex().getFunctions(NameKind.exact(queryName));
            for (FunctionElement indexedFunction : forFiles(getFileObject()).prefer(functions)) {
                retval.add(new FunctionScopeImpl(this, indexedFunction));
            }
        }
        return retval;
    }

    @Override
    public List<? extends ConstantElement> findConstants(final QualifiedName queryName) {
        List<ConstantElement> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredConstants(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> constants =
                    getIndex().getConstants(NameKind.exact(queryName));
            for (org.netbeans.modules.php.editor.api.elements.ConstantElement constant : forFiles(getFileObject()).prefer(constants)) {
                retval.add(new ConstantElementImpl(this, constant));
            }
        }
        return retval;
    }

    @Override
    public List<? extends VariableName> findVariables(String queryName) {
        List<VariableName> retval = new ArrayList<>();
        retval.addAll(ModelUtils.filter(ModelUtils.getDeclaredVariables(model.getFileScope()), queryName));
        if (retval.isEmpty()) {
            Set<VariableElement> vars = getIndex().getTopLevelVariables(NameKind.exact(queryName));
            for (VariableElement indexedVariable : forFiles(getFileObject()).prefer(vars)) {
                retval.add(new VariableNameImpl(this, indexedVariable));
            }
        }
        return retval;
    }

    @Override
    public List<? extends MethodScope> findMethods(TypeScope type) {
        List<MethodScope> retval = new ArrayList<>();
        //PhpModifiers attribs = new PhpModifiers(modifiers);
        Set<MethodElement> methods = getIndex().getDeclaredMethods(type);
        for (MethodElement idxFunc : forFiles(getFileObject()).prefer(methods)) {
            retval.add(new MethodScopeImpl(type, idxFunc));
        }
        return retval;
    }

    @Override
    public List<? extends ClassConstantElement> findClassConstants(TypeScope type) {
        List<ClassConstantElement> retval = new ArrayList<>();
        Set<TypeConstantElement> constants = getIndex().getDeclaredTypeConstants(type);
        for (TypeConstantElement con : forFiles(getFileObject()).prefer(constants)) {
            retval.add(new ClassConstantElementImpl(type, con));
        }
        return retval;
    }

    @Override
    public List<? extends CaseElement> findEnumCases(TypeScope type) {
        List<CaseElement> retval = new ArrayList<>();
        Set<EnumCaseElement> enumCases = getIndex().getDeclaredEnumCases(type);
        for (EnumCaseElement enumCase : forFiles(getFileObject()).prefer(enumCases)) {
            retval.add(new CaseElementImpl(type, enumCase));
        }
        return retval;
    }

    @Override
    public List<? extends MethodScope> findMethods(TypeScope type, String queryName, int... modifiers) {
        List<MethodScope> retval = new ArrayList<>();
        //PhpModifiers attribs = new PhpModifiers(modifiers);
        Set<MethodElement> methods = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredMethods(type));
        for (MethodElement idxFunc : forFiles(getFileObject()).prefer(methods)) {
            retval.add(new MethodScopeImpl(type, idxFunc));
        }
        return retval;
    }

    @Override
    public List<? extends MethodScope> findInheritedMethods(TypeScope typeScope, String queryName) {
        List<MethodScope> retval = new ArrayList<>();
        Set<MethodElement> methods = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getInheritedMethods(typeScope));
        for (MethodElement idxFunc : forFiles(getFileObject()).prefer(methods)) {
            retval.add(new MethodScopeImpl(typeScope, idxFunc));
        }
        return retval;
    }

    @Override
    public List<? extends ClassConstantElement> findClassConstants(TypeScope type, String queryName) {
        List<ClassConstantElement> retval = new ArrayList<>();
        Set<TypeConstantElement> constants = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredTypeConstants(type));
        for (TypeConstantElement con : forFiles(getFileObject()).prefer(constants)) {
            retval.add(new ClassConstantElementImpl(type, con));
        }
        return retval;
    }

    @Override
    public List<? extends CaseElement> findEnumCases(TypeScope type, String queryName) {
        List<CaseElement> retval = new ArrayList<>();
        Set<EnumCaseElement> enumCases = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredEnumCases(type));
        for (EnumCaseElement caseElement : forFiles(getFileObject()).prefer(enumCases)) {
            retval.add(new CaseElementImpl(type, caseElement));
        }
        return retval;
    }

    @Override
    public List<? extends ClassConstantElement> findInheritedClassConstants(ClassScope type, String queryName) {
        List<ClassConstantElement> retval = new ArrayList<>();
        Set<TypeConstantElement> constants = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getInheritedTypeConstants(type));
        for (TypeConstantElement con : forFiles(getFileObject()).prefer(constants)) {
            retval.add(new ClassConstantElementImpl(type, con));
        }
        return retval;
    }

    @Override
    public List<? extends FieldElement> findFields(ClassScope clsScope, final int... modifiers) {
        List<FieldElement> retval = new ArrayList<>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = getIndex().getDeclaredFields(clsScope);
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : forFiles(getFileObject()).prefer(fields)) {
            retval.add(new FieldElementImpl(clsScope, fld));
        }
        return retval;
    }

    @Override
    public List<? extends FieldElement> findFields(TraitScope traitScope, final int... modifiers) {
        List<FieldElement> retval = new ArrayList<>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = getIndex().getDeclaredFields(traitScope);
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : forFiles(getFileObject()).prefer(fields)) {
            retval.add(new FieldElementImpl(traitScope, fld));
        }
        return retval;
    }

    @Override
    public List<? extends FieldElement> findFields(ClassScope clsScope, final String queryName, final int... modifiers) {
        List<FieldElement> retval = new ArrayList<>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredFields(clsScope));
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : forFiles(getFileObject()).prefer(fields)) {
            retval.add(new FieldElementImpl(clsScope, fld));
        }
        return retval;
    }

    @Override
    public List<? extends FieldElement> findFields(TraitScope traitScope, final String queryName, final int... modifiers) {
        List<FieldElement> retval = new ArrayList<>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredFields(traitScope));
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : forFiles(getFileObject()).prefer(fields)) {
            retval.add(new FieldElementImpl(traitScope, fld));
        }
        return retval;
    }

    public IndexScopeImpl getCachedModelSupport() {
        return null;
    }

    @Override
    public List<? extends FieldElement> findInheritedFields(ClassScope clsScope, String queryName) {
        List<FieldElement> retval = new ArrayList<>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getInheritedFields(clsScope));
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : forFiles(getFileObject()).prefer(fields)) {
            retval.add(new FieldElementImpl(clsScope, fld));
        }
        return retval;
    }

    @Override
    public OffsetRange getBlockRange() {
        return getNameRange();
    }

    @Override
    public List<? extends ModelElementImpl> getElements() {
        throw new IllegalStateException();
    }
}
