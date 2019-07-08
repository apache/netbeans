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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 *
 * @author petr
 */
public interface Visitor {

    public void visit(ArrayAccess arrayAccess);

    //public void endVisit(ArrayAccess arrayAccess);
    public void visit(ArrayCreation arrayCreation);

    //public void endVisit(ArrayCreation arrayCreation);
    public void visit(ArrayElement arrayElement);

    public void visit(ArrowFunctionDeclaration arrowFunctionDeclaration);

    //public void endVisit(ArrayElement arrayElement);
    public void visit(Assignment assignment);

    //public void endVisit(Assignment assignment);
    public void visit(ASTError astError);

    public void visit(ASTErrorExpression astErrorExpression);

    //public void endVisit(ASTError astError);
    public void visit(BackTickExpression backTickExpression);

    //public void endVisit(BackTickExpression backTickExpression);
    public void visit(Block block);

    //public void endVisit(Block block);
    public void visit(BreakStatement breakStatement);

    //public void endVisit(BreakStatement breakStatement);
    public void visit(CastExpression castExpression);

    //public void endVisit(CastExpression castExpression);
    public void visit(CatchClause catchClause);

    //public void endVisit(CatchClause catchClause);
    public void visit(ConstantDeclaration classConstantDeclaration);

    //public void endVisit(ClassConstantDeclaration classConstantDeclaration);
    public void visit(ClassDeclaration classDeclaration);

    //public void endVisit(ClassDeclaration classDeclaration);
    public void visit(ClassInstanceCreation classInstanceCreation);

    //public void endVisit(ClassInstanceCreation classInstanceCreation);
    public void visit(ClassName className);

    //public void endVisit(ClassName className);
    public void visit(CloneExpression cloneExpression);

    //public void endVisit(CloneExpression cloneExpression);
    public void visit(Comment comment);

    //public void endVisit(Comment comment);
    public void visit(ConditionalExpression conditionalExpression);

    //public void endVisit(ConditionalExpression conditionalExpression);
    public void visit(ContinueStatement continueStatement);

    //public void endVisit(ContinueStatement continueStatement);
    public void visit(DeclareStatement declareStatement);

    public void visit(DereferencableVariable dereferencableVariable);

    //public void endVisit(DeclareStatement declareStatement);
    public void visit(DoStatement doStatement);

    //public void endVisit(DoStatement doStatement);
    public void visit(EchoStatement echoStatement);

    //public void endVisit(EchoStatement echoStatement);
    public void visit(EmptyStatement emptyStatement);

    public void visit(ExpressionArrayAccess node);

    //public void endVisit(EmptyStatement emptyStatement);
    public void visit(ExpressionStatement expressionStatement);

    //public void endVisit(ExpressionStatement expressionStatement);
    public void visit(FieldAccess fieldAccess);

    //public void endVisit(FieldAccess fieldAccess);
    public void visit(FieldsDeclaration fieldsDeclaration);

    public void visit(FinallyClause finallyClause);

    //public void endVisit(FieldsDeclaration fieldsDeclaration);
    public void visit(ForEachStatement forEachStatement);

    //public void endVisit(ForEachStatement forEachStatement);
    public void visit(FormalParameter formalParameter);

    //public void endVisit(FormalParameter formalParameter);
    public void visit(ForStatement forStatement);

    //public void endVisit(ForStatement forStatement);
    public void visit(FunctionDeclaration functionDeclaration);

    //public void endVisit(FunctionDeclaration functionDeclaration);
    public void visit(FunctionInvocation functionInvocation);

    //public void endVisit(FunctionInvocation functionInvocation);
    public void visit(FunctionName functionName);

    //public void endVisit(FunctionName functionName);
    public void visit(GlobalStatement globalStatement);

    //public void endVisit(GlobalStatement globalStatement);
    public void visit(Identifier identifier);

    //public void endVisit(Identifier identifier);
    public void visit(NamespaceName namespaceName);

    public void visit(NullableType nullableType);

    public void visit(NamespaceDeclaration declaration);

    public void visit(GotoLabel label);

    public void visit(GotoStatement statement);

    public void visit(LambdaFunctionDeclaration declaration);

    public void visit(UseStatement statement);

    public void visit(SingleUseStatementPart statementPart);

    public void visit(GroupUseStatementPart statementPart);

    public void visit(IfStatement ifStatement);

    //public void endVisit(IfStatement ifStatement);
    public void visit(IgnoreError ignoreError);

