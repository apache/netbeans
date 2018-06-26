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