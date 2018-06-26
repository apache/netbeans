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
package org.netbeans.modules.php.editor.api.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public abstract class ElementFilter {

    public ElementFilter() {
    }

    public static ElementFilter allOf(final Collection<ElementFilter> filters) {
        return ElementFilter.allOf(filters.toArray(new ElementFilter[filters.size()]));
    }

    public static ElementFilter allOf(final ElementFilter... filters) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (ElementFilter elementFilter : filters) {
                    if (!elementFilter.isAccepted(element)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static ElementFilter anyOf(final Collection<ElementFilter> filters) {
        return ElementFilter.anyOf(filters.toArray(new ElementFilter[filters.size()]));
    }

    public static ElementFilter anyOf(final ElementFilter... filters) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (ElementFilter elementFilter : filters) {
                    if (elementFilter.isAccepted(element)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ElementFilter forName(final NameKind name) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return name.matchesName(element);
            }
        };
    }

    public static ElementFilter forConstructor() {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                boolean result = false;
                if (element instanceof MethodElement) {
                    result = ((MethodElement) element).isConstructor();
                }
                return result;
            }
        };
    }

    public static ElementFilter forIncludedNames(final Collection<String> includedNames, final PhpElementKind kind) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (String name : includedNames) {
                    if (NameKind.exact(name).matchesName(kind, element.getName())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ElementFilter forExcludedNames(final Collection<String> excludedNames, final PhpElementKind kind) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                for (String name : excludedNames) {
                    if (NameKind.exact(name).matchesName(kind, element.getName())) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
    public static <T extends PhpElement> ElementFilter forExcludedElements(final Collection<T> excludedElements) {
        return new ElementFilter() {
            private ElementFilter delegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (delegate == null) {
                    PhpElementKind kind = PhpElementKind.CLASS;
                    if (excludedElements.size() > 0) {
                        kind = excludedElements.iterator().next().getPhpElementKind();
                    }
                    delegate  = ElementFilter.forExcludedNames(toNames(excludedElements), kind);
                }
                return delegate.isAccepted(element);
            }
        };

    }


    public static ElementFilter forOffset(final int offset) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getOffset() == offset;
            }
        };
    }

    public static ElementFilter forKind(final PhpElementKind kind) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpElementKind().equals(kind);
            }
        };
    }

    public static ElementFilter forVirtualExtensions() {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getElementQuery().getQueryScope().isVirtualScope();
            }
        };
    }

    public static ElementFilter forFiles(final FileObject... files) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                boolean retval = true;
                for (FileObject fileObject : files) {
                    //if file is deleted
                    if (fileObject == null) {
                        continue;
                    }
                    String nameExt = fileObject.getNameExt();
                    String elementURL = element.getFilenameUrl();
                    if ((elementURL != null && elementURL.indexOf(nameExt) < 0) || element.getFileObject() != fileObject) {
                        retval = false;
                        break;
                    }
                }
                return retval;
            }
        };
    }

    public static ElementFilter forSuperClassName(final QualifiedName supeClassNameQuery) {
        return new ElementFilter() {
            final NameKind.Exact superNameKind = NameKind.exact(supeClassNameQuery);
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof ClassElement) {
                    final QualifiedName nextSuperName = ((ClassElement) element).getSuperClassName();
                    return nextSuperName != null ? superNameKind.matchesName(PhpElementKind.CLASS, nextSuperName) : false;
                }
                return true;
            }
        };
    }

    public static ElementFilter forSuperInterfaceNames(final Set<QualifiedName> supeIfaceNameQueries) {
        final Set<ElementFilter> filters = new HashSet<>();
        for (final QualifiedName qualifiedName : supeIfaceNameQueries) {
            filters.add(forSuperInterfaceName(qualifiedName));
        }
        return ElementFilter.allOf(filters.toArray(new ElementFilter[filters.size()]));
    }

    public static ElementFilter forSuperInterfaceName(final QualifiedName supeIfaceNameQuery) {
        return new ElementFilter() {
            final NameKind.Exact superNameKind = NameKind.exact(supeIfaceNameQuery);
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeElement) {
                    Set<QualifiedName> superInterfaces = ((TypeElement) element).getSuperInterfaces();
                    for (QualifiedName nextSuperName : superInterfaces) {
                        if (superNameKind.matchesName(PhpElementKind.IFACE, nextSuperName)) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        };
    }

    public static ElementFilter forTypesFromNamespaces(final Set<QualifiedName> namespaces) {
        Set<ElementFilter> filters = new HashSet<>();
        for (QualifiedName ns : namespaces) {
            filters.add(ElementFilter.forTypesFromNamespace(ns));
        }
        return ElementFilter.anyOf(filters);

    }

    public static ElementFilter forTypesFromNamespace(final QualifiedName namespace) {
        final Exact nsName = NameKind.exact(namespace);
        return new ElementFilter() {
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeElement) {
                    TypeElement type = (TypeElement) element;
                    return nsName.matchesName(type.getPhpElementKind(), type.getNamespaceName());
                }
                return true;
            }
        };
    }

    public static ElementFilter forEqualTypes(final TypeElement typeElement) {
        return ElementFilter.allOf(
                ElementFilter.forName(NameKind.exact(typeElement.getName())),
                ElementFilter.forFiles(typeElement.getFileObject())/*,
                ElementFilter.forOffset(typeElement.getOffset())*/);

    }

    public static ElementFilter forMembersOfTypes(final Set<TypeElement> typeElements) {
        List<ElementFilter> filters = new ArrayList<>();
        for (TypeElement typeElement : typeElements) {
            filters.add(ElementFilter.forMembersOfType(typeElement));
        }
        return ElementFilter.anyOf(filters);
    }

    public static ElementFilter forMembersOfType(final TypeElement typeElement) {
        return new ElementFilter() {
            private ElementFilter filterDelegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    if (filterDelegate == null) {
                        filterDelegate = forEqualTypes(typeElement);
                    }
                    //return thisTypeElement.equals(typeElement);
                    return filterDelegate.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }
    public static ElementFilter forMembersOfInterface() {
        return new ElementFilter() {
            private ElementFilter filterDelegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    if (filterDelegate == null) {
                        filterDelegate = forKind(PhpElementKind.IFACE);
                    }
                    //return thisTypeElement.equals(typeElement);
                    return filterDelegate.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }
    public static ElementFilter forMembersOfClass() {
        return new ElementFilter() {
            private ElementFilter filterDelegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    if (filterDelegate == null) {
                        filterDelegate = forKind(PhpElementKind.CLASS);
                    }
                    //return thisTypeElement.equals(typeElement);
                    return filterDelegate.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }
    public static ElementFilter forMembersOfTypeName(final TypeElement typeElement) {
        return new ElementFilter() {
            private ElementFilter filterDelegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    if (filterDelegate == null) {
                        filterDelegate = forName(NameKind.exact(typeElement.getFullyQualifiedName()));
                    }
                    return filterDelegate.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }
    public static ElementFilter forMembersOfTypeName(final NameKind name) {
        return new ElementFilter() {
            private ElementFilter filterDelegate = null;
            @Override
            public boolean isAccepted(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    if (filterDelegate == null) {
                        filterDelegate = forName(name);
                    }
                    return filterDelegate.isAccepted(((TypeMemberElement) element).getType());
                }
                return true;
            }
        };
    }

    public static <T extends PhpElement> ElementFilter forInstanceOf(final Class<T> cls) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return cls.isAssignableFrom(element.getClass());
            }
        };
    }

    public static ElementFilter forAnyOfFlags(final int flags) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return (element.getPhpModifiers().toFlags() & flags) != 0;
            }
        };
    }

    public static ElementFilter forAllOfFlags(final int flags) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return (element.getPhpModifiers().toFlags() & flags) == flags;
            }
        };
    }

    /**
     * @param publicOrNot true means that not public elements are filtered and only public are returned.
     * False means that pulic elements are filtered and and only not public are returned
     */
    public static ElementFilter forPublicModifiers(final boolean publicOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpModifiers().isPublic() == publicOrNot;
            }
        };
    }

    public static ElementFilter forPrivateModifiers(final boolean privateOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpModifiers().isPrivate() == privateOrNot;
            }
        };
    }

    public static ElementFilter forStaticModifiers(final boolean staticOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.getPhpModifiers().isStatic() == staticOrNot;
            }
        };
    }

    public static ElementFilter forDeprecated(final boolean deprecatedOrNot) {
        return new ElementFilter() {

            @Override
            public boolean isAccepted(PhpElement element) {
                return element.isDeprecated() == deprecatedOrNot;
            }
        };
    }

    public abstract boolean isAccepted(PhpElement element);

    public <T extends PhpElement> Set<T> filter(T original) {
        return filter(Collections.<T>singleton(original));
    }
    public <T extends PhpElement> Set<T> filter(Set<T> original) {
        Set<T> retval = new HashSet<>();
        for (T baseElement : original) {
            if (isAccepted(baseElement)) {
                retval.add(baseElement);
            }
        }
        return Collections.unmodifiableSet(retval);
    }
    public <T extends PhpElement> Set<T> reverseFilter(Set<T> original) {
        Set<T> retval = new HashSet<>();
        for (T baseElement : original) {
            if (!isAccepted(baseElement)) {
                retval.add(baseElement);
            }
        }
        return Collections.unmodifiableSet(retval);
    }

    //slow impl.
    public <T extends PhpElement> Set<T> prefer(Set<T> original) {
        Set<T> retval = original;
        Set<T> notAccepted = new HashSet<>();
        Map<T, ElementFilter>  accepted = new HashMap<>();

        for (T baseElement : original) {
            if (isAccepted(baseElement)) {
                List<ElementFilter> filters = new ArrayList<>();
                if (baseElement instanceof FullyQualifiedElement) {
                    FullyQualifiedElement fqnElement = (FullyQualifiedElement) baseElement;
                    filters.add(ElementFilter.forName(NameKind.exact(fqnElement.getFullyQualifiedName())));
                } else {
                    filters.add(ElementFilter.forName(NameKind.exact(baseElement.getName())));
                }
                if (baseElement instanceof TypeMemberElement) {
                    TypeMemberElement member = (TypeMemberElement) baseElement;
                    filters.add(ElementFilter.forMembersOfTypeName(member.getType()));
                }
                accepted.put(baseElement, ElementFilter.allOf(filters));
            } else {
                notAccepted.add(baseElement);
            }
        }
        if (accepted.size() > 0 && notAccepted.size() > 0) {
            retval = new HashSet<>(original);
            for (Entry<T, ElementFilter> entry : accepted.entrySet()) {
                ElementFilter filter = entry.getValue();
                retval.removeAll(filter.filter(notAccepted));
            }
        }

        return Collections.unmodifiableSet(retval);
    }

    private static Set<String> toNames(Collection<? extends PhpElement> elements) {
        Set<String> names = new HashSet<>();
        for (PhpElement elem : elements) {
            names.add(elem.getName());
        }
        return names;
    }

}
