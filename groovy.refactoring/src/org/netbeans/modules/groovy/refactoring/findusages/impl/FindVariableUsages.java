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

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.VariableRefactoringElement;

/**
 *
 * @author Martin Janicek
 */
public class FindVariableUsages extends AbstractFindUsages {

    public FindVariableUsages(RefactoringElement element) {
        super(element);
    }

    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        return singleVisitor(new FindVariableUsagesVisitor(moduleNode));
    }


    private class FindVariableUsagesVisitor extends AbstractFindUsagesVisitor {

        private final String variableType;
        private final String variableName;

        
        public FindVariableUsagesVisitor(ModuleNode moduleNode) {
           super(moduleNode);
           assert (element instanceof VariableRefactoringElement) : "Expected VariableRefactoringElement but it was: " + element.getClass().getSimpleName(); // NOI18N
           final VariableRefactoringElement varElement = (VariableRefactoringElement) element;

           this.variableType = varElement.getVariableTypeName();
           this.variableName = varElement.getVariableName();
        }

        @Override
        public void visitField(FieldNode field) {
            if (!field.isSynthetic()) {
                addField(field);
            }
            super.visitField(field);
        }

        @Override
        public void visitProperty(PropertyNode property) {
            if (!property.isSynthetic()) {
                addField(property.getField());
            }
            super.visitProperty(property);
        }

        private void addField(FieldNode field) {
            final String fieldName = field.getName();
            final String fieldOwner = field.getOwner().getName();

            addIfEqual(field, fieldOwner, fieldName);
        }

        @Override
        public void visitPropertyExpression(PropertyExpression expression) {
            final Expression objectExpression = expression.getObjectExpression();
            if (objectExpression == null) {
                return;
            }

            final String varName = expression.getPropertyAsString();
            if (objectExpression instanceof VariableExpression) {
                final VariableExpression varExpression = ((VariableExpression) objectExpression);

                final String varType;
                if ("this".equals(varExpression.getName())) { // NOI18N
                    String fileName = getSourceUnit().getName();            // returns file name (e.g. Tester.groovy)
                    varType = fileName.substring(0, fileName.indexOf(".")); // remove the .groovy suffix
                } else {
                    varType = varExpression.getType().getName();
                }
                addIfEqual(expression.getProperty(), varType, varName);
            } else {
                // No need to check for "this" here
                addIfEqual(expression.getProperty(), objectExpression.getType().getName(), varName);
            }
            super.visitPropertyExpression(expression);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            final VariableExpression variableExpression = ((VariableExpression) expression);
            final Variable variable = variableExpression.getAccessedVariable();

            if (variable != null) {
                final String varName = variableExpression.getText();
                final String varType = variable.getType().getName();

                addIfEqual(expression, varType, varName);
            }
            super.visitVariableExpression(expression);
        }

        private void addIfEqual(ASTNode nodeToAdd, String type, String name) {
            // Currently visited field is dynamic --> Just check for the correct name
            // because we want to add everything that can possibly match the finding one
            if ("java.lang.Object".equals(type)) { // NOI18N
                addIfNameEquals(nodeToAdd, name);
            }

            // Finding field is dynamic --> Just check for the correct name because
            // we want to add everything that can possibly match it
            if ("java.lang.Object".equals(variableType)) { // NOI18N
                addIfNameEquals(nodeToAdd, name);
            }

            // Normal situation when both (finding and visiting) fields/variables
            // are statically typed and thus we can check if the types are the same
            
            // The reason why the endWith method is used instead of equals is that
            // in case of "this" reference we don't get whole fqn from the Expression
            // and only information we got is the name of the file
            if (variableType.endsWith(type)) {
                addIfNameEquals(nodeToAdd, name);
            }
        }

        private void addIfNameEquals(ASTNode nodeToAdd, String name) {
            if (variableName.equals(name)) {
                usages.add(nodeToAdd);
            }
        }
    }
}
