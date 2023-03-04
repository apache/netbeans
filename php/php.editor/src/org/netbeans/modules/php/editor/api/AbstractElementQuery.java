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
package org.netbeans.modules.php.editor.api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;

/**
 *
 * @author Radek Matous
 */
public class AbstractElementQuery implements ElementQuery {

    private final LinkedList<PhpElement> elements = new LinkedList<>();
    final QueryScope queryScope;

    public AbstractElementQuery(final QueryScope queryScope) {
        this.queryScope = queryScope;
    }

    @Override
    public final Set<ClassElement> getClasses() {
        return getElements(ClassElement.class);
    }

    @Override
    public final Set<ClassElement> getClasses(NameKind query) {
        return getElements(ClassElement.class, query);
    }

    @Override
    public final Set<ConstantElement> getConstants() {
        return getElements(ConstantElement.class);
    }

    @Override
    public final Set<ConstantElement> getConstants(NameKind query) {
        return getElements(ConstantElement.class, query);
    }

    @Override
    public final Set<MethodElement> getConstructors(NameKind typeQuery) {
        ElementFilter forName = ElementFilter.forName(NameKind.exact(MethodElement.CONSTRUCTOR_NAME));
        return forName.filter(getElements(MethodElement.class, typeQuery));
    }

    @Override
    public final Set<FieldElement> getFields(Exact classQuery, NameKind fieldQuery) {
        return getElements(FieldElement.class, classQuery, fieldQuery);
    }

    @Override
    public final Set<FieldElement> getFields(NameKind fieldQuery) {
        return getElements(FieldElement.class, fieldQuery);
    }

    @Override
    public final Set<FunctionElement> getFunctions() {
        return getElements(FunctionElement.class);
    }

    @Override
    public final Set<FunctionElement> getFunctions(NameKind query) {
        return getElements(FunctionElement.class, query);
    }

    @Override
    public final Set<InterfaceElement> getInterfaces() {
        return getElements(InterfaceElement.class);
    }

    @Override
    public final Set<InterfaceElement> getInterfaces(NameKind query) {
        return getElements(InterfaceElement.class, query);
    }

    @Override
    public final Set<MethodElement> getMethods(Exact typeQuery, NameKind methodQuery) {
        return getElements(MethodElement.class, typeQuery, methodQuery);
    }

    @Override
    public final Set<MethodElement> getMethods(NameKind methodQuery) {
        return getElements(MethodElement.class, methodQuery);
    }

    @Override
    public final Set<NamespaceElement> getNamespaces(NameKind query) {
        return getElements(NamespaceElement.class, query);
    }

    @Override
    public final Set<TypeConstantElement> getTypeConstants(Exact typeQuery, NameKind constantQuery) {
        return getElements(TypeConstantElement.class, typeQuery, constantQuery);
    }

    @Override
    public final Set<TypeConstantElement> getTypeConstants(NameKind constantQuery) {
        return getElements(TypeConstantElement.class, constantQuery);
    }

    @Override
    public Set<EnumCaseElement> getEnumCases(Exact typeQuery, NameKind enumCaseQuery) {
        return getElements(EnumCaseElement.class, typeQuery, enumCaseQuery);
    }

    @Override
    public Set<EnumCaseElement> getEnumCases(NameKind enumCaseQuery) {
        return getElements(EnumCaseElement.class, enumCaseQuery);
    }

    @Override
    public final Set<TypeMemberElement> getTypeMembers(Exact typeQuery, NameKind memberQuery) {
        return getElements(TypeMemberElement.class, memberQuery);
    }

    @Override
    public final Set<TypeElement> getTypes(NameKind query) {
        return getElements(TypeElement.class, query);
    }

    @Override
    public Set<VariableElement> getTopLevelVariables(NameKind query) {
        return getElements(VariableElement.class, query);
    }

    @Override
    public QueryScope getQueryScope() {
        return queryScope;
    }

    public final synchronized <T extends PhpElement> Set<T> getElements(Class<T> clz) {
        Set<T> retval = new HashSet<>();
        final ElementFilter clsFilter = ElementFilter.forInstanceOf(clz);
        for (PhpElement phpElement : getElements()) {
            if (clsFilter.isAccepted(phpElement)) {
                retval.add((T) phpElement);
            }
        }
        return retval;
    }


    public final <T extends PhpElement> Set<T> getElements(Class<T> clz, NameKind query) {
        return ElementFilter.forName(query).filter(getElements(clz));
    }

    public final <T extends TypeMemberElement> Set<T> getElements(Class<T> clz, NameKind.Exact typeQuery, NameKind memberQuery) {
        return ElementFilter.allOf(ElementFilter.forName(memberQuery), ElementFilter.forMembersOfTypeName(typeQuery)).filter(getElements(clz));
    }

    public final synchronized void addElement(final PhpElement retval) {
        getElements().add(retval);
    }

    public final synchronized void addElements(final Set<? extends PhpElement> retval) {
        getElements().addAll(retval);
    }

    public final synchronized <T extends PhpElement> T getLast(Class<T> clz) {
        final ElementFilter clsFilter = ElementFilter.forInstanceOf(clz);
        for (Iterator<PhpElement> it = elements.descendingIterator(); it.hasNext();) {
            PhpElement phpElement = it.next();
            if (clsFilter.isAccepted(phpElement)) {
                return (T) phpElement;
            }
        }
        return null;
    }

    public final synchronized PhpElement getAnyLast(Class... classes) {
        final Set<ElementFilter> filters = new HashSet<>();
        for (Class clz : classes) {
            filters.add(ElementFilter.forInstanceOf(clz));
        }
        final ElementFilter mergeFilter = ElementFilter.anyOf(filters);
        for (Iterator<PhpElement> it = elements.descendingIterator(); it.hasNext();) {
            PhpElement phpElement = it.next();
            if (mergeFilter.isAccepted(phpElement)) {
                return phpElement;
            }
        }
        return null;
    }

    /**
     * @return the elements
     */
    public LinkedList<PhpElement> getElements() {
        return elements;
    }
}
