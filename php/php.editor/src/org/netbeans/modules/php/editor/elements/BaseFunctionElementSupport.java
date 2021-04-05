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
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;

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
                    if (returns1.size() == 1) {
                        String returnType = asString(PrintAs.ReturnTypes, element, typeNameResolver, phpVersion);
                        if (StringUtils.hasText(returnType)) {
                            boolean isNullableType = CodeUtils.isNullableType(returnType);
                            if (isNullableType) {
                                returnType = returnType.substring(1);
                            }
                            if ("\\self".equals(returnType) // NOI18N
                                    && element instanceof TypeMemberElement) {
                                // #267563
                                returnType = ((TypeMemberElement) element).getType().getFullyQualifiedName().toString();
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
                                template.append("|"); //NOI18N
                            }
                            template.append(typeNameResolver.resolve(typeName).toString());
                        }
                    } else {
                        String typeName = typeResolver.getRawTypeName();
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append("|"); //NOI18N
                            }
                            template.append(typeName);
                        }
                    }
                }
                break;
            case ReturnTypes:
                for (TypeResolver typeResolver : getReturnTypes()) {
                    if (typeResolver.isResolved()) {
                        QualifiedName typeName = typeResolver.getTypeName(false);
                        if (typeName != null) {
                            if (template.length() > 0) {
                                template.append("|"); //NOI18N
                            }
                            if (typeResolver.isNullableType()) {
                                template.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                            }
                            template.append(typeNameResolver.resolve(typeName).toString());
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
                if (paramInfo.startsWith("self ") // NOI18N
                        && element instanceof TypeMemberElement) {
                    // #267563
                    paramInfo = ((TypeMemberElement) element).getType().getFullyQualifiedName().toString() + paramInfo.substring(4);
                }
                if (isNullableType) {
                    paramSb.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                }
                paramSb.append(paramInfo);
                template.append(paramSb);
            }
        }
        return template.toString();
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
        };

        Set<TypeResolver> getReturnTypes();
    }

    public static final class ReturnTypesImpl implements ReturnTypes {
        private final Set<TypeResolver> returnTypes;

        public static ReturnTypes create(Set<TypeResolver> returnTypes) {
            return new ReturnTypesImpl(returnTypes);
        }

        private ReturnTypesImpl(Set<TypeResolver> returnTypes) {
            this.returnTypes = returnTypes;
        }

        @Override
        public Set<TypeResolver> getReturnTypes() {
            return Collections.unmodifiableSet(returnTypes);
        }

    }

}
