/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.completion.inference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;

/**
 * Visitor that find variables usable at given offset. This include
 * method/constructor parameters, closure parameters, variables defined in
 * for loop and of course local variables.
 * <p>
 * For local variables it handles:
 * <ul>
 *   <li>test = "something"
 *   <li>def test = "something"
 *   <li>def test
 *   <li>String test = "something"
 *   <li>String test
 * </ul>
 *
 * @author Petr Hejl
 */
// FIXME scriptMode ?
public class VariableFinderVisitor extends ClassCodeVisitorSupport {

    private final  SourceUnit sourceUnit;

    private final AstPath path;

    private final BaseDocument doc;

    private final int cursorOffset;

    private Set<ASTNode> blocks = new HashSet<ASTNode>();

    private Map<String, Variable> variables = new HashMap<String, Variable>();

    public VariableFinderVisitor(SourceUnit sourceUnit, AstPath path, BaseDocument doc, int cursorOffset) {
        this.sourceUnit = sourceUnit;
        this.path = path;
        this.doc = doc;
        this.cursorOffset = cursorOffset;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public Collection<Variable> getVariables() {
        return variables.values();
    }

    public void collect() {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, cursorOffset);
        if (ts == null) {
            return;
        }
        Token<GroovyTokenId> token = ts.token();
        if (token == null) {
            return;
        }

        ASTNode last = null;

        blocks.clear();
        variables.clear();

        // We are going through the path up marking all the declaration
        // blocks and the top (last) one.
        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode scope = it.next();
            if ((scope instanceof ClosureExpression) || (scope instanceof MethodNode)
                    || (scope instanceof ConstructorNode) || (scope instanceof ForStatement)
                    || (scope instanceof BlockStatement) || (scope instanceof ClosureListExpression)
                    || (scope instanceof CatchStatement)) {

                last = scope;
                blocks.add(scope);

                // In for loop we have to allow visitor to visit ClosureListExpression
                if ((scope instanceof ForStatement)
                        && (((ForStatement) scope).getCollectionExpression() instanceof ClosureListExpression)) {
                    blocks.add(((ForStatement) scope).getCollectionExpression());
                }
            }
        }

        // Lets visit the code from top. We visit only allowed blocks
        // to avoid visiting subtrees declared before offset, but not usable.
        // ie
        // def clos = {
        //     def x = {
        //         String str
        //     }
        //     ^ // we are here and we dont want to get str as possibility
        // }
        if (last instanceof ClosureExpression) {
            visitClosureExpression((ClosureExpression) last);
        } else if (last instanceof MethodNode) {
            visitMethod((MethodNode) last);
        } else if (last instanceof ConstructorNode) {
            visitConstructor((ConstructorNode) last);
        } else if (last instanceof ForStatement) {
            visitForLoop((ForStatement) last);
        } else if (last instanceof BlockStatement) {
            visitBlockStatement((BlockStatement) last);
        } else if (last instanceof ClosureListExpression) {
            visitClosureListExpression((ClosureListExpression) last);
        } else if (last instanceof CatchStatement) {
            visitCatchStatement((CatchStatement) last);
        }

    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        // if we are in the same block we check position, if it occurs after
        // current position we ignore it
        if (blocks.isEmpty()
                && expression.getLineNumber() >= 0 && expression.getColumnNumber() >= 0
                && path.getLineNumber() >= 0 && path.getColumnNumber() >= 0
                && (expression.getLineNumber() > path.getLineNumber()
                || (expression.getLineNumber() == path.getLineNumber() && expression.getColumnNumber() >= path.getColumnNumber()))) {
            return;
        }

        if (!expression.isMultipleAssignmentDeclaration()) {
            VariableExpression variableExpression = expression.getVariableExpression();
            if (variableExpression.getAccessedVariable() != null) {
                String name = variableExpression.getAccessedVariable().getName();
                variables.put(name, variableExpression.getAccessedVariable());
            }
        }
        // perhaps we could visit just declaration or do nothing
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        // if we are in the same block we check position, if it occurs after
        // current position we ignore it
        if (blocks.isEmpty()
                && expression.getLineNumber() >= 0 && expression.getColumnNumber() >= 0
                && path.getLineNumber() >= 0 && path.getColumnNumber() >= 0
                && (expression.getLineNumber() > path.getLineNumber()
                || (expression.getLineNumber() == path.getLineNumber() && expression.getColumnNumber() >= path.getColumnNumber()))) {
            return;
        }

        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof VariableExpression) {
            if (expression.getOperation().isA(Types.EQUAL)) {
                VariableExpression variableExpression = (VariableExpression) leftExpression;
                if (variableExpression.getAccessedVariable() != null) {
                    String name = variableExpression.getAccessedVariable().getName();
                    if (!variables.containsKey(name)) {
                        variables.put(name, variableExpression.getAccessedVariable());
                    }
                }
            }
        }
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (!blocks.remove(node)) {
            return;
        }

        for (Parameter param : node.getParameters()) {
            variables.put(param.getName(), param);
        }
        super.visitMethod(node);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        if (!blocks.remove(statement)) {
            return;
        }

        if (statement.getVariable() != null) {
            String name = statement.getVariable().getName();
            variables.put(name, statement.getVariable());
        }
        super.visitCatchStatement(statement);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (!blocks.remove(expression)) {
            return;
        }

        if (expression.isParameterSpecified()) {
            for (Parameter param : expression.getParameters()) {
                variables.put(param.getName(), param);
            }
        } else {
            variables.put("it", new VariableExpression("it")); // NOI18N
        }
        super.visitClosureExpression(expression);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        if (!blocks.remove(node)) {
            return;
        }

        for (Parameter param : node.getParameters()) {
            variables.put(param.getName(), param);
        }

        super.visitConstructor(node);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        if (!blocks.remove(forLoop)) {
            return;
        }

        Parameter param = forLoop.getVariable();
        if (param != ForStatement.FOR_LOOP_DUMMY) {
            variables.put(param.getName(), param);
        }
        super.visitForLoop(forLoop);
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        if (!blocks.remove(block)) {
            return;
        }

        super.visitBlockStatement(block);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression cle) {
        // FIXME whole tree allowed ?
        if (!blocks.remove(cle)) {
            return;
        }

        super.visitClosureListExpression(cle);
    }
}
