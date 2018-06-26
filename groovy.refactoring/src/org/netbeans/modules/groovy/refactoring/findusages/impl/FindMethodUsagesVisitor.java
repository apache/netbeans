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
