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
package org.netbeans.modules.php.editor.parser.astnodes.visitors;

import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BreakStatement;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.CastExpression;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.CloneExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DereferencableVariable;
import org.netbeans.modules.php.editor.parser.astnodes.DereferencedArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EchoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FinallyClause;
import org.netbeans.modules.php.editor.parser.astnodes.FirstClassCallableArg;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.HaltCompiler;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IgnoreError;
import org.netbeans.modules.php.editor.parser.astnodes.InLineHtml;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.MatchArm;
import org.netbeans.modules.php.editor.parser.astnodes.MatchExpression;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamedArgument;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Quote;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression;
import org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.Visitor;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.YieldExpression;
import org.netbeans.modules.php.editor.parser.astnodes.YieldFromExpression;

/**
 *
 * @author Petr Pisl
 */
public class DefaultVisitor implements Visitor {

    public void scan(ASTNode node) {
        if (node != null) {
            node.accept(this);
        }
    }

    public void scan(Iterable<? extends ASTNode> nodes) {
        if (nodes != null) {
            for (ASTNode n : nodes) {
                scan(n);
            }
        }
    }

    @Override
    public void visit(ArrayAccess node) {
        scan(node.getName());
        scan(node.getDimension());
    }

    @Override
    public void visit(ArrayCreation node) {
        scan(node.getElements());
    }

    @Override
    public void visit(ArrayElement node) {
        scan(node.getKey());
        scan(node.getValue());
    }

    @Override
    public void visit(ArrowFunctionDeclaration node) {
        scan(node.getAttributes());
        scan(node.getFormalParameters());
        scan(node.getReturnType());
        scan(node.getExpression());
    }

    @Override
    public void visit(Assignment node) {
        scan(node.getLeftHandSide());
        scan(node.getRightHandSide());
    }

    @Override
    public void visit(ASTError astError) {
    }

    @Override
    public void visit(ASTErrorExpression astErrorExpression) {
    }

    @Override
    public void visit(Attribute attribute) {
        scan(attribute.getAttributeDeclarations());
    }

    @Override
    public void visit(AttributeDeclaration attributeDeclaration) {
        scan(attributeDeclaration.getAttributeName());
        scan(attributeDeclaration.getParameters());
    }

    @Override
    public void visit(BackTickExpression node) {
        scan(node.getExpressions());
    }

    @Override
    public void visit(Block node) {
        scan(node.getStatements());
    }

    @Override
    public void visit(BreakStatement node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(CaseDeclaration node) {
        scan(node.getAttributes());
        scan(node.getName());
        scan(node.getInitializer());
    }

    @Override
    public void visit(CastExpression node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(CatchClause node) {
        scan(node.getClassNames());
        scan(node.getVariable());
        scan(node.getBody());
    }

    @Override
    public void visit(ConstantDeclaration node) {
        scan(node.getAttributes());
        scan(node.getConstType());
        scan(node.getNames());
        scan(node.getInitializers());
    }

    @Override
    public void visit(ClassDeclaration node) {
        scan(node.getAttributes());
        scan(node.getName());
        scan(node.getSuperClass());
        scan(node.getInterfaces());
        scan(node.getBody());
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        scan(node.getAttributes());
        scan(node.getClassName());
        scan(node.ctorParams());
        scan(node.getSuperClass());
        scan(node.getInterfaces());
        scan(node.getBody());
    }

    @Override
    public void visit(ClassName node) {
        scan(node.getName());
    }

    @Override
    public void visit(CloneExpression node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(Comment comment) {
    }

    @Override
    public void visit(ConstantVariable constantVariable) {
        scan(constantVariable.getName());
    }

    @Override
    public void visit(ConditionalExpression node) {
        scan(node.getCondition());
        scan(node.getIfTrue());
        scan(node.getIfFalse());
    }

    @Override
    public void visit(ContinueStatement node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(DeclareStatement node) {
        scan(node.getDirectiveNames());
        scan(node.getDirectiveValues());
        scan(node.getBody());
    }

    @Override
    public void visit(DereferencableVariable node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(DoStatement node) {
        scan(node.getCondition());
        scan(node.getBody());
    }

    @Override
    public void visit(EchoStatement node) {
        scan(node.getExpressions());
    }

    @Override
    public void visit(EmptyStatement emptyStatement) {
    }

    @Override
    public void visit(EnumDeclaration node) {
        scan(node.getAttributes());
        scan(node.getName());
        scan(node.getBackingType());
        scan(node.getInterfaces());
        scan(node.getBody());
    }

    @Override
    public void visit(ExpressionArrayAccess node) {
        scan(node.getExpression());
        scan(node.getDimension());
    }

    @Override
    public void visit(ExpressionStatement node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(FieldAccess node) {
        scan(node.getDispatcher());
        scan(node.getField());
    }

    @Override
    public void visit(FieldsDeclaration node) {
        scan(node.getAttributes());
        scan(node.getFieldType());
        scan(node.getFields());
    }

    @Override
    public void visit(FinallyClause node) {
        scan(node.getBody());
    }

    @Override
    public void visit(FirstClassCallableArg firstClassCallableArg) {
        // noop
    }

    @Override
    public void visit(ForEachStatement node) {
        scan(node.getExpression());
        scan(node.getKey());
        scan(node.getValue());
        scan(node.getStatement());
    }

    @Override
    public void visit(FormalParameter node) {
        scan(node.getAttributes());
        scan(node.getParameterType());
        scan(node.getParameterName());
        scan(node.getDefaultValue());
    }

    @Override
    public void visit(ForStatement node) {
        scan(node.getInitializers());
        scan(node.getConditions());
        scan(node.getUpdaters());
        scan(node.getBody());
    }

    @Override
    public void visit(FunctionDeclaration node) {
        scan(node.getAttributes());
        scan(node.getFunctionName());
        scan(node.getFormalParameters());
        scan(node.getReturnType());
        scan(node.getBody());
    }

    @Override
    public void visit(FunctionInvocation node) {
        scan(node.getFunctionName());
        scan(node.getParameters());
    }

    @Override
    public void visit(FunctionName node) {
        scan(node.getName());
    }

    @Override
    public void visit(GlobalStatement node) {
        scan(node.getVariables());
    }

    @Override
    public void visit(Identifier identifier) {
    }

    @Override
    public void visit(IfStatement node) {
        scan(node.getCondition());
        scan(node.getTrueStatement());
        scan(node.getFalseStatement());
    }

    @Override
    public void visit(IgnoreError node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(Include node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(InfixExpression node) {
        scan(node.getLeft());
        scan(node.getRight());
    }

    @Override
    public void visit(InLineHtml inLineHtml) {
    }

    @Override
    public void visit(InstanceOfExpression node) {
        scan(node.getExpression());
        scan(node.getClassName());
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        scan(node.getAttributes());
        scan(node.getName());
        scan(node.getInterfaces());
        scan(node.getBody());
    }

    @Override
    public void visit(IntersectionType node) {
        scan(node.getTypes());
    }

    @Override
    public void visit(ListVariable node) {
        scan(node.getElements());
    }

    @Override
    public void visit(MatchArm node) {
        scan(node.getConditions());
        scan(node.getExpression());
    }

    @Override
    public void visit(MatchExpression node) {
        scan(node.getExpression());
        scan(node.getMatchArms());
    }

    @Override
    public void visit(MethodDeclaration node) {
        scan(node.getAttributes());
        scan(node.getFunction());
    }

    @Override
    public void visit(MethodInvocation node) {
        scan(node.getDispatcher());
        scan(node.getMethod());
    }

    @Override
    public void visit(NamedArgument node) {
        scan(node.getParameterName());
        scan(node.getExpression());
    }

    @Override
    public void visit(ParenthesisExpression node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(PostfixExpression node) {
        scan(node.getVariable());
    }

    @Override
    public void visit(PrefixExpression node) {
        scan(node.getVariable());
    }

    @Override
    public void visit(Program program) {
        scan(program.getStatements());
    }

    @Override
    public void visit(Quote node) {
        scan(node.getExpressions());
    }

    @Override
    public void visit(Reference node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(ReflectionVariable node) {
        scan(node.getName());
    }

    @Override
    public void visit(ReturnStatement node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(Scalar scalar) {
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        scan(node.getName());
        scan(node.getValue());
    }

    @Override
    public void visit(StaticConstantAccess node) {
        scan(node.getDispatcher());
        scan(node.getConstant());
    }

    @Override
    public void visit(StaticFieldAccess node) {
        scan(node.getDispatcher());
        scan(node.getField());
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        scan(node.getDispatcher());
        scan(node.getMethod());
    }

    @Override
    public void visit(StaticStatement node) {
        scan(node.getExpressions());
    }

    @Override
    public void visit(SwitchCase node) {
        scan(node.getValue());
        scan(node.getActions());
    }

    @Override
    public void visit(SwitchStatement node) {
        scan(node.getExpression());
        scan(node.getBody());
    }

    @Override
    public void visit(ThrowExpression node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(TryStatement node) {
        scan(node.getCatchClauses());
        scan(node.getBody());
        scan(node.getFinallyClause());
    }

    @Override
    public void visit(UnaryOperation node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(UnionType node) {
        scan(node.getTypes());
    }

    @Override
    public void visit(Variable node) {
        scan(node.getName());
    }

    @Override
    public void visit(Variadic node) {
        scan(node.getExpression());
    }

    @Override
    public void visit(WhileStatement node) {
        scan(node.getCondition());
        scan(node.getBody());
    }

    @Override
    public void visit(ASTNode node) {
    }

    @Override
    public void visit(PHPDocBlock node) {
        scan(node.getTags());
    }

    @Override
    public void visit(PHPDocNode node) {
    }

    @Override
    public void visit(PHPDocTag node) {
    }

    @Override
    public void visit(PHPDocTypeNode node) {
    }

    @Override
    public void visit(PHPDocTypeTag node) {
        scan(node.getTypes());
    }

    @Override
    public void visit(PHPDocMethodTag node) {
        scan(node.getMethodName());
        scan(node.getTypes());
        scan(node.getParameters());
    }

    @Override
    public void visit(PHPDocVarTypeTag node) {
        scan(node.getVariable());
        scan(node.getTypes());
    }

    @Override
    public void visit(PHPDocStaticAccessType node) {
        scan(node.getClassName());
        scan(node.getConstant());
    }

    @Override
    public void visit(PHPVarComment node) {
        scan(node.getVariable());
    }

    @Override
    public void visit(NamespaceName namespaceName) {
        scan(namespaceName.getSegments());
    }

    @Override
    public void visit(NullableType nullableType) {
        scan(nullableType.getType());
    }

    @Override
    public void visit(NamespaceDeclaration declaration) {
        scan(declaration.getName());
        scan(declaration.getBody());
    }

    @Override
    public void visit(GotoLabel label) {
        scan(label.getName());
    }

    @Override
    public void visit(GotoStatement statement) {
        scan(statement.getLabel());
    }

    @Override
    public void visit(LambdaFunctionDeclaration declaration) {
        scan(declaration.getAttributes());
        scan(declaration.getFormalParameters());
        scan(declaration.getLexicalVariables());
        scan(declaration.getReturnType());
        scan(declaration.getBody());
    }

    @Override
    public void visit(UseStatement statement) {
        scan(statement.getParts());
    }

    @Override
    public void visit(SingleUseStatementPart statementPart) {
        scan(statementPart.getName());
        scan(statementPart.getAlias());
    }

    @Override
    public void visit(GroupUseStatementPart statementPart) {
        scan(statementPart.getBaseNamespaceName());
        scan(statementPart.getItems());
    }

    @Override
    public void visit(TraitDeclaration traitDeclaration) {
        scan(traitDeclaration.getAttributes());
        scan(traitDeclaration.getName());
        scan(traitDeclaration.getBody());
    }

    @Override
    public void visit(TraitMethodAliasDeclaration traitsAliasStatement) {
        scan(traitsAliasStatement.getTraitName());
        scan(traitsAliasStatement.getOldMethodName());
        scan(traitsAliasStatement.getNewMethodName());
    }

    @Override
    public void visit(TraitConflictResolutionDeclaration traitsInsteadofStatement) {
        scan(traitsInsteadofStatement.getPreferredTraitName());
        scan(traitsInsteadofStatement.getMethodName());
        scan(traitsInsteadofStatement.getSuppressedTraitNames());
    }

    @Override
    public void visit(UnpackableArrayElement unpackableArrayElement) {
        scan(unpackableArrayElement.getValue());
    }

    @Override
    public void visit(UseTraitStatement useTraitsStatement) {
        scan(useTraitsStatement.getParts());
        scan(useTraitsStatement.getBody());
    }

    @Override
    public void visit(UseTraitStatementPart useTraitStatementPart) {
        scan(useTraitStatementPart.getName());
    }

    @Override
    public void visit(YieldExpression node) {
        scan(node.getKey());
        scan(node.getValue());
    }

    @Override
    public void visit(YieldFromExpression node) {
        scan(node.getExpr());
    }

    @Override
    public void visit(AnonymousObjectVariable node) {
        scan(node.getName());
    }

    @Override
    public void visit(DereferencedArrayAccess node) {
        scan(node.getDispatcher());
        scan(node.getDimension());
    }

    @Override
    public void visit(ArrayDimension node) {
        scan(node.getIndex());
    }

    @Override
    public void visit(HaltCompiler node) {
    }

}
