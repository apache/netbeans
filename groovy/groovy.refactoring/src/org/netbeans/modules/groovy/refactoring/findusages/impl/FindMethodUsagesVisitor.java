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
package org.netbeans.modules.groovy.refactoring.findusages.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.Methods;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;
import org.netbeans.modules.groovy.refactoring.findusages.model.MethodRefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;

/**
 *
 * @author Martin Janicek
 */
public class FindMethodUsagesVisitor extends AbstractFindUsagesVisitor {

    private final MethodRefactoringElement element;
    private final String methodType;
    private final String methodName;
    private final List<String> methodParams;
    private final GroovyIndex index;
    private IndexedMethod method;


    public FindMethodUsagesVisitor(ModuleNode moduleNode, RefactoringElement refactoringElement) {
        super(moduleNode);
        assert (refactoringElement instanceof MethodRefactoringElement) : "It was: " + refactoringElement.getClass().getSimpleName();
        this.element = (MethodRefactoringElement) refactoringElement;
        this.methodType = element.getMethodTypeName();
        this.methodName = element.getName();
        this.methodParams = element.getMethodParameters();

        ClasspathInfo cpInfo = GroovyProjectUtil.getClasspathInfoFor(element.getFileObject());
        ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);

        this.index = GroovyIndex.get(Arrays.asList(cp.getRoots()));

        // Find all methods with the same name and the same method type
        final Set<IndexedMethod> possibleMethods = index.getMethods(methodName, methodType, QuerySupport.Kind.EXACT);
        for (IndexedMethod indexedMethod : possibleMethods) {
            final boolean sameName = methodName.equals(indexedMethod.getName());
            final boolean sameParameters = Methods.isSameList(methodParams, indexedMethod.getParameterTypes());

            // Find the exact method we are looking for
            if (sameName && sameParameters) {
                this.method = indexedMethod;
            }
        }
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        final String className = ElementUtils.getDeclaringClassName(node);

        if (!node.hasNoRealSourcePosition() && methodType.equals(className)) {
            if (isConstructor && methodType.endsWith(methodName)) {
                usages.add(node);
            } else if (methodName.equals(node.getName()) && Methods.hasSameParameters(method, node)) {
                usages.add(node);
            }
        }
        super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression constructorCall) {
        final String typeName = constructorCall.getType().getNameWithoutPackage();

        if (methodName.equals(typeName)) {
            usages.add(constructorCall);
        }

        super.visitConstructorCallExpression(constructorCall);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {
        Expression expression = methodCall.getObjectExpression();

        if (expression instanceof VariableExpression) {
            VariableExpression variableExpression = ((VariableExpression) expression);
            Variable variable = variableExpression.getAccessedVariable();

            if (variable != null) {
                if (variable.isDynamicTyped()) {
                    addDynamicVarUsages(methodCall, variable);
                } else {
                    addStaticVarUsages(methodCall, variable);
                }
            } else {
                addThisUsages(methodCall);
            }

        } else if (expression instanceof ConstructorCallExpression) {
            addConstructorUsages(methodCall, (ConstructorCallExpression) expression);
        }
        super.visitMethodCallExpression(methodCall);
    }

    private void addDynamicVarUsages(MethodCallExpression methodCall, Variable variable) {
        // In method call on the dynamic type we want to add all references no matter on the runtime type
        findAndAdd(variable.getType(), methodCall);
    }

    private void addStaticVarUsages(MethodCallExpression methodCall, Variable variable) {
        if (methodType.equals(variable.getType().getName())) {
            findAndAdd(variable.getType(), methodCall);
        }
    }

    private void addThisUsages(MethodCallExpression methodCall) {
        final List<ClassNode> classes = moduleNode.getClasses(); //classes declared in current file
        for (ClassNode classNode : classes) {
            if (methodType.equals(classNode.getName())) {
                findAndAdd(element.getMethodType(), methodCall);
                break;
            }
        }
    }

    private void addConstructorUsages(MethodCallExpression methodCall, ConstructorCallExpression constructorCallExpression) {
        final ClassNode type = constructorCallExpression.getType();
        if (methodType.equals(ElementUtils.getDeclaringClassName(type))) {
            findAndAdd(type, methodCall);
        }
    }

    /**
     * Tries to find exact MethodNode in the given ClassNode. The MethodNode is
     * based on MethodCallExpresion. If there is an exact match, the methodCall
     * is added into the usages list.
     *
     * @param type
     * @param methodCall
     */
    private void findAndAdd(ClassNode type, MethodCallExpression methodCall) {
        if (method == null || methodCall == null) {
            return;
        }
        if (methodName.equals(methodCall.getMethodAsString()) && Methods.hasSameParameters(method, methodCall)) {
            usages.add(methodCall.getMethod());
        }
    }
}
