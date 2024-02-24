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
        return ElementFilter.allOf(filters.toArray(new ElementFilter[0]));
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
        return ElementFilter.anyOf(filters.toArray(new ElementFilter[0]));
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
        return ElementFilter.allOf(filters.toArray(new ElementFilter[0]));
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
