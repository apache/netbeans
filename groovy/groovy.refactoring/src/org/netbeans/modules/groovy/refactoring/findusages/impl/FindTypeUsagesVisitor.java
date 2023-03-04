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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.ElementUtils;

/**
 * Visitor for collecting all usages for a given
 * <code>ModuleNode</code> and fully qualified name of the finding class.
 *
 * @author Martin Janicek
 */
public class FindTypeUsagesVisitor extends AbstractFindUsagesVisitor {

    private final String findingFqn;

    public FindTypeUsagesVisitor(ModuleNode moduleNode, String findingFqn) {
        super(moduleNode);
        this.findingFqn = ElementUtils.normalizeTypeName(findingFqn, null);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        addIfEquals(expression);
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        addIfEquals(expression);
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        addIfEquals(expression);
        super.visitClassExpression(expression);
    }

    @Override
    public void visitField(FieldNode field) {
        addIfEquals(field);
        super.visitField(field);
    }

    @Override
    public void visitProperty(PropertyNode property) {
        if (property.isSynthetic()) {
            addIfEquals(property);
        }
        super.visitProperty(property);
    }

    @Override
    public void visitImports(ModuleNode node) {
        for (ImportNode importNode : node.getImports()) {
            final ClassNode importType = importNode.getType();
            if (!importNode.isStar()) {
                // ImportNode itself doesn't contain line/column information, so we need set them manually
                importNode.setLineNumber(importType.getLineNumber());
                importNode.setColumnNumber(importType.getColumnNumber());
                importNode.setLastLineNumber(importType.getLastLineNumber());
                importNode.setLastColumnNumber(importType.getLastColumnNumber());

                if (isEquals(importNode)) {
                    usages.add(new ASTUtils.FakeASTNode(importNode, importNode.getClassName()));
                }
            }
        }
        super.visitImports(node);
    }

    @Override
    public void visitClass(ClassNode clazz) {
        if (isEquals(clazz.getSuperClass())) {
            // Oh my goodness I have absolutely no idea why the hack getSuperClass() doesn't return valid initiated superclass
            // and the method with a weird name getUnresolvedSuperClass(false) is actually returning resolved super class (with
            // line/column numbers set)
            usages.add(new ASTUtils.FakeASTNode(clazz.getUnresolvedSuperClass(false), clazz.getSuperClass().getNameWithoutPackage()));
        }
        for (ClassNode interfaceNode : clazz.getInterfaces()) {
            addIfEquals(interfaceNode);
        }
        super.visitClass(clazz);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode method, boolean isConstructor) {
        if (!isConstructor && isEquals(method.getReturnType())) {
            addIfEquals(method);
        }

        for (Parameter param : method.getParameters()) {
            addIfEquals(param);
        }
        super.visitConstructorOrMethod(method, isConstructor);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        addIfEquals(call);
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        addIfEquals(forLoop);
        super.visitForLoop(forLoop);
    }

    private void addIfEquals(ASTNode node) {
        final ClassNode type = ElementUtils.getType(node);
        if (isEquals(node)) {
            if (type.getLineNumber() > 0 && type.getColumnNumber() > 0) {
                usages.add(new ASTUtils.FakeASTNode(type, ElementUtils.getTypeName(node)));
            } else if (node.getLineNumber() > 0 && node.getColumnNumber() > 0) {
                usages.add(new ASTUtils.FakeASTNode(node, ElementUtils.getTypeName(node)));
            }
        }

        final GenericsType[] genericTypes = type.getGenericsTypes();
        if (genericTypes != null && genericTypes.length > 0) {
            for (GenericsType genericType : genericTypes) {
                addIfEquals(genericType.getType());
            }
        }
    }

    private boolean isEquals(ASTNode node) {
        if (findingFqn.equals(ElementUtils.getTypeName(node))) {
            return true;
        }
        return false;
    }
}