/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.findusages.model;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.netbeans.modules.groovy.editor.api.ElementUtils;

/**
 *
 * @author Martin Janicek
 */
public class MethodSignatureBuilder {

    private StringBuilder builder;

    public MethodSignatureBuilder() {
        builder = new StringBuilder();
    }



    public MethodSignatureBuilder appendMethodName(MethodNode method) {
        if (isConstructor(method)) {
            builder.append(method.getDeclaringClass().getNameWithoutPackage());
        } else {
            builder.append(method.getName());
        }
        return this;
    }

    private boolean isConstructor(MethodNode method) {
        if ("<init>".equals(method.getName())) { // NOI18N
            return true;
        }
        return false;
    }

    public MethodSignatureBuilder appendMethodName(MethodCallExpression methodCall) {
        builder.append(methodCall.getMethodAsString());
        return this;
    }

    public MethodSignatureBuilder appendMethodName(ConstructorCallExpression constructorCall) {
        builder.append(constructorCall.getType().getNameWithoutPackage());
        return this;
    }

    public MethodSignatureBuilder appendMethodParams(MethodNode method) {
        Parameter[] params = method.getParameters();

        builder.append("("); // NOI18N
        if (params.length > 0) {
            for (Parameter param : params) {
                builder.append(ElementUtils.getType(param.getType()).getNameWithoutPackage());
                builder.append(" "); // NOI18N
                builder.append(param.getName());
                builder.append(","); // NOI18N
            }
            builder.setLength(builder.length() - 1);
        }
        builder.append(")"); // NOI18N
        return this;
    }

    public MethodSignatureBuilder appendMethodParams(Expression arguments) {
        builder.append("("); // NOI18N

        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression argumentList = ((ArgumentListExpression) arguments);
            if (argumentList.getExpressions().size() > 0) {
                for (Expression argument : argumentList.getExpressions()) {
                    builder.append(ElementUtils.getTypeNameWithoutPackage(argument.getType()));
                    builder.append(" "); // NOI18N
                    builder.append(argument.getText());
                    builder.append(","); // NOI18N
                }
                builder.setLength(builder.length() - 1);
            }
        }
        builder.append(")"); // NOI18N
        return this;
    }

    public MethodSignatureBuilder appendReturnType(MethodNode method) {
        builder.append(" : "); // NOI18N
        builder.append(method.getReturnType().getNameWithoutPackage());
        return this;
    }

    public MethodSignatureBuilder appendReturnType(MethodCallExpression methodCall) {
        builder.append(" : "); // NOI18N

        final MethodNode methodTarget = methodCall.getMethodTarget();
        if (methodTarget != null && methodTarget.getReturnType() != null) {
            builder.append(methodTarget.getReturnType().getNameWithoutPackage());
        } else {
            // We don't know exact return type - just show an Object for now
            builder.append("Object"); // NOI18N
        }
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
