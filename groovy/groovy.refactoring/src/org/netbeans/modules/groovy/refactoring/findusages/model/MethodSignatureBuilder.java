/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
