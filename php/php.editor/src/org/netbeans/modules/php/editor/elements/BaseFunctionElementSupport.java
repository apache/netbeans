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

package org.netbeans.modules.php.editor.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;

/**
 * @author Radek Matous
 */
public class BaseFunctionElementSupport  {
    private final Parameters parameters;
    private final ReturnTypes returnTypes;

    protected BaseFunctionElementSupport(Parameters parameters, ReturnTypes returnTypes) {
        assert parameters != null;
        assert returnTypes != null;
        this.parameters = parameters;
        this.returnTypes = returnTypes;
    }

    public final List<ParameterElement> getParameters() {
        return parameters.getParameters();
    }

    public final Collection<TypeResolver> getReturnTypes() {
        return returnTypes.getReturnTypes();
    }

    public final String getDeclaredReturnType() {
        return returnTypes.getDeclaredReturnType();
    }

    public final boolean isReturnUnionType() {
        return returnTypes.isUnionType();
    }

    public final boolean isReturnIntersectionType() {
        return returnTypes.isIntersectionType();
    }

    public final String asString(PrintAs as, BaseFunctionElement element, TypeNameResolver typeNameResolver) {
        return asString(as, element, typeNameResolver, null);
    }

    public final String asString(PrintAs as, BaseFunctionElement element, TypeNameResolver typeNameResolver, @NullAllowed PhpVersion phpVersion) {
        StringBuilder template = new StringBuilder();
        switch (as) {
            case NameAndParamsDeclaration:
                template.append(" ").append(element.getName()).append("("); //NOI18N
                template.append(parameters2String(element, getParameters(), OutputType.COMPLETE_DECLARATION, typeNameResolver, phpVersion));
                template.append(")"); //NOI18N
                break;
            case NameAndParamsInvocation:
                template.append(" ").append(element.getName()).append("("); //NOI18N
                template.append(parameters2String(element, getParameters(), OutputType.SIMPLE_NAME, typeNameResolver, phpVersion));
                template.append(")"); //NOI18N
                break;
            case DeclarationWithoutBody:
                final String modifiers = element.getPhpModifiers().toString();
                if (modifiers.length() > 0) {
                    template.append(modifiers).append(" "); //NOI18N
                }
                template.append("function"); //NOI18N
                template.append(asString(PrintAs.NameAndParamsDeclaration, element, typeNameResolver, phpVersion));
                if (phpVersion != null
                        && phpVersion.compareTo(PhpVersion.PHP_70) >= 0) {
                    Collection<TypeResolver> returns1 = getReturnTypes();
                    // we can also write the union type in phpdoc e.g. @return int|float
                    // check whether the union type is the actual declared return type to avoid adding the union type for phpdoc
                    if (returns1.size() == 1 || isReturnUnionType() || isReturnIntersectionType()) {
                        String returnType = asString(PrintAs.ReturnTypes, element, typeNameResolver, phpVersion);
                        if (StringUtils.hasText(returnType)) {
                            boolean isNullableType = CodeUtils.isNullableType(returnType);
                            if (isNullableType) {
                                returnType = returnType.substring(1);
                            }
                            template.append(": "); // NOI18N
                            if (isNullableType) {
                                template.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                            }
                            template.append(returnType);
                        }
                    }
                }
                break;
            case DeclarationWithEmptyBody:
                template.append(asString(PrintAs.DeclarationWithoutBody, element, typeNameResolver, phpVersion));
                template.append("{\n}"); //NOI18N
                break;
            case DeclarationWithParentCallInBody:
                template.append(asString(PrintAs.DeclarationWithoutBody, element, typeNameResolver, phpVersion));
                Collection<TypeResolver> returns2 = getReturnTypes();
                String methdodInvocation = asString(PrintAs.NameAndParamsInvocation, element, typeNameResolver, phpVersion);
                if (methdodInvocation.startsWith(" ")) {
                    methdodInvocation = methdodInvocation.substring(1);
                }
                if (returns2.size() > 0) {
                    template.append(String.format("{%nreturn parent::%s;%n}", methdodInvocation)); //NOI18N
                } else {
                    template.append(String.format("{%nparent::%s;%n}", methdodInvocation)); //NOI18N
                }
                break;
            case ReturnSemiTypes:
                for (TypeResolver typeResolver : getReturnTypes()) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeName = typeResolver.getTypeName(false);
                        if (typeName != null) {
                            appendSeparator(template);
                            template.append(typeNameResolver.resolve(typeName).toString());
                        }
                    } else {
                        String typeName = typeResolver.getRawTypeName();
                        if (typeName != null) {
                            appendSeparator(template);
                            template.append(typeName);
                        }
                    }
                }
                break;
            case ReturnTypes:
                boolean hasArray = false;
                String declaredReturnType = getDeclaredReturnType();
                if (StringUtils.hasText(declaredReturnType)) {
                    if (element instanceof MethodElement) {
                        MethodElement method = (MethodElement) element;
                        if (method.isMagic()) {
                            assert phpVersion != null;
                            template.append(MethodElementImpl.getValidType(declaredReturnType, phpVersion));
                            break;
                        }
                    }
                    String typeTemplate = Type.toTypeTemplate(declaredReturnType);
                    List<String> types = Arrays.asList(Type.splitTypes(declaredReturnType));
                    template.append(String.format(typeTemplate, (Object[]) resolveReturnTypes(types, typeNameResolver, element)));
                    break;
                }
                for (TypeResolver typeResolver : getReturnTypes()) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeName = typeResolver.getTypeName(false);
                        if (typeName != null) {
                            appendSeparator(template);
                            if (typeResolver.isNullableType()) {
                                template.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                            }
                            String returnType = typeNameResolver.resolve(typeName).toString();
                            returnType = resolveSpecialType(returnType, element, typeNameResolver);
                            // NETBEANS-5370: related to NETBEANS-4509
                            if (returnType.endsWith("[]")) { // NOI18N
                                returnType = Type.ARRAY;
                            }
                            if (returnType.equals(Type.ARRAY)) {
                                if (hasArray) {
                                    continue;
                                }
                                hasArray = true;
                            }
                            template.append(returnType);
                        }
                    }
                }
                break;
            default:
                assert false : as;
        }
        return template.toString();
    }

    private String[] resolveReturnTypes(List<String> types, TypeNameResolver typeNameResolver, BaseFunctionElement element) {
        List<String> replaced = new ArrayList<>(types.size());
        if (types.size() == getReturnTypes().size()) {
            for (TypeResolver typeResolver : getReturnTypes()) {
                if (typeResolver.isResolved()) {
                    QualifiedName typeName = typeResolver.getTypeName(false);
                    if (typeName != null) {
                        String returnType = typeNameResolver.resolve(typeName).toString();
                        // NETBEANS-5370: related to NETBEANS-4509
                        if (returnType.endsWith("[]")) { // NOI18N
                            returnType = Type.ARRAY;
                        }
                        returnType = resolveSpecialType(returnType, element, typeNameResolver);
                        replaced.add(returnType);
                    }
                }
            }
        } else {
            replaced.addAll(types);
        }
        return replaced.toArray(new String[0]);
    }

    private String resolveSpecialType(String returnType, BaseFunctionElement element, TypeNameResolver typeNameResolver) {
        String resolvedType = returnType;
        if (resolvedType.equals("\\" + Type.SELF) // NOI18N
                || resolvedType.equals("\\" + Type.PARENT)) { // NOI18N
            resolvedType = resolvedType.substring(1);
        }
        if ((Type.SELF).equals(resolvedType)
                && element instanceof TypeMemberElement) {
            // #267563
            resolvedType = typeNameResolver.resolve(((TypeMemberElement) element).getType().getFullyQualifiedName()).toString();
        } else if ((Type.PARENT).equals(resolvedType)
                && element instanceof TypeMemberElement) {
            TypeElement typeElement = ((TypeMemberElement) element).getType();
            if (typeElement instanceof ClassElement) {
                QualifiedName superClassName = ((ClassElement) typeElement).getSuperClassName();
                if (superClassName != null) {
                    resolvedType = typeNameResolver.resolve(superClassName).toString();
                } else {
                    resolvedType = Type.PARENT;
                }
            }
        }
        return resolvedType;
    }

    private void appendSeparator(StringBuilder template) {
        if (template.length() == 0) {
            return;
        }
        if (isReturnIntersectionType()) {
            template.append(Type.SEPARATOR_INTERSECTION);
        } else {
            template.append(Type.SEPARATOR);
        }
    }

    private static String parameters2String(final BaseFunctionElement element, final List<ParameterElement> parameterList, OutputType stringOutputType, TypeNameResolver typeNameResolver, PhpVersion phpVersion) {
        StringBuilder template = new StringBuilder();
        if (parameterList.size() > 0) {
            for (int i = 0, n = parameterList.size(); i < n; i++) {
                StringBuilder paramSb = new StringBuilder();
                if (i > 0) {
                    paramSb.append(", "); //NOI18N
                }
                final ParameterElement param = parameterList.get(i);
                String paramInfo = param.asString(stringOutputType, typeNameResolver, phpVersion);
                boolean isNullableType = CodeUtils.isNullableType(paramInfo);
                if (isNullableType) {
                    paramInfo = paramInfo.substring(1);
                }
                paramInfo = resolveSpecialTypes(paramInfo, element, typeNameResolver, param);
                if (isNullableType) {
                    paramSb.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                }
                paramSb.append(paramInfo);
                template.append(paramSb);
            }
        }
        return template.toString();
    }

    private static String resolveSpecialTypes(String paramInfo, final BaseFunctionElement element, TypeNameResolver typeNameResolver, final ParameterElement param) {
        String parameterInfo = paramInfo;
        if (parameterInfo.startsWith(Type.SELF + " ") // NOI18N
                && element instanceof TypeMemberElement) {
            // #267563
            parameterInfo = typeNameResolver.resolve(((TypeMemberElement) element).getType().getFullyQualifiedName()).toString() + parameterInfo.substring(Type.SELF.length());
        }
        if (parameterInfo.startsWith(Type.PARENT + " ") // NOI18N
                && element instanceof TypeMemberElement) {
            TypeElement typeElement = ((TypeMemberElement) element).getType();
            if (typeElement instanceof ClassElement) {
                QualifiedName superClassName = ((ClassElement) typeElement).getSuperClassName();
                if (superClassName != null) {
                    parameterInfo = typeNameResolver.resolve(superClassName).toString() + parameterInfo.substring(Type.PARENT.length());
                }
            }
        }
        if (param.isUnionType()
                && element instanceof TypeMemberElement) {
            parameterInfo = resolveSpecialTypesInUnionType(parameterInfo, element, typeNameResolver);
        }
        return parameterInfo;
    }

    private static String resolveSpecialTypesInUnionType(String paramInfo, final BaseFunctionElement element, TypeNameResolver typeNameResolver) {
        // NETBEANS-4443 PHP 8.0: Union Types 2.0
        String parameterInfo = paramInfo;
        // e.g. int|float|Foo|null $param
        int indexOfWhitespace = parameterInfo.indexOf(' ');
        if (indexOfWhitespace == -1) {
            // no types e.g. $param
            return parameterInfo;
        }
        String unionType = parameterInfo.substring(0, indexOfWhitespace);
        List<String> unionTypeList = StringUtils.explode(unionType, Type.SEPARATOR);
        StringBuilder sb = new StringBuilder();
        if (unionTypeList.contains(Type.SELF) || unionTypeList.contains(Type.PARENT)) {
            for (String type : unionTypeList) {
                if (sb.length() > 0) {
                    sb.append(Type.SEPARATOR);
                }
                if (Type.SELF.equals(type)) {
                    sb.append(typeNameResolver.resolve(((TypeMemberElement) element).getType().getFullyQualifiedName()).toString());
                } else if (Type.PARENT.equals(type)) {
                    TypeElement typeElement = ((TypeMemberElement) element).getType();
                    if (typeElement instanceof ClassElement) {
                        QualifiedName superClassName = ((ClassElement) typeElement).getSuperClassName();
                        if (superClassName != null) {
                            sb.append(typeNameResolver.resolve(superClassName).toString());
                        } else {
                            sb.append(type);
                        }
                    }
                } else {
                    sb.append(type);
                }
            }
            parameterInfo = sb.toString() + parameterInfo.substring(indexOfWhitespace);
        }
        return parameterInfo;
    }

    public interface Parameters {
        List<ParameterElement> getParameters();
    }

    public static final class ParametersImpl implements Parameters {
        private final List<ParameterElement> parameters;

        public static Parameters create(List<ParameterElement> parameters) {
            return new ParametersImpl(parameters);
        }

        private ParametersImpl(List<ParameterElement> parameters) {
            this.parameters = parameters;
        }

        @Override
        public List<ParameterElement> getParameters() {
            return parameters;
        }

    }

    public interface ReturnTypes {
        ReturnTypes NONE = new ReturnTypes() {

            @Override
            public Set<TypeResolver> getReturnTypes() {
                return Collections.<TypeResolver>emptySet();
            }

            @Override
            public boolean isUnionType() {
                return false;
            }

            @Override
            public boolean isIntersectionType() {
                return false;
            }

            @Override
            public String getDeclaredReturnType() {
                return null;
            }
        };

        Set<TypeResolver> getReturnTypes();
        boolean isUnionType();
        boolean isIntersectionType();
        @CheckForNull
        String getDeclaredReturnType();
    }

    public static final class ReturnTypesImpl implements ReturnTypes {

        private final Set<TypeResolver> returnTypes;
        private final boolean isUnionType;
        private final boolean isIntersectionType;
        @NullAllowed
        private final String declaredReturnType;

        public static ReturnTypes create(Set<TypeResolver> returnTypes, Expression node) {
            return new ReturnTypesImpl(returnTypes, node);
        }

        public static ReturnTypes create(@NullAllowed String declaredType) {
            if (StringUtils.isEmpty(declaredType)) {
                return ReturnTypes.NONE;
            }
            Set<TypeResolver> returnTypes = TypeResolverImpl.parseTypes(declaredType);
            return new ReturnTypesImpl(returnTypes, declaredType);
        }

        private ReturnTypesImpl(Set<TypeResolver> returnTypes, Expression node) {
            this.returnTypes = returnTypes;
            this.isUnionType = node instanceof UnionType;
            this.isIntersectionType = node instanceof IntersectionType;
            this.declaredReturnType = CodeUtils.extractQualifiedName(node);
        }

        private ReturnTypesImpl(Set<TypeResolver> returnTypes, String declaredType) {
            this.returnTypes = returnTypes;
            this.isUnionType = Type.isUnionType(declaredType);
            this.isIntersectionType = Type.isIntersectionType(declaredType);
            this.declaredReturnType = declaredType;
        }

        @Override
        public Set<TypeResolver> getReturnTypes() {
            return Collections.unmodifiableSet(returnTypes);
        }

        @Override
        public String getDeclaredReturnType() {
            return declaredReturnType;
        }

        @Override
        public boolean isUnionType() {
            return isUnionType;
        }

        @Override
        public boolean isIntersectionType() {
            return isIntersectionType;
        }

    }

}
