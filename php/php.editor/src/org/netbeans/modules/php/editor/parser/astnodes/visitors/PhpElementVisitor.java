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
package org.netbeans.modules.php.editor.parser.astnodes.visitors;

import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.FileElementQuery;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
public class PhpElementVisitor extends DefaultTreePathVisitor {

    private final FileElementQuery elementQuery;

    protected PhpElementVisitor(final PHPParseResult result) {
        elementQuery = FileElementQuery.getInstance(result);
    }

    public static ElementQuery.File createElementQuery(final PHPParseResult result) {
        final PhpElementVisitor instance = new PhpElementVisitor(result);
        instance.scan(instance.elementQuery.getResult().getProgram());
        return instance.toElementQuery();
    }

    @Override
    public void visit(ClassDeclaration node) {
        final NamespaceElement namespace = elementQuery.getLast(NamespaceElement.class);
        elementQuery.create(namespace, node);
        super.visit(node);
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        final NamespaceElement namespace = elementQuery.getLast(NamespaceElement.class);
        elementQuery.create(namespace, node);
        super.visit(node);
    }

    @Override
    public void visit(TraitDeclaration node) {
        final NamespaceElement namespace = elementQuery.getLast(NamespaceElement.class);
        elementQuery.create(namespace, node);
        super.visit(node);
    }

    @Override
    public void visit(EnumDeclaration node) {
        final NamespaceElement namespace = elementQuery.getLast(NamespaceElement.class);
        elementQuery.create(namespace, node);
        super.visit(node);
    }

    @Override
    public void visit(ConstantDeclaration node) {
        PhpElement last = elementQuery.getAnyLast(NamespaceElement.class, TypeElement.class);
        if (last instanceof TypeElement) {
            elementQuery.createTypeConstant((TypeElement) last, node);
        } else {
            elementQuery.createConstant((NamespaceElement) last, node);
        }
        super.visit(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        final NamespaceElement namespace = elementQuery.getLast(NamespaceElement.class);
        elementQuery.create(namespace, node);
        super.visit(node);
    }

    @Override
    public void visit(FieldsDeclaration node) {
        final TypeElement type = elementQuery.getLast(TypeElement.class);
        elementQuery.create(type, node);
        super.visit(node);
    }

    @Override
    public void visit(FieldAccess node) {
        final TypeElement type = elementQuery.getLast(TypeElement.class);
        elementQuery.create(type, node);
        super.visit(node);
    }

    @Override
    public void visit(MethodDeclaration node) {
        final TypeElement type = elementQuery.getLast(TypeElement.class);
        elementQuery.create(type, node);
        FunctionDeclaration function = node.getFunction();
        addToPath(node);
        scan(function.getFunctionName());
        scan(function.getFormalParameters());
        scan(function.getBody());
        removeFromPath();
    }

    @Override
    public void visit(NamespaceDeclaration node) {
        elementQuery.create(node);
        super.visit(node);
    }

    @Override
    public void visit(Variable node) {
        String extractVariableName = CodeUtils.extractVariableName(node);
        if (extractVariableName != null && !extractVariableName.startsWith("$")) { //NOI18N
            super.visit(node);
            return;
        }
        boolean isMethodDeclaration = false;
        boolean isFunctionDeclaration = false;
        boolean isTopLevelVariable = true;
        for (ASTNode scopeNode : getPath()) {
            if (scopeNode instanceof MethodDeclaration) {
                isMethodDeclaration = true;
                break;
            } else if (scopeNode instanceof FunctionDeclaration) {
                isFunctionDeclaration = true;
                break;
            } else if (scopeNode instanceof TypeDeclaration || scopeNode instanceof SingleFieldDeclaration
                    || scopeNode instanceof StaticFieldAccess || scopeNode instanceof FieldsDeclaration) {
                isTopLevelVariable = false;
                break;
            }
        }
        if (isMethodDeclaration) {
            MethodElement method = elementQuery.getLast(MethodElement.class);
            assert method != null;
            elementQuery.createMethodVariable(method, node);
        } else if (isFunctionDeclaration) {
            FunctionElement function = elementQuery.getLast(FunctionElement.class);
            assert function != null;
            elementQuery.createFunctionVariable(function, node);
        } else if (isTopLevelVariable) {
            elementQuery.createTopLevelVariable(node);
        }
        super.visit(node);
    }

    public final ElementQuery.File toElementQuery() {
        return elementQuery;
    }

    /**
     * @return the elementQuery
     */
    public FileElementQuery getElementQuery() {
        return elementQuery;
    }
}
