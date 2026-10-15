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

package org.netbeans.modules.groovy.editor.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Visitor that builds path to element identified at given position
 *
 * @todo skipping irrelevant subtrees, see IsInside(...) method
 *
 * @author Martin Adamek
 */
public class PathFinderVisitor extends ClassCodeVisitorSupport {

    private static final Logger LOG = Logger.getLogger(PathFinderVisitor.class.getName());

    private final SourceUnit sourceUnit;

    private final int line;
    
    private final int column;

    private final List<ASTNode> path = new ArrayList<ASTNode>();
    
    /**
     * Terminate at outermost node.
     */
    private final boolean outermost;

    public PathFinderVisitor(SourceUnit sourceUnit, int line, int column) {
        this(sourceUnit, line, column, false);
    }
    
    PathFinderVisitor(SourceUnit sourceUnit, int line, int column, boolean outermost) {
        this.sourceUnit = sourceUnit;
        this.line = line;
        this.column = column;
        this.outermost = outermost;
    }

    public List<ASTNode> getPath() {
        return new ArrayList<ASTNode>(path);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    // super visitor doesn't visit parameters
    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        super.visitConstructorOrMethod(node, isConstructor);
        for (Parameter parameter : node.getParameters()) {
            isInside(parameter, line, column);
        }
    }

    @Override
    protected void visitStatement(Statement statement) {
    }
    
