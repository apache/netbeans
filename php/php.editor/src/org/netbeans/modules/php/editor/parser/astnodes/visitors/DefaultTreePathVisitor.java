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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BreakStatement;
import org.netbeans.modules.php.editor.parser.astnodes.CastExpression;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.CloneExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EchoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FinallyClause;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IgnoreError;
import org.netbeans.modules.php.editor.parser.astnodes.InLineHtml;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
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
import org.netbeans.modules.php.editor.parser.astnodes.ThrowStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.YieldExpression;
import org.netbeans.modules.php.editor.parser.astnodes.YieldFromExpression;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class DefaultTreePathVisitor extends DefaultVisitor {

    // @GuardedBy(path)
    private final List<ASTNode> path = new LinkedList<>();

    /**
     * Reversed order.
     *
     * @return
     */
    public List<ASTNode> getPath() {
        synchronized (path) {
            return new LinkedList<>(path);
        }
    }

    @Override
    public void visit(NamespaceDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(UseStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(SingleUseStatementPart node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(GroupUseStatementPart node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ArrayAccess node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ArrayCreation node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ArrayElement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ArrowFunctionDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Assignment node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ASTError astError) {
        super.visit(astError);
    }

    @Override
    public void visit(ASTErrorExpression astErrorExpression) {
        super.visit(astErrorExpression);
    }

    @Override
    public void visit(BackTickExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Block node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(BreakStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(CastExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(CatchClause node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ConstantDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ClassDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ClassName node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(CloneExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Comment node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ConditionalExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ContinueStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(DeclareStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(DoStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(EchoStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(EmptyStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ExpressionStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FieldAccess node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FieldsDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FinallyClause node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ForEachStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FormalParameter node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ForStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FunctionDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FunctionInvocation node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(FunctionName node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(GlobalStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Identifier node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(IfStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(IgnoreError node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Include node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(InfixExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(InLineHtml node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(InstanceOfExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ListVariable node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(MethodDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(MethodInvocation node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ParenthesisExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PostfixExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PrefixExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Program node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Quote node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Reference node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ReflectionVariable node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ReturnStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Scalar node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(StaticConstantAccess node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(StaticFieldAccess node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(StaticStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(SwitchCase node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(SwitchStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ThrowStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(TryStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(UnaryOperation node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(Variable node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(WhileStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(ASTNode node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PHPDocBlock node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PHPDocTypeTag node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PHPDocTag node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PHPDocVarTypeTag node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PHPDocMethodTag node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(PHPDocNode node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(TraitDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(TraitMethodAliasDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(TraitConflictResolutionDeclaration node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(UnpackableArrayElement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(UseTraitStatement node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(UseTraitStatementPart node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(YieldExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(YieldFromExpression node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    @Override
    public void visit(AnonymousObjectVariable node) {
        addToPath(node);
        super.visit(node);
        removeFromPath();
    }

    protected void addToPath(ASTNode node) {
        if (node != null) {
            synchronized (path) {
                path.add(0, node);
            }
        }
    }

    protected void removeFromPath() {
        synchronized (path) {
            path.remove(0);
        }
    }
}
