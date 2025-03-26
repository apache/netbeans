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
package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import static org.netbeans.modules.php.editor.PredefinedSymbols.Attributes.OVERRIDE;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import static org.netbeans.modules.php.editor.codegen.CGSGenerator.NEW_LINE;
import org.netbeans.modules.php.editor.elements.ElementUtils;
import org.netbeans.modules.php.editor.model.impl.Type;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface SinglePropertyMethodCreator<T extends Property> {

    String create(T property);

    //~ Inner classes

    final class InheritedMethodCreator implements SinglePropertyMethodCreator<MethodProperty> {

        private final CGSInfo cgsInfo;


        public InheritedMethodCreator(CGSInfo cgsInfo) {
            assert cgsInfo != null;
            this.cgsInfo = cgsInfo;
        }

        @Override
        public String create(MethodProperty property) {
            final StringBuilder inheritedMethod = new StringBuilder();
            final MethodElement method = property.getMethod();
            inheritedMethod.append(getOverrideAttribute(method)); // PHP 8.3
            Collection<TypeResolver> returnTypes = method.getReturnTypes();
            if (method.isAbstract() || method.isMagic() || method.getType().isInterface() || method.getType().isTrait()
                    || ElementUtils.isVoidOrNeverType(returnTypes)) {
                inheritedMethod.append(method.asString(
                        BaseFunctionElement.PrintAs.DeclarationWithEmptyBody,
                        cgsInfo.createTypeNameResolver(method),
                        cgsInfo.getPhpVersion()).replace("abstract ", CodeUtils.EMPTY_STRING)); //NOI18N;
                if (ElementUtils.isToStringMagicMethod(method)) {
                    // GH-6783
                    inheritedMethod.deleteCharAt(inheritedMethod.length() - 1); // delete "}"
                    inheritedMethod.append(ElementUtils.getToStringMagicMethodBody(method.getType(), cgsInfo.getIndex()));
                    inheritedMethod.append("\n}"); // NOI18N
                }
            } else {
                inheritedMethod.append(method.asString(
                        BaseFunctionElement.PrintAs.DeclarationWithParentCallInBody,
                        cgsInfo.createTypeNameResolver(method),
                        cgsInfo.getPhpVersion()).replace("abstract ", CodeUtils.EMPTY_STRING)); //NOI18N;
            }
            inheritedMethod.append(NEW_LINE);
            return inheritedMethod.toString();
        }

        private String getOverrideAttribute(MethodElement method) {
            if (!method.isMagic()
                    && (!method.getType().isTrait() || ElementUtils.isAbstractTraitMethod(method))
                    && cgsInfo.getPhpVersion().hasOverrideAttribute()) {
                return OVERRIDE.asAttributeExpression() + CodeUtils.NEW_LINE;
            }
            return CodeUtils.EMPTY_STRING;
        }
    }

    abstract class SinglePropertyMethodCreatorImpl implements SinglePropertyMethodCreator<Property> {
        protected static final String TEMPLATE_NAME = "${TEMPLATE_NAME}"; //NOI18N
        protected static final String FUNCTION_MODIFIER = "${FUNCTION_MODIFIER}"; //NOI18N
        protected final CGSInfo cgsInfo;

        public SinglePropertyMethodCreatorImpl(CGSInfo cgsInfo) {
            assert cgsInfo != null;
            this.cgsInfo = cgsInfo;
        }

        @Override
        public abstract String create(Property property);

        protected String getMethodName(Property property) {
            String changedName = cgsInfo.getHowToGenerate() == CGSGenerator.GenWay.WITHOUT_UNDERSCORE
                    ? CodegenUtils.upFirstLetterWithoutUnderscore(property.getName())
                    : CodegenUtils.upFirstLetter(property.getName());
            return CodegenUtils.getUnusedMethodName(new ArrayList<>(), changedName);
        }

        protected String getAccessModifier() {
            return cgsInfo.isPublicModifier() ? "public " : ""; //NOI18N
        }

    }

    final class SingleGetterCreator extends SinglePropertyMethodCreatorImpl {
        private static final String RETURN_TYPE = "${returnType}"; // NOI18N
        private static final String GETTER_TEMPLATE
            = CGSGenerator.ACCESS_MODIFIER + FUNCTION_MODIFIER + " function " + TEMPLATE_NAME + "()" + RETURN_TYPE + " {"
            + CGSGenerator.NEW_LINE + "return " + CGSGenerator.ACCESSOR + CGSGenerator.PROPERTY + ";" + CGSGenerator.NEW_LINE + "}" + CGSGenerator.NEW_LINE;    //NOI18N

        public SingleGetterCreator(CGSInfo cgsInfo) {
            super(cgsInfo);
        }

        @Override
        public String create(Property property) {
            StringBuilder getter = new StringBuilder();
            String methodName = getMethodName(property);
            String type = ""; // NOI18N
            if (cgsInfo.getPhpVersion().compareTo(PhpVersion.PHP_70) >= 0) {
                type = property.getType();
            }
            getter.append(
                    GETTER_TEMPLATE.replace(TEMPLATE_NAME, cgsInfo.getHowToGenerate().getGetterTemplate())
                    .replace(CGSGenerator.ACCESS_MODIFIER, getAccessModifier())
                    .replace(FUNCTION_MODIFIER, property.getFunctionModifier())
                    .replace(CGSGenerator.UNDERSCORED_METHOD_NAME, property.getName())
                    .replace(CGSGenerator.ACCESSOR, property.getAccessor())
                    .replace(CGSGenerator.PROPERTY, property.getAccessedName())
                    .replace(CGSGenerator.UP_FIRST_LETTER_PROPERTY, methodName)
                    .replace(CGSGenerator.UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName)
                    .replace(RETURN_TYPE, type.isEmpty() ? "" : ": " + property.getTypeForTemplate())); // NOI18N
            getter.append(CGSGenerator.NEW_LINE);
            return getter.toString();
        }

    }

    final class SingleSetterCreator extends SinglePropertyMethodCreatorImpl {
        private static final String PARAM_TYPE = "${PARAM_TYPE}"; //NOI18N
        private static final String FLUENT_SETTER = "${FluentSetter}"; //NOI18N
        private static final String RETURN_TYPE = "${ReturnType}"; // NOI18N
        private static final String SETTER_TEMPLATE
            = CGSGenerator.ACCESS_MODIFIER + FUNCTION_MODIFIER + " function " + TEMPLATE_NAME + "(" + PARAM_TYPE + "$$" + CGSGenerator.PARAM_NAME + ")" + RETURN_TYPE + "{"
            + CGSGenerator.ASSIGNMENT_TEMPLATE + CGSGenerator.NEW_LINE + FLUENT_SETTER + "}" + CGSGenerator.NEW_LINE; //NOI18N

        private final FluentSetterReturnPartCreator fluentSetterCreator;

        public SingleSetterCreator(CGSInfo cgsInfo) {
            super(cgsInfo);
            this.fluentSetterCreator = new FluentSetterReturnPartCreator(cgsInfo.isFluentSetter());
        }

        @Override
        public String create(Property property) {
            StringBuilder setter = new StringBuilder();
            String name = property.getName();
            String paramName = cgsInfo.getHowToGenerate() == CGSGenerator.GenWay.WITHOUT_UNDERSCORE ? CodegenUtils.withoutUnderscore(name) : name;
            String type = property.getType();
            String methodName = getMethodName(property);
            setter.append(
                    SETTER_TEMPLATE.replace(TEMPLATE_NAME, cgsInfo.getHowToGenerate().getSetterTemplate())
                    .replace(CGSGenerator.ACCESS_MODIFIER, getAccessModifier())
                    .replace(FUNCTION_MODIFIER, property.getFunctionModifier())
                    .replace(CGSGenerator.UNDERSCORED_METHOD_NAME, name)
                    .replace(CGSGenerator.ACCESSOR, property.getAccessor())
                    .replace(CGSGenerator.PROPERTY, property.getAccessedName())
                    .replace(FLUENT_SETTER, fluentSetterCreator.create(property))
                    .replace(RETURN_TYPE, getReturnType())
                    .replace(CGSGenerator.PARAM_NAME, paramName)
                    .replace(CGSGenerator.UP_FIRST_LETTER_PROPERTY, methodName)
                    .replace(CGSGenerator.UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName)
                    .replace(PARAM_TYPE, type.isEmpty() ? type : property.getTypeForTemplate()));
            setter.append(CGSGenerator.NEW_LINE);
            return setter.toString();
        }

        private String getReturnType() {
            if (!cgsInfo.isFluentSetter() && cgsInfo.getPhpVersion().hasVoidReturnType()) {
                return String.format(": %s ", Type.VOID); // NOI18N
            }
            return " "; // NOI18N
        }

        private static final class FluentSetterReturnPartCreator {
            private final boolean isStatic;

            FluentSetterReturnPartCreator(boolean isStatic) {
                this.isStatic = isStatic;
            }

            public String create(Property property) {
                assert property != null;
                return isStatic ? "return " + property.getFluentReturnAccessor() + ";" + CGSGenerator.NEW_LINE : ""; //NOI18N
            }

        }

    }

}
