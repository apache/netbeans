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
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public class OverridingMethodsImpl implements OverridingMethods {

    private String classSignatureForInheritedMethods = ""; //NOI18N
    private String classSignatureForInheritedByMethods = ""; //NOI18N
    private String classSignatureForInheritedByTypes = ""; //NOI18N
    private String classSignatureForInheritedFields = ""; //NOI18N
    private String classSignatureForInheritedByFields = ""; //NOI18N
    private String classSignatureForInheritedClassConstants = ""; //NOI18N
    private String classSignatureForInheritedByClassConstants = ""; //NOI18N
    /** just very simple implementation for now*/
    private Set<MethodElement> inheritedMethods = Collections.emptySet();
    private Set<MethodElement> inheritedByMethods = Collections.emptySet();
    private Set<TypeElement> inheritedByTypes = new LinkedHashSet<>();
    private Set<org.netbeans.modules.php.editor.api.elements.FieldElement> inheritedFields = Collections.emptySet();
    private Set<org.netbeans.modules.php.editor.api.elements.FieldElement> inheritedByFields = Collections.emptySet();
    private Set<TypeConstantElement> inheritedClassConstants = Collections.emptySet();
    private Set<TypeConstantElement> inheritedByClassConstants = Collections.emptySet();

    @Override
    public Collection<? extends AlternativeLocation> overrides(ParserResult info, ElementHandle handle) {
        assert handle instanceof ModelElement;
        if (handle instanceof MethodScope) {
            MethodScope method = (MethodScope) handle;
            final ElementFilter methodNameFilter = ElementFilter.forName(NameKind.exact(method.getName()));
            Set<MethodElement> overridenMethods = methodNameFilter.filter(getInheritedMethods(info, method));
            List<AlternativeLocation> retval = new ArrayList<>();
            Scope typeScope = method.getInScope();
            if (!(typeScope instanceof TraitScope)) {
                ElementFilter notPrivateFilter = ElementFilter.forPrivateModifiers(false);
                overridenMethods = notPrivateFilter.filter(overridenMethods);
            }
            for (MethodElement methodElement : overridenMethods) {
                retval.add(MethodLocation.newInstance(methodElement));
            }
            return retval;
        } else if (handle instanceof FieldElement) {
            FieldElement field = (FieldElement) handle;
            final ElementFilter fieldNameFilter = ElementFilter.forName(NameKind.exact(field.getName()));
            Set<org.netbeans.modules.php.editor.api.elements.FieldElement> overridenFields = fieldNameFilter.filter(getInheritedFields(info, field));
            List<AlternativeLocation> retval = new ArrayList<>();
            for (org.netbeans.modules.php.editor.api.elements.FieldElement fieldElement : overridenFields) {
                retval.add(FieldLocation.newInstance(fieldElement));
            }
            return retval;
        } else if (handle instanceof ClassConstantElement) {
            ClassConstantElement constant = (ClassConstantElement) handle;
            final ElementFilter constantNameFilter = ElementFilter.forName(NameKind.exact(constant.getName()));
            Set<TypeConstantElement> overridenConstants = constantNameFilter.filter(getInheritedClassConstants(info, constant));
            List<AlternativeLocation> retval = new ArrayList<>();
            for (TypeConstantElement constantElement : overridenConstants) {
                retval.add(ClassConstantLocation.newInstance(constantElement));
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
        } else if (handle instanceof FieldElement) {
            FieldElement field = (FieldElement) handle;
            final ElementFilter fieldFilter = ElementFilter.allOf(
                    ElementFilter.forName(NameKind.exact(field.getName())),
                    ElementFilter.forPrivateModifiers(false)
            );
            final Set<org.netbeans.modules.php.editor.api.elements.FieldElement> overridenByFields = fieldFilter.filter(getInheritedByFields(info, field));
            List<AlternativeLocation> retval = new ArrayList<>();
            for (org.netbeans.modules.php.editor.api.elements.FieldElement fieldElement : overridenByFields) {
                retval.add(FieldLocation.newInstance(fieldElement));
            }
            return retval;
        } else if (handle instanceof ClassConstantElement) {
            ClassConstantElement constant = (ClassConstantElement) handle;
            final ElementFilter constantFilter = ElementFilter.allOf(
                    ElementFilter.forName(NameKind.exact(constant.getName())),
                    ElementFilter.forPrivateModifiers(false)
            );
            final Set<TypeConstantElement> overridenByConstants = constantFilter.filter(getInheritedByClassConstants(info, constant));
            List<AlternativeLocation> retval = new ArrayList<>();
            for (TypeConstantElement constantElement : overridenByConstants) {
                retval.add(ClassConstantLocation.newInstance(constantElement));
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
        return Collections.unmodifiableSet(inheritedMethods);
    }

    /**
     * Get inherited fields.
     *
     * @param info the parser result
     * @param field the filed
     * @return the inherited fields
     */
    private Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getInheritedFields(final ParserResult info, final FieldElement field) {
        Scope inScope = field.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = typeScope.getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedFields)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedFields = index.getInheritedFields(typeScope);
        }
        classSignatureForInheritedFields = signature;
        return Collections.unmodifiableSet(inheritedFields);
    }

    /**
     * Get inherited class constants.
     *
     * @param info the parser result
     * @param constant the constant
     * @return the inherited constants
     */
    private Set<TypeConstantElement> getInheritedClassConstants(final ParserResult info, final ClassConstantElement constant) {
        Scope inScope = constant.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = typeScope.getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedClassConstants)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedClassConstants = index.getInheritedTypeConstants(typeScope);
        }
        classSignatureForInheritedClassConstants = signature;
        return Collections.unmodifiableSet(inheritedClassConstants);
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
        return Collections.unmodifiableSet(inheritedByTypes);
    }

    /**
     * @return the inheritedByMethods
     */
    private Set<MethodElement> getInheritedByMethods(final ParserResult info, final MethodScope method) {
        Scope inScope = method.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        if (!(typeScope instanceof TraitScope)
                && method.getPhpModifiers().isPrivate()) {
            return Collections.emptySet();
        }
        final String signature = ((TypeScope) inScope).getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedByMethods)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedByMethods = new HashSet<>();
            for (TypeElement nextType : getInheritedByTypes(info, typeScope)) {
                inheritedByMethods.addAll(index.getDeclaredMethods(nextType));
            }
        }
        classSignatureForInheritedByMethods = signature;
        return Collections.unmodifiableSet(inheritedByMethods);
    }

    /**
     * @return the inheritedByFields
     */
    private Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getInheritedByFields(final ParserResult info, final FieldElement field) {
        Scope inScope = field.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = ((TypeScope) inScope).getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedByFields)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedByFields = new HashSet<>();
            for (TypeElement nextType : getInheritedByTypes(info, typeScope)) {
                inheritedByFields.addAll(index.getDeclaredFields(nextType));
            }
        }
        classSignatureForInheritedByFields = signature;
        return Collections.unmodifiableSet(inheritedByFields);
    }

    /**
     * @return the inheritedByClassConstants
     */
    private Set<TypeConstantElement> getInheritedByClassConstants(final ParserResult info, final ClassConstantElement constant) {
        Scope inScope = constant.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = ((TypeScope) inScope).getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedByClassConstants)) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedByClassConstants = new HashSet<>();
            for (TypeElement nextType : getInheritedByTypes(info, typeScope)) {
                inheritedByClassConstants.addAll(index.getDeclaredTypeConstants(nextType));
            }
        }
        classSignatureForInheritedByClassConstants = signature;
        return Collections.unmodifiableSet(inheritedByClassConstants);
    }

    //~ inner classes
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

    private static final class FieldLocation extends DeclarationFinderImpl.AlternativeLocationImpl {

        public static FieldLocation newInstance(PhpElement modelElement) {
            FileObject fileObject = modelElement.getFileObject();
            DeclarationLocation declarationLocation = fileObject == null ? DeclarationLocation.NONE : new DeclarationLocation(fileObject, modelElement.getOffset(), modelElement);
            return new FieldLocation(modelElement, declarationLocation);
        }

        private FieldLocation(PhpElement modelElement, DeclarationLocation declaration) {
            super(modelElement, declaration);
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder(30);
            org.netbeans.modules.php.editor.api.elements.FieldElement field = (org.netbeans.modules.php.editor.api.elements.FieldElement) getElement();
            final TypeElement type = field.getType();
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

    private static final class ClassConstantLocation extends DeclarationFinderImpl.AlternativeLocationImpl {

        public static ClassConstantLocation newInstance(PhpElement modelElement) {
            FileObject fileObject = modelElement.getFileObject();
            DeclarationLocation declarationLocation = fileObject == null ? DeclarationLocation.NONE : new DeclarationLocation(fileObject, modelElement.getOffset(), modelElement);
            return new ClassConstantLocation(modelElement, declarationLocation);
        }

        private ClassConstantLocation(PhpElement modelElement, DeclarationLocation declaration) {
            super(modelElement, declaration);
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder(30);
            TypeConstantElement constant = (TypeConstantElement) getElement();
            final TypeElement type = constant.getType();
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

}
