/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
