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

package org.netbeans.modules.groovy.editor.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;

/**
 * Visitor for finding direct chidren of AST node
 * 
 * @author Martin Adamek
 */
public class ASTChildrenVisitor implements GroovyCodeVisitor {

    private List<ASTNode> children = new ArrayList<>();
    
    public List<ASTNode> children() {
        return children;
    }
    
    @Override
    public void visitBlockStatement(BlockStatement block) {
        List statements = block.getStatements();
        for (Iterator iter = statements.iterator(); iter.hasNext(); ) {
            Statement statement = (Statement) iter.next();
            children.add(statement);
        }
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        children.add(forLoop.getCollectionExpression());
        children.add(forLoop.getLoopBlock());
    }

    @Override
    public void visitWhileLoop(WhileStatement loop) {
        children.add(loop.getBooleanExpression());
        children.add(loop.getLoopBlock());
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop) {
        children.add(loop.getLoopBlock());
        children.add(loop.getBooleanExpression());
    }

    @Override
    public void visitIfElse(IfStatement ifElse) {
        children.add(ifElse.getBooleanExpression());
        children.add(ifElse.getIfBlock());
        children.add(ifElse.getElseBlock());
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        children.add(statement.getExpression());
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        children.add(statement.getExpression());
    }

    @Override
    public void visitAssertStatement(AssertStatement statement) {
        children.add(statement.getBooleanExpression());
        children.add(statement.getMessageExpression());
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        children.add(statement.getTryStatement());
        List list = statement.getCatchStatements();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            CatchStatement catchStatement = (CatchStatement) iter.next();
            children.add(catchStatement);
        }
        children.add(statement.getFinallyStatement());
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        children.add(statement.getExpression());
        List list = statement.getCaseStatements();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            CaseStatement caseStatement = (CaseStatement) iter.next();
            children.add(caseStatement);
        }
        children.add(statement.getDefaultStatement());
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
        children.add(statement.getExpression());
        children.add(statement.getCode());
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        children.add(statement.getExpression());
        children.add(statement.getCode());
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        children.add(statement.getExpression());
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        children.add(call.getObjectExpression());
        children.add(call.getMethod());
        children.add(call.getArguments());
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        children.add(call.getArguments());
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        children.add(call.getArguments());
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        children.add(expression.getLeftExpression());
        children.add(expression.getRightExpression());
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        children.add(expression.getBooleanExpression());
        children.add(expression.getTrueExpression());
        children.add(expression.getFalseExpression());
    }
    
    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        children.add(expression.getCode());
    }

    @Override
    public void visitLambdaExpression(LambdaExpression expression) {
        visitClosureExpression(expression);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }
    
    @Override
    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        children.add(expression.getKeyExpression());
        children.add(expression.getValueExpression());
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        children.add(expression.getFrom());
        children.add(expression.getTo());
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        children.add(expression.getExpression());
    }
 
    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        children.add(expression.getExpression());
        children.add(expression.getMethodName());
    }

    @Override
    public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        visitMethodPointerExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        children.add(expression.getExpression());
    }
    
    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        children.add(expression.getExpression());
    }
    
    @Override
    public void visitCastExpression(CastExpression expression) {
        children.add(expression.getExpression());
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }
    
    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
    	children.add(expression.getObjectExpression());
    	children.add(expression.getProperty());
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
    	children.add(expression.getObjectExpression());
    	children.add(expression.getProperty());
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
    	children.add(statement.getCode());
    }
    
    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
    	visitTupleExpression(ale);
    }
    
    @Override
    public void visitClosureListExpression(ClosureListExpression cle) {
        visitListOfExpressions(cle.getExpressions());
    }

    // added in Groovy 1.6
    // TODO check this
    @Override
    public void visitBytecodeExpression(BytecodeExpression bce) {
    }
}
