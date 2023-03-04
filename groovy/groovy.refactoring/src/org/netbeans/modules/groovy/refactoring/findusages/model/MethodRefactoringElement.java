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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public class MethodRefactoringElement extends RefactoringElement {

    private ClassNode methodType;

    public MethodRefactoringElement(FileObject fileObject, ASTNode node) {
        this(fileObject, node, null);
    }

    public MethodRefactoringElement(FileObject fileObject, ASTNode node, ClassNode methodType) {
        super(fileObject, node);
        this.methodType = methodType;
    }


    @Override
    public String getName() {
        return ElementUtils.getNameWithoutPackage(node);
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof ConstructorNode || node instanceof ConstructorCallExpression) {
            return ElementKind.CONSTRUCTOR;
        }
        return ElementKind.METHOD;
    }

    @Override
    public String getShowcase() {
        if (node instanceof ConstructorNode) {
            return getConstructorSignature((ConstructorNode) node);
        } else  if (node instanceof MethodNode) {
            return getMethodSignature((MethodNode) node);
        } else if (node instanceof ConstructorCallExpression) {
            return getConstructorSignature((ConstructorCallExpression) node);
        } else if (node instanceof MethodCallExpression) {
            return getMethodSignature((MethodCallExpression) node);
        }
        return "Should not happened - MethodRefactoringElement: getShowcase():85"; // NOI18N
    }

    public String getMethodTypeName() {
        return methodType.getName();
    }

    public ClassNode getMethodType() {
        return methodType;
    }

    public List<String> getMethodParameters() {
        if (node instanceof MethodCallExpression) {
            return getParamTypes((MethodCallExpression) node);
        }
        if (node instanceof MethodNode) {
            return getParams((MethodNode) node);
        }
        assert false : "Weird class type found: " + node.getClass().getSimpleName();
        return Collections.emptyList();
    }

    private List<String> getParamTypes(MethodCallExpression methodCall) {
        final Expression arguments = methodCall.getArguments();
        final List<String> paramTypes = new ArrayList<String>();

        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression argumentList = ((ArgumentListExpression) arguments);
            if (argumentList.getExpressions().size() > 0) {
                for (Expression argument : argumentList.getExpressions()) {
                    paramTypes.add(ElementUtils.getTypeName(argument.getType()));
                }
            }
        }
        return paramTypes;
    }

    private List<String> getParams(MethodNode methodNode) {
        final List<String> paramTypes = new ArrayList<String>();

        for (Parameter param : methodNode.getParameters()) {
            paramTypes.add(ElementUtils.getTypeName(param));
        }
        return paramTypes;
    }

    public void setMethodType(ClassNode methodType) {
        this.methodType = methodType;
    }

    private String getMethodSignature(MethodNode method) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(method);
        builder.appendMethodParams(method);
        builder.appendReturnType(method);

        return builder.toString();
    }

    private String getConstructorSignature(ConstructorNode constructor) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(constructor);
        builder.appendMethodParams(constructor);

        return builder.toString();
    }

    private String getConstructorSignature(ConstructorCallExpression constructorCall) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(constructorCall);
        builder.appendMethodParams(constructorCall);

        return builder.toString();
    }

    private String getMethodSignature(MethodCallExpression methodCall) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(methodCall);
        builder.appendMethodParams(methodCall);
        builder.appendReturnType(methodCall);

        return builder.toString();
    }
}
