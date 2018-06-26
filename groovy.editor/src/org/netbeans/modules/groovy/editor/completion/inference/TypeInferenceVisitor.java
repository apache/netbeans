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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion.inference;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.netbeans.editor.BaseDocument;
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

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (expression == leaf) {
            leafReached = true;
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
                        guessedType = ((ConstructorCallExpression) rightExpression).getType();
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

}