    //public void endVisit(IgnoreError ignoreError);
    public void visit(Include include);

    //public void endVisit(Include include);
    public void visit(InfixExpression infixExpression);

    //public void endVisit(InfixExpression infixExpression);
    public void visit(InLineHtml inLineHtml);

    //public void endVisit(InLineHtml inLineHtml);
    public void visit(InstanceOfExpression instanceOfExpression);

    //public void endVisit(InstanceOfExpression instanceOfExpression);
    public void visit(InterfaceDeclaration interfaceDeclaration);

    //public void endVisit(InterfaceDeclaration interfaceDeclaration);
    public void visit(ListVariable listVariable);

    //public void endVisit(ListVariable listVariable);
    public void visit(MethodDeclaration methodDeclaration);

    //public void endVisit(MethodDeclaration methodDeclaration);
    public void visit(MethodInvocation methodInvocation);

    //public void endVisit(MethodInvocation methodInvocation);
    public void visit(ParenthesisExpression parenthesisExpression);

    public void visit(PHPDocBlock phpDocBlock);

    public void visit(PHPDocTag phpDocTag);

    public void visit(PHPDocNode phpDocNode);

    public void visit(PHPDocTypeNode phpDocTypeNode);

    public void visit(PHPDocStaticAccessType node);

    public void visit(PHPDocTypeTag node);

    public void visit(PHPDocVarTypeTag node);

    public void visit(PHPVarComment node);

    public void visit(PHPDocMethodTag node);

    //public void endVisit(ParenthesisExpression parenthesisExpression);
    public void visit(PostfixExpression postfixExpression);

    //public void endVisit(PostfixExpression postfixExpression);
    public void visit(PrefixExpression prefixExpression);

    //public void endVisit(PrefixExpression prefixExpression);
    public void visit(Program program);

    //public void endVisit(Program program);
    public void visit(Quote quote);

    //public void endVisit(Quote quote);
    public void visit(Reference reference);

    //public void endVisit(Reference reference);
    public void visit(ReflectionVariable reflectionVariable);

    //public void endVisit(ReflectionVariable reflectionVariable);
    public void visit(ReturnStatement returnStatement);

    //public void endVisit(ReturnStatement returnStatement);
    public void visit(Scalar scalar);

    //public void endVisit(Scalar scalar);
    public void visit(SingleFieldDeclaration singleFieldDeclaration);

    //public void endVisit(SingleFieldDeclaration singleFieldDeclaration);
    public void visit(StaticConstantAccess classConstantAccess);

    //public void endVisit(StaticConstantAccess staticConstantAccess);
    public void visit(StaticFieldAccess staticFieldAccess);

    //public void endVisit(StaticFieldAccess staticFieldAccess);
    public void visit(StaticMethodInvocation staticMethodInvocation);

    //public void endVisit(StaticMethodInvocation staticMethodInvocation);
    public void visit(StaticStatement staticStatement);

    //public void endVisit(StaticStatement staticStatement);
    public void visit(SwitchCase switchCase);

    //public void endVisit(SwitchCase switchCase);
    public void visit(SwitchStatement switchStatement);

    //public void endVisit(SwitchStatement switchStatement);
    public void visit(ThrowStatement throwStatement);

    public void visit(TraitDeclaration traitDeclaration);

    public void visit(TraitMethodAliasDeclaration traitsAliasStatement);

    public void visit(TraitConflictResolutionDeclaration traitsInsteadofStatement);

    //public void endVisit(ThrowStatement throwStatement);
    public void visit(TryStatement tryStatement);

    //public void endVisit(TryStatement tryStatement);
    public void visit(UnaryOperation unaryOperation);

    public void visit(UnpackableArrayElement unpackableArrayElement);

    public void visit(UseTraitStatement useTraitStatement);

    public void visit(UseTraitStatementPart useTraitStatementPart);

    //public void endVisit(UnaryOperation unaryOperation);
    public void visit(Variable variable);

    public void visit(Variadic variadic);

    ////public void endVisit(Variable variable);
    public void visit(WhileStatement whileStatement);

    public void visit(YieldExpression node);

    public void visit(YieldFromExpression node);

    ////public void endVisit(WhileStatement whileStatement);
    public void visit(ASTNode node);
    ////public void endVisit(ASTNode node);

    public void visit(AnonymousObjectVariable node);

    public void visit(DereferencedArrayAccess node);

    public void visit(ArrayDimension node);

    public void visit(HaltCompiler node);
}
