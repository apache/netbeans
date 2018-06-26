/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;

import static org.netbeans.modules.php.editor.codegen.CGSGenerator.NEW_LINE;

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
            if (method.isAbstract() || method.isMagic() || method.getType().isInterface()) {
                inheritedMethod.append(method.asString(
                        BaseFunctionElement.PrintAs.DeclarationWithEmptyBody,
                        cgsInfo.createTypeNameResolver(method),
                        cgsInfo.getPhpVersion()).replace("abstract ", "")); //NOI18N;
            } else {
                inheritedMethod.append(method.asString(
                        BaseFunctionElement.PrintAs.DeclarationWithParentCallInBody,
                        cgsInfo.createTypeNameResolver(method),
                        cgsInfo.getPhpVersion()).replace("abstract ", "")); //NOI18N;
            }
            inheritedMethod.append(NEW_LINE);
            return inheritedMethod.toString();
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
            return CodegenUtils.getUnusedMethodName(new ArrayList<String>(), changedName);
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
        private static final String SETTER_TEMPLATE
            = CGSGenerator.ACCESS_MODIFIER + FUNCTION_MODIFIER + " function " + TEMPLATE_NAME + "(" + PARAM_TYPE + "$$" + CGSGenerator.PARAM_NAME + ") {"
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
                    .replace(CGSGenerator.PARAM_NAME, paramName)
                    .replace(CGSGenerator.UP_FIRST_LETTER_PROPERTY, methodName)
                    .replace(CGSGenerator.UP_FIRST_LETTER_PROPERTY_WITHOUT_UNDERSCORE, methodName)
                    .replace(PARAM_TYPE, type.isEmpty() ? type : property.getTypeForTemplate()));
            setter.append(CGSGenerator.NEW_LINE);
            return setter.toString();
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
