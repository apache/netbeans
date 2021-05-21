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

package org.netbeans.modules.php.editor.csl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public class OverridingMethodsImpl implements OverridingMethods {
    private String classSignatureForInheritedMethods = ""; //NOI18N
    private String classSignatureForInheritedByMethods = ""; //NOI18N
    private String classSignatureForInheritedByTypes = ""; //NOI18N
    /** just very simple implementation for now*/
    private Set<MethodElement> inheritedMethods = Collections.emptySet();
    private Set<MethodElement> inheritedByMethods = Collections.emptySet();
    private Set<TypeElement> inheritedByTypes = new LinkedHashSet<>();
    @Override
    public Collection<? extends AlternativeLocation> overrides(ParserResult info, ElementHandle handle) {
        assert handle instanceof ModelElement;
        if (handle instanceof MethodScope) {
            MethodScope method = (MethodScope) handle;
            final ElementFilter methodNameFilter = ElementFilter.forName(NameKind.exact(method.getName()));
            final Set<MethodElement> overridenMethods = methodNameFilter.filter(getInheritedMethods(info, method));
            List<AlternativeLocation> retval = new ArrayList<>();
            for (MethodElement methodElement : overridenMethods) {
                retval.add(MethodLocation.newInstance(methodElement));
            }
            return retval;
        }
        return null;
    }

    @Override
    public Collection<? extends AlternativeLocation> overriddenBy(ParserResult info, ElementHandle handle) {
        assert handle instanceof ModelElement;
        if (handle instanceof MethodScope) {
            MethodScope method = (MethodScope) handle;
            final ElementFilter methodNameFilter = ElementFilter.forName(NameKind.exact(method.getName()));
            final Set<MethodElement> overridenByMethods = methodNameFilter.filter(getInheritedByMethods(info, method));
            List<AlternativeLocation> retval = new ArrayList<>();
            for (MethodElement methodElement : overridenByMethods) {
                retval.add(MethodLocation.newInstance(methodElement));
            }
            return retval;
        } else if (handle instanceof TypeScope) {
            List<AlternativeLocation> retval = new ArrayList<>();
            for (TypeElement typeElement : getInheritedByTypes(info, (TypeScope) handle)) {
                retval.add(TypeLocation.newInstance(typeElement));
            }
            return retval;
        }

        return null;
    }


    @Override
    public boolean isOverriddenBySupported(ParserResult info, ElementHandle handle) {
        return true;
    }

    /**
     * @return the inheritedMethods
     */
    private Set<MethodElement> getInheritedMethods(final ParserResult info, final MethodScope method) {
        Scope inScope = method.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = typeScope.getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedMethods)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedMethods = index.getInheritedMethods(typeScope);
        }
        classSignatureForInheritedMethods = signature;
        return inheritedMethods;
    }


    /**
     * @return the inheritedByTypes
     */
    private Set<TypeElement> getInheritedByTypes(final ParserResult info, final TypeScope type) {
        final String signature = type.getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedByTypes)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedByTypes = index.getInheritedByTypes(type);
        }
        classSignatureForInheritedByTypes = signature;
        return inheritedByTypes;
    }

    /**
     * @return the inheritedByMethods
     */
    private Set<MethodElement> getInheritedByMethods(final ParserResult info, final MethodScope method) {
        Scope inScope = method.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = ((TypeScope) inScope).getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedByMethods)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedByMethods = new HashSet<>();
            for (TypeElement nextType : getInheritedByTypes(info, typeScope)) {
                inheritedByMethods.addAll(index.getDeclaredMethods(nextType));
            }
        }
        classSignatureForInheritedByMethods = signature;
        return inheritedByMethods;
    }

    private static final class MethodLocation extends DeclarationFinderImpl.AlternativeLocationImpl {

        public static MethodLocation newInstance(PhpElement modelElement) {
            FileObject fileObject = modelElement.getFileObject();
            DeclarationLocation declarationLocation = fileObject == null ? DeclarationLocation.NONE : new DeclarationLocation(fileObject, modelElement.getOffset(), modelElement);
            return new MethodLocation(modelElement, declarationLocation);
        }

        private MethodLocation(PhpElement modelElement, DeclarationLocation declarationLocation) {
            super(modelElement, declarationLocation);
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder(30);
            MethodElement method = (MethodElement) getElement();
            final TypeElement type = method.getType();
            sb.append(type.getFullyQualifiedName().toNotFullyQualified().toString());
            final FileObject fileObject = type.getFileObject();
            if (fileObject != null) {
                sb.append(" ("); // NOI18N
                sb.append(fileObject.getNameExt());
                sb.append(")"); // NOI18N
            }
            return sb.toString();
        }
    }
    private static final class TypeLocation extends DeclarationFinderImpl.AlternativeLocationImpl {

        public static TypeLocation newInstance(PhpElement modelElement) {
            FileObject fileObject = modelElement.getFileObject();
            DeclarationLocation declarationLocation = fileObject == null ? DeclarationLocation.NONE : new DeclarationLocation(fileObject, modelElement.getOffset(), modelElement);
            return new TypeLocation(modelElement, declarationLocation);
        }

        private TypeLocation(PhpElement modelElement, DeclarationLocation declarationLocation) {
            super(modelElement, declarationLocation);
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder(30);
            TypeElement type = (TypeElement) getElement();
            sb.append(type.getFullyQualifiedName().toNotFullyQualified().toString());
            FileObject fileObject = type.getFileObject();
            if (fileObject != null) {
                sb.append(" ("); // NOI18N
                sb.append(fileObject.getNameExt());
                sb.append(")"); // NOI18N
            }
            return sb.toString();
        }
    }

}
