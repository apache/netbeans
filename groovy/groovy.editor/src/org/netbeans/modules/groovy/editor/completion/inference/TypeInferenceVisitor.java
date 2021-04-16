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

package org.netbeans.modules.groovy.editor.completion.inference;

import groovy.lang.Range;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.occurrences.TypeVisitor;

/**
 *
 * @author Petr Hejl
 */
public class TypeInferenceVisitor extends TypeVisitor {

    private ClassNode guessedType;

    private boolean leafReached = false; // flag saying if visiting reached the node that we are investigating

    public TypeInferenceVisitor(SourceUnit sourceUnit, AstPath path, BaseDocument doc, int cursorOffset) {
        // we don't want to visit all classes in module
        super(sourceUnit, path, doc, cursorOffset, false);
    }

    /**
     * Tries to guess the type from the last assignment expression before actual
     * position of the leaf
     *
     * @return guessed type or null if there is no way to calculate it
     */
    public ClassNode getGuessedType() {
        return guessedType;
    }

    @Override
    public void collect() {
        guessedType = null;
        leafReached = false;

        super.collect();
    }

    @Override
    protected void visitParameters(Parameter[] parameters, Variable variable) {
        if (!leafReached) {
            for (Parameter param : parameters) {
                if (sameVariableName(param, variable)) {
                    guessedType = param.getType();
                    break;
                }
            }
        }
    }

    public void visitField(FieldNode node) {
        if (sameVariableName(leaf, node)) {
            if (node.hasInitialExpression()){
                Expression expression = node.getInitialExpression();
                if (expression instanceof ConstantExpression
                        && !expression.getText().equals("null")) { // NOI18N
                    guessedType = ((ConstantExpression) expression).getType();
                } else if (expression instanceof ConstructorCallExpression) {
                    guessedType = ((ConstructorCallExpression) expression).getType();
                } else if (expression instanceof MethodCallExpression) {
                    int newOffset = ASTUtils.getOffset(doc, expression.getLineNumber(), expression.getColumnNumber());
                    AstPath newPath = new AstPath(path.root(), newOffset, doc);
                    guessedType = MethodInference.findCallerType(expression, newPath, doc, newOffset);
                }
            }
        }
    }


    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (sameVariableName(leaf, expression.getLeftExpression())) {
            guessedType = deriveExpressonType(expression.getRightExpression());
        }
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
            if (expression.isSuperExpression()) {
                guessedType = expression.getType().getSuperClass();
            }
            if (null != expression.getAccessedVariable()) {
                Variable accessedVariable = expression.getAccessedVariable();

                if (accessedVariable.hasInitialExpression()) {
                    Expression initialExpression = expression.getAccessedVariable().getInitialExpression();
                    if (initialExpression instanceof ConstantExpression
                            && !initialExpression.getText().equals("null")) { // NOI18N
                        guessedType = ((ConstantExpression) initialExpression).getType();
                    } else if (initialExpression instanceof ConstructorCallExpression) {
                        guessedType = ClassHelper.make(((ConstructorCallExpression) initialExpression).getType().getName());
                    } else if (initialExpression instanceof MethodCallExpression) {
                        int newOffset = ASTUtils.getOffset(doc, initialExpression.getLineNumber(), initialExpression.getColumnNumber());
                        AstPath newPath = new AstPath(path.root(), newOffset, doc);
                        guessedType = MethodInference.findCallerType(initialExpression, newPath, doc, newOffset);
                    } else if (initialExpression instanceof ListExpression) {
                        guessedType = ((ListExpression) initialExpression).getType();
                    } else if (initialExpression instanceof MapExpression) {
                        guessedType = ((MapExpression) initialExpression).getType();
                    } else if (initialExpression instanceof RangeExpression) {
                        // this should work, but the type is Object - nut sure why
                        // guessedType = ((RangeExpression)initialExpression).getType();
                        guessedType = ClassHelper.makeWithoutCaching(Range.class, true);                
                    }
                } else if (accessedVariable instanceof Parameter) {
                    Parameter param = (Parameter) accessedVariable;
                    guessedType = param.getType();
                }
            } else if (!expression.getType().getName().equals("java.lang.Object")) {
                guessedType = expression.getType();

            }
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        if (!leafReached) {
            // have a look at assignment and try to get type from its right side
            Expression leftExpression = expression.getLeftExpression();
            if (leftExpression instanceof VariableExpression) {
                if (expression.getOperation().isA(Types.EQUAL) && sameVariableName(leaf, leftExpression)) {
                    Expression rightExpression = expression.getRightExpression();
                    if (rightExpression instanceof ConstantExpression
                            && !rightExpression.getText().equals("null")) { // NOI18N
                        guessedType = ((ConstantExpression) rightExpression).getType();
                    } else if (rightExpression instanceof ConstructorCallExpression) {
                        guessedType = ClassHelper.make(((ConstructorCallExpression) rightExpression).getType().getName());
                    } else if (rightExpression instanceof MethodCallExpression) {
                        guessedType = MethodInference.findCallerType(rightExpression, path, doc, cursorOffset);
                    } else if (rightExpression instanceof StaticMethodCallExpression) {
                        guessedType = MethodInference.findCallerType(rightExpression, path, doc, cursorOffset);
                    }
                }
            }
        }
        super.visitBinaryExpression(expression);
    }

    private static boolean sameVariableName(Parameter param, Variable variable) {
        return param.getName().equals(variable.getName());
    }

    private static boolean sameVariableName(ASTNode node1, ASTNode node2) {
        return node1 instanceof VariableExpression && node2 instanceof VariableExpression
                && ((VariableExpression) node1).getName().equals(((VariableExpression) node2).getName());
    }
	
    private static boolean sameVariableName(ASTNode node1, FieldNode node2) {
        return node1 instanceof VariableExpression
                && ((VariableExpression) node1).getName().equals(node2.getName());
    }

    private ClassNode deriveExpressonType(Expression expression) {
        ClassNode derivedExpressionType = null;
        if (expression instanceof ConstantExpression
                && !expression.getText().equals("null")) { // NOI18N
            derivedExpressionType = ((ConstantExpression) expression).getType();
        } else if (expression instanceof ConstructorCallExpression) {
            derivedExpressionType = ((ConstructorCallExpression) expression).getType();
        } else if (expression instanceof MethodCallExpression) {
            int newOffset = ASTUtils.getOffset(doc, expression.getLineNumber(), expression.getColumnNumber());
            AstPath newPath = new AstPath(path.root(), newOffset, doc);
            derivedExpressionType = MethodInference.findCallerType(expression, newPath, doc, newOffset);
        } else if (expression instanceof StaticMethodCallExpression) {
            derivedExpressionType = MethodInference.findCallerType(expression, path, doc, cursorOffset);
        } else if (expression instanceof ListExpression) {
            derivedExpressionType = ((ListExpression) expression).getType();
        } else if (expression instanceof MapExpression) {
            derivedExpressionType = ((MapExpression) expression).getType();
        } else if (expression instanceof RangeExpression) {
            // this should work, but the type is Object - nut sure why
            // guessedType = ((RangeExpression)initialExpression).getType();
            derivedExpressionType = ClassHelper.makeWithoutCaching(Range.class, true);                
        } 
        return derivedExpressionType;
    }

}
