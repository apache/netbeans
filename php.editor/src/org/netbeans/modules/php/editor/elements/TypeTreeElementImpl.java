/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
