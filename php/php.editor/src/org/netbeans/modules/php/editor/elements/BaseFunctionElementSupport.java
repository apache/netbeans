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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.impl.Type;

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

    public final boolean isReturnUnionType() {
        return returnTypes.isUnionType();
    }

    public final String asString(PrintAs as, BaseFunctionElement element, TypeNameResolver typeNameResolver) {
        return asString(as, element, typeNameResolver, null);
    }

    public final String asString(PrintAs as, BaseFunctionElement element, TypeNameResolver typeNameResolver, @NullAllowed PhpVersion phpVersion) {
        StringBuilder template = new StringBuilder();
        switch (as) {
            case NameAndParamsDeclaration:
                template.append(" ").append(element.getName()).append("("); //NOI18N
                template.append(parameters2String(element, getParameters(), OutputType.COMPLETE_DECLARATION, typeNameResolver));
                template.append(")"); //NOI18N
                break;
            case NameAndParamsInvocation:
                template.append(" ").append(element.getName()).append("("); //NOI18N
                template.append(parameters2String(element, getParameters(), OutputType.SIMPLE_NAME, typeNameResolver));
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
                    if (returns1.size() == 1 || isReturnUnionType()) {
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
                            if (template.length() > 0) {
                                template.append(Type.SEPARATOR);
                            }
                            template.append(typeNameResolver.resolve(typeName).toString());
                        }
                    } else {
                        String typeName = typeResolver.getRawTypeName();
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append(Type.SEPARATOR);
                            }
                            template.append(typeName);
                        }
                    }
                }
                break;
            case ReturnTypes:
                boolean hasArray = false;
                for (TypeResolver typeResolver : getReturnTypes()) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeName = typeResolver.getTypeName(false);
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append(Type.SEPARATOR);
                            }
                            if (typeResolver.isNullableType()) {
                                template.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                            }
                            String returnType = typeNameResolver.resolve(typeName).toString();
                            if (("\\" + Type.SELF).equals(returnType) // NOI18N
                                    && element instanceof TypeMemberElement) {
                                // #267563
                                returnType = typeNameResolver.resolve(((TypeMemberElement) element).getType().getFullyQualifiedName()).toString();
                            }
                            if (("\\" + Type.PARENT).equals(returnType) // NOI18N
                                    && element instanceof TypeMemberElement) {
                                TypeElement typeElement = ((TypeMemberElement) element).getType();
                                if (typeElement instanceof ClassElement) {
                                    QualifiedName superClassName = ((ClassElement) typeElement).getSuperClassName();
                                    if (superClassName != null) {
                                        returnType = typeNameResolver.resolve(superClassName).toString();
                                    } else {
                                        returnType = Type.PARENT;
                                    }
                                }
                            }
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

    private static String parameters2String(final BaseFunctionElement element, final List<ParameterElement> parameterList, OutputType stringOutputType, TypeNameResolver typeNameResolver) {
        StringBuilder template = new StringBuilder();
        if (parameterList.size() > 0) {
            for (int i = 0, n = parameterList.size(); i < n; i++) {
                StringBuilder paramSb = new StringBuilder();
                if (i > 0) {
                    paramSb.append(", "); //NOI18N
                }
                final ParameterElement param = parameterList.get(i);
                String paramInfo = param.asString(stringOutputType, typeNameResolver);
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
        };

        Set<TypeResolver> getReturnTypes();
        boolean isUnionType();
    }

    public static final class ReturnTypesImpl implements ReturnTypes {

        private final Set<TypeResolver> returnTypes;
        private final boolean isUnionType;

        public static ReturnTypes create(Set<TypeResolver> returnTypes, boolean isUnionType) {
            return new ReturnTypesImpl(returnTypes, isUnionType);
        }

        private ReturnTypesImpl(Set<TypeResolver> returnTypes, boolean isUnionType) {
            this.returnTypes = returnTypes;
            this.isUnionType = isUnionType;
        }

        @Override
        public Set<TypeResolver> getReturnTypes() {
            return Collections.unmodifiableSet(returnTypes);
        }

        @Override
        public boolean isUnionType() {
            return isUnionType;
        }

    }

}
