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

package org.netbeans.modules.php.editor.elements;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.TraitedElement;
import org.netbeans.modules.php.editor.api.elements.TreeElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;

/**
 * @author Radek Matous
 */
final class TypeTreeElementImpl  implements TreeElement<TypeElement> {
    private final TypeElement delegate;
    private final Set<TypeElement> preferredTypes;
    private final boolean superTypesAsChildren;
    private volatile Set<TreeElement<TypeElement>> children;

    TypeTreeElementImpl(final TypeElement delegate, final boolean superTypesAsChildren) {
        this(delegate, new HashSet<TypeElement>(), superTypesAsChildren);
    }
    TypeTreeElementImpl(final TypeElement delegate, final Set<TypeElement> preferredTypes, boolean superTypesAsChildren) {
        this.delegate = delegate;
        this.preferredTypes = preferredTypes;
        this.superTypesAsChildren = superTypesAsChildren;
    }

    @Override
    public Set<TreeElement<TypeElement>> children() {
        if (children == null) {
            if (superTypesAsChildren) {
                children = childrenForSuperTypes();
            } else {
                children = childrenForSubTypes();
            }
        }
        return children;
    }

    private Set<TreeElement<TypeElement>> childrenForSuperTypes() {
        final HashSet<TreeElement<TypeElement>> directTypes = new HashSet<>();
        if (delegate instanceof ClassElement) {
            final QualifiedName superClassName = ((ClassElement) delegate).getSuperClassName();
            if (superClassName != null) {
                addDirectType(superClassName, directTypes);
            }
        }
        for (final QualifiedName iface : delegate.getSuperInterfaces()) {
            addDirectType(iface, directTypes);
        }
        if (delegate.isTraited()) {
            TraitedElement traitedElement = (TraitedElement) delegate;
            for (QualifiedName trait : traitedElement.getUsedTraits()) {
                addDirectType(trait, directTypes);
            }
        }
        return directTypes;
    }

    private void addDirectType(final QualifiedName typeName, final HashSet<TreeElement<TypeElement>> directTypes) {
        final ElementFilter forName = ElementFilter.forName(NameKind.exact(typeName));
        Set<TypeElement> types = forName.filter(preferredTypes);
        if (types.isEmpty()) {
            Index index = getIndex();
            types = index.getTypes(NameKind.exact(typeName));
        }
        for (TypeElement typeElementImpl : types) {
            directTypes.add(new TypeTreeElementImpl(typeElementImpl, preferredTypes, superTypesAsChildren));
        }
    }

    private Set<TreeElement<TypeElement>> childrenForSubTypes() {
        final HashSet<TreeElement<TypeElement>> directTypes = new HashSet<>();

        Index index = getIndex();
        Set<TypeElement> directInheritedByTypes = index.getDirectInheritedByTypes(delegate);
        for (TypeElement typeElement : directInheritedByTypes) {
            directTypes.add(new TypeTreeElementImpl(typeElement, preferredTypes, superTypesAsChildren));
        }

        return directTypes;
    }

    @Override
    public TypeElement getElement() {
        return delegate;
    }

    private Index getIndex() {
        final ElementQuery elementQuery = delegate.getElementQuery();
        boolean indexScope = elementQuery.getQueryScope().isIndexScope();
        if (indexScope && (elementQuery instanceof Index)) {
            return (Index) elementQuery;
        }
        return null;
    }
}