    /**
     * Possibly terminates the search after adding node to the path. Returns false, if
     * the `outermost' is true and the line:column corresponds to the last character of
     * the tested node (returns false). This ensures that path terminates at the outermost
     * expression. 
     * 
     * @param node added/tested node
     * @return false to terminate depth-first traversal
     */
    private boolean addToPath(ASTNode node) {
        path.add(node);
        if (!outermost || node == null || !isInSource(node)) {
            return false;
        }
        if (!isInside(node, line, column, true, false)) {
            return false;
        }
        
        // exception: if the expression is a method expression and
        // the method expression does not use parenthesis, then any trailing thing will
        // be joined to the last method's argument expr.
        if (node instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression)node;
            if (mce.getArguments() instanceof ArgumentListExpression) {
                ArgumentListExpression ale = (ArgumentListExpression)mce.getArguments();
                if (ale.getLastLineNumber() == mce.getLastLineNumber() &&
                    ale.getLastColumnNumber() == mce.getLastColumnNumber()) {
                    return false;
                }
            }
        } else if (!(
                (node instanceof PropertyExpression) ||
                (node instanceof GStringExpression)
            )) {
            return false;
        }
        return true;
    }

    @Override
    public void visitBlockStatement(BlockStatement node) {
        if (isInside(node, line, column, false)) {
            if (addToPath(node)) {
                return;
            }
        } else {
            for (Object object : node.getStatements()) {
                if (isInside((ASTNode) object, line, column, false)) {
                    if (addToPath(node)) {
                        return;
                    }
                    break;
                }
            }
        }

        for (Object object : node.getStatements()) {
            Statement statement = (Statement) object;
            statement.visit(this);
        }
    }

    @Override
    public void visitForLoop(ForStatement node) {
        if (isInside(node, line, column)) {
            super.visitForLoop(node);
        }
    }

    @Override
    public void visitWhileLoop(WhileStatement node) {
        if (isInside(node, line, column)) {
            super.visitWhileLoop(node);
        }
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement node) {
        if (isInside(node, line, column)) {
            super.visitDoWhileLoop(node);
        }
    }

    @Override
    public void visitIfElse(IfStatement node) {
        if (isInside(node, line, column)) {
            super.visitIfElse(node);
        }
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement node) {
        if (isInside(node, line, column)) {
            super.visitExpressionStatement(node);
        }
    }

    @Override
    public void visitReturnStatement(ReturnStatement node) {
        if (isInside(node, line, column)) {
            super.visitReturnStatement(node);
        }
    }

    @Override
    public void visitAssertStatement(AssertStatement node) {
        if (isInside(node, line, column)) {
            super.visitAssertStatement(node);
        }
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement node) {
        if (isInside(node, line, column)) {
            super.visitTryCatchFinally(node);
        }
    }

    @Override
    public void visitSwitch(SwitchStatement node) {
        if (isInside(node, line, column)) {
            super.visitSwitch(node);
        }
    }

    @Override
    public void visitCaseStatement(CaseStatement node) {
        if (isInside(node, line, column)) {
            super.visitCaseStatement(node);
        }
    }

    @Override
    public void visitBreakStatement(BreakStatement node) {
        if (isInside(node, line, column)) {
            super.visitBreakStatement(node);
        }
    }

    @Override
    public void visitContinueStatement(ContinueStatement node) {
        if (isInside(node, line, column)) {
            super.visitContinueStatement(node);
        }
    }

    @Override
    public void visitThrowStatement(ThrowStatement node) {
        if (isInside(node, line, column)) {
            super.visitThrowStatement(node);
        }
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement node) {
        if (isInside(node, line, column)) {
            super.visitSynchronizedStatement(node);
        }
    }

    @Override
    public void visitCatchStatement(CatchStatement node) {
        if (isInside(node, line, column)) {
            super.visitCatchStatement(node);
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression node) {
        if (isInside(node, line, column)) {
                super.visitMethodCallExpression(node);
        }
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        if (isInside(node, line, column)) {
            super.visitStaticMethodCallExpression(node);
        }
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression node) {
        if (isInside(node, line, column)) {
            super.visitConstructorCallExpression(node);
        }
    }

    @Override
    public void visitTernaryExpression(TernaryExpression node) {
        if (isInside(node, line, column)) {
            super.visitTernaryExpression(node);
        }
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression node) {
        if (isInside(node, line, column)) {
            super.visitShortTernaryExpression(node);
        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression node) {
        if (isInside(node, line, column)) {
            super.visitBinaryExpression(node);
        }
    }

    @Override
    public void visitPrefixExpression(PrefixExpression node) {
        if (isInside(node, line, column)) {
            super.visitPrefixExpression(node);
        }
    }

    @Override
    public void visitPostfixExpression(PostfixExpression node) {
        if (isInside(node, line, column)) {
            super.visitPostfixExpression(node);
        }
    }

    @Override
    public void visitBooleanExpression(BooleanExpression node) {
        if (isInside(node, line, column)) {
            super.visitBooleanExpression(node);
        }
    }

    @Override
    public void visitClosureExpression(ClosureExpression node) {
        if (isInside(node, line, column)) {
            super.visitClosureExpression(node);
            if (node.isParameterSpecified()) {
                for (Parameter parameter : node.getParameters()) {
                    isInside(parameter, line, column);
                }
            }
        }
    }

    @Override
    public void visitTupleExpression(TupleExpression node) {
        if (isInside(node, line, column)) {
            super.visitTupleExpression(node);
        }
    }

    @Override
    public void visitMapExpression(MapExpression node) {
        if (isInside(node, line, column)) {
            super.visitMapExpression(node);
        }
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression node) {
        if (isInside(node, line, column)) {
            super.visitMapEntryExpression(node);
        }
    }

    @Override
    public void visitListExpression(ListExpression node) {
        if (isInside(node, line, column)) {
            super.visitListExpression(node);
        }
    }

    @Override
    public void visitRangeExpression(RangeExpression node) {
        if (isInside(node, line, column)) {
            super.visitRangeExpression(node);
        }
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {

        // XXX PropertyExpression has wrong offsets, e.g. 4-4 for 'this.field1 = 77'
        // and was never added to path,
        // therefore let's check if its children are wraping given position
        // and add it then

        Expression objectExpression = node.getObjectExpression();
        Expression property = node.getProperty();

        if (isInside(node, line, column, false)) {
            if (addToPath(node)) {
                return;
            }
        } else {
            boolean nodeAdded = false;
            if (isInside(objectExpression, line, column, false)) {
                if (addToPath(node)) {
                    return;
                }
                nodeAdded = true;
            }
            if (isInside(property, line, column, false)) {
                if (!nodeAdded) {
                    path.add(node);
                }
            }
        }

        objectExpression.visit(this);
        property.visit(this);
    }

    @Override
    public void visitAttributeExpression(AttributeExpression node) {
        if (isInside(node, line, column)) {
            super.visitAttributeExpression(node);
        }
    }

    @Override
    public void visitFieldExpression(FieldExpression node) {
        if (isInside(node, line, column)) {
            super.visitFieldExpression(node);
        }
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression node) {
        if (isInside(node, line, column)) {
            super.visitMethodPointerExpression(node);
        }
    }

    @Override
    public void visitConstantExpression(ConstantExpression node) {
        if (isInside(node, line, column)) {
            super.visitConstantExpression(node);
        }
    }

    @Override
    public void visitClassExpression(ClassExpression node) {
        if (isInside(node, line, column)) {
            super.visitClassExpression(node);
        }
    }

    @Override
    public void visitVariableExpression(VariableExpression node) {
        if (isInside(node, line, column)) {
            super.visitVariableExpression(node);
        }
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression node) {
        if (isInside(node, line, column)) {
            super.visitDeclarationExpression(node);
        }
    }

    @Override
    public void visitGStringExpression(GStringExpression node) {
        if (isInside(node, line, column)) {
            super.visitGStringExpression(node);
        }
    }

    @Override
    public void visitArrayExpression(ArrayExpression node) {
        if (isInside(node, line, column)) {
            super.visitArrayExpression(node);
        }
    }

    @Override
    public void visitSpreadExpression(SpreadExpression node) {
        if (isInside(node, line, column)) {
            super.visitSpreadExpression(node);
        }
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression node) {
        if (isInside(node, line, column)) {
            super.visitSpreadMapExpression(node);
        }
    }

    @Override
    public void visitNotExpression(NotExpression node) {
        if (isInside(node, line, column)) {
            super.visitNotExpression(node);
        }
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression node) {
        if (isInside(node, line, column)) {
            super.visitUnaryMinusExpression(node);
        }
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression node) {
        if (isInside(node, line, column)) {
            super.visitUnaryPlusExpression(node);
        }
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
        if (isInside(node, line, column)) {
            super.visitBitwiseNegationExpression(node);
        }
    }

    @Override
    public void visitCastExpression(CastExpression node) {
        if (isInside(node, line, column)) {
            super.visitCastExpression(node);
        }
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression node) {
        if (isInside(node, line, column)) {
            super.visitArgumentlistExpression(node);
        }
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression node) {
        if (isInside(node, line, column)) {
            super.visitClosureListExpression(node);
        }
    }

    @Override
    public void visitClass(ClassNode node) {
        if (isInside(node, line, column)) {
            super.visitClass(node);
        }
    }

    @Override
    protected void visitAnnotation(AnnotationNode node) {
        if (isInside(node, line, column)) {
            super.visitAnnotation(node);
        }
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        // we don't want synthetic constructors duplicating field initial expressions
        if (!node.isSynthetic() && isInside(node, line, column)) {
            super.visitConstructor(node);
        }
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (isInside(node, line, column)) {
            super.visitMethod(node);
        }
    }

    @Override
    public void visitField(FieldNode node) {
        // we don't want synthetic fields duplicating property initial expressions
        if (!node.isSynthetic() && isInside(node, line, column)) {
            super.visitField(node);
        }
    }

    @Override
    public void visitProperty(PropertyNode node) {
        // we don't want synthetic static initializers introducing this variables
        if (!node.isSynthetic() && isInside(node, line, column)) {
            FieldNode field = node.getField();
            if (field != null) {
                visitAnnotations(field);
            }
            super.visitProperty(node);
        }
    }

    @Override
    public void visitImports(ModuleNode node) {
        if (node != null) {
            for (ImportNode importNode : node.getImports()) {
                if (isInside(importNode, line, column)) {
                    visitAnnotations(importNode);
                    importNode.visit(this);
                }
            }
            for (ImportNode importStarNode : node.getStarImports()) {
                if (isInside(importStarNode, line, column)) {
                    visitAnnotations(importStarNode);
                    importStarNode.visit(this);
                }
            }
            for (ImportNode importStaticNode : node.getStaticImports().values()) {
                if (isInside(importStaticNode, line, column)) {
                    visitAnnotations(importStaticNode);
                    importStaticNode.visit(this);
                }
            }
            for (ImportNode importStaticStarNode : node.getStaticStarImports().values()) {
                if (isInside(importStaticStarNode, line, column)) {
                    visitAnnotations(importStaticStarNode);
                    importStaticStarNode.visit(this);
                }
            }
        }
    }

    private boolean isInside(ASTNode node, int line, int column) {
        return isInside(node, line, column, true);
    }
    
    private boolean isInside(ASTNode node, int line, int column, boolean addToPath) {
        return isInside(node, line, column, false, addToPath);
    }
    
    /**
     * Determines if the line:column is inside a node. 
     * Note: this method is hacked up, as the AST does not contain proper positions for some nodes,
     * the brute-force search in all subtrees must be done. So if `addToPath` is true, the method
     * returns true regardless of whether the line:column is inside or not.
     * This behaviour will be fixed - see NETBEANS-5935
     * <p>
     * Includes a special case behaviour if `atEnd` is true: checks just for the ending position in 
     * the node (= returns true iff line:column correspond to the end position)
     * @param node node to check
     * @param line the anchor line
     * @param column the anchor column
     * @param atEnd true, if we just check the end position
     * @param addToPath true, if the matching node should be added to path, false for pure query.
     * @return 
     */
    private boolean isInside(ASTNode node, int line, int column, boolean atEnd, boolean addToPath) {
        if (node == null || !isInSource(node)) {
            return false;
        }

        fixNode(node);

        int beginLine = node.getLineNumber();
        int beginColumn = node.getColumnNumber();
        int endLine = node.getLastLineNumber();
        int endColumn = node.getLastColumnNumber();

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "isInside: " + node + " - "
                    + beginLine + ", " + beginColumn + ", " + endLine + ", " + endColumn);
        }

        if (beginLine == -1 || beginColumn == -1 || endLine == -1 || endColumn == -1) {
            // this node doesn't provide its coordinates, some wrappers do that
            // let's say yes and visit its children
            return !atEnd && addToPath ? true : false;
        }

        if (node instanceof ArgumentListExpression || node instanceof TupleExpression) {
            beginColumn++;
            endColumn--;
        }

        boolean result = false;

        if (beginLine == endLine) {
            if (line == beginLine && column >= beginColumn && column < endColumn) {
                if (atEnd) {
                    if (column == endColumn - 1) {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }
        } else if (line == beginLine) {
            if (column >= beginColumn) {
                result = !atEnd;
            }
        } else if (line == endLine) {
            if (column < endColumn) {
                if (atEnd) {
                    if (column == endColumn - 1) {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }
        } else if (beginLine < line && line < endLine) {
            result = !atEnd;
        } else {
            result = false;
        }

        if (result && addToPath) {
            if (addToPath(node)) {
                return false;
            }
            LOG.log(Level.FINEST, "Path: {0}", path);
        }

        // if addToPath is false, return result, we want to know real state of affairs
        // and not to continue traversing
        return addToPath ? true : result;
    }

    private void fixNode(ASTNode node) {
        // FIXME http://jira.codehaus.org/browse/GROOVY-3263
        if (node instanceof MethodCallExpression && !((MethodCallExpression) node).isImplicitThis()) {
            MethodCallExpression call = (MethodCallExpression) node;
            if (call.getObjectExpression() == VariableExpression.THIS_EXPRESSION
                    || call.getObjectExpression() == VariableExpression.SUPER_EXPRESSION) {
                // this is not bulletproof but fix most of the problems
                VariableExpression var = new VariableExpression(
                        call.getObjectExpression() == VariableExpression.THIS_EXPRESSION ? "this" : "super", // NOI18N
                        call.getObjectExpression().getType()); // NOI18N
                var.setLineNumber(call.getLineNumber());
                var.setColumnNumber(call.getColumnNumber());
                var.setLastLineNumber(call.getMethod().getLineNumber());
                var.setLastColumnNumber(call.getMethod().getColumnNumber());
                call.setObjectExpression(var);
            }
        // FIXME http://jira.codehaus.org/browse/GROOVY-3472
        } else if (node instanceof MethodNode || node instanceof ClosureExpression) {
            Statement code = null;
            if (node instanceof MethodNode) {
                code = ((MethodNode) node).getCode();
            } else {
                code = ((ClosureExpression) node).getCode();
            }

            if (code instanceof BlockStatement
                && ((code.getLineNumber() < 0 && code.getColumnNumber() < 0)
                    || (code.getLastLineNumber() < 0 && code.getLastColumnNumber() < 0))) {
                BlockStatement block = (BlockStatement) code;
                List<Statement> statements = block.getStatements();
                if (statements != null && !statements.isEmpty()) {
                    if (code.getLineNumber() < 0 && code.getColumnNumber() < 0) {
                        Statement first = statements.get(0);
                        code.setLineNumber(first.getLineNumber());
                        code.setColumnNumber(first.getColumnNumber());
                    }
                    if (code.getLastLineNumber() < 0 && code.getLastColumnNumber() < 0) {
                        // maybe not accurate
                        code.setLastLineNumber(node.getLastLineNumber());
                        int lastColumn = node.getLastColumnNumber();
                        if (lastColumn > 0) {
                            lastColumn--;
                        }
                        code.setLastColumnNumber(lastColumn);
                    }
                }
            }
        }
    }

    private boolean isInSource(ASTNode node) {
        if (node instanceof AnnotatedNode) {
            if (((AnnotatedNode) node).hasNoRealSourcePosition()) {
                return false;
            }
        }

        // FIXME probably http://jira.codehaus.org/browse/GROOVY-3263
        if (node instanceof StaticMethodCallExpression && node.getLineNumber() == -1
                && node.getLastLineNumber() == -1 && node.getColumnNumber() == -1
                && node.getLastColumnNumber() == -1) {

            StaticMethodCallExpression methodCall = (StaticMethodCallExpression) node;
            if ("initMetaClass".equals(methodCall.getMethod())) { // NOI18N
                Expression args = methodCall.getArguments();
                if (args instanceof VariableExpression) {
                    VariableExpression var = (VariableExpression) args;
                    if ("this".equals(var.getName())) { // NOI18N
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
