/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            final VariableExpression variableExpression = expression;
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
