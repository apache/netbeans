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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import java.util.List;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;


/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class TreeDuplicator {

    private final TreeMaker make;
    private final GeneratorUtilities genUtils;

    public TreeDuplicator(TreeMaker make, GeneratorUtilities genUtils) {
        this.make = make;
        this.genUtils = genUtils;
    }

    public <T extends Tree> T duplicate(T tree) {
        T result = tree;
        switch (tree.getKind()) {
            case BREAK:{
                BreakTree t = (BreakTree) tree;
                result = make.setLabel(tree, t.getLabel());
                break;
            }
            case CLASS:
            case INTERFACE:
            case ENUM:
            case RECORD:
            case ANNOTATION_TYPE:{
                ClassTree t = (ClassTree) tree;
                result = make.setLabel(tree, t.getSimpleName());
                break;
            }
            case CONTINUE:{
                ContinueTree t = (ContinueTree) tree;
                result = make.setLabel(tree, t.getLabel());
                break;
            }
            case IDENTIFIER:{
                IdentifierTree t = (IdentifierTree) tree;
                result = make.setLabel(tree, t.getName());
                break;
            }
            case LABELED_STATEMENT:{
                LabeledStatementTree t = (LabeledStatementTree) tree;
                result = make.setLabel(tree, t.getLabel());
                break;
            }
            case MEMBER_SELECT:{
                MemberSelectTree t = (MemberSelectTree) tree;
                result = make.setLabel(tree, t.getIdentifier());
                break;
            }
            case METHOD:{
                MethodTree t = (MethodTree) tree;
                result = make.setLabel(tree, t.getName());
                break;
            }
            case TYPE_PARAMETER:{
                TypeParameterTree t = (TypeParameterTree) tree;
                result = make.setLabel(tree, t.getName());
                break;
            }
            case VARIABLE: {
                VariableTree mrt = (VariableTree) tree;
                result = make.setLabel(tree, mrt.getName());
                break;
            }
            case MEMBER_REFERENCE: {
                MemberReferenceTree mrt = (MemberReferenceTree) tree;
                result = make.setLabel(tree, mrt.getName());
                break;
            }
            case ANNOTATION: {
                AnnotationTree t = (AnnotationTree) tree;
                result = (T) make.Annotation(t.getAnnotationType(), t.getArguments());
                break;
            }
            case TYPE_ANNOTATION: {
                AnnotationTree t = (AnnotationTree) tree;
                result = (T) make.Annotation(t.getAnnotationType(), t.getArguments());
                break;
            }
            case ARRAY_ACCESS: {
                ArrayAccessTree t = (ArrayAccessTree) tree;
                result = (T) make.ArrayAccess(t.getIndex(), t.getIndex());
                break;
            }
            case ARRAY_TYPE: {
                ArrayTypeTree t = (ArrayTypeTree) tree;
                result = (T) make.ArrayType(t.getType());
                break;
            }
            case ASSERT: {
                AssertTree t = (AssertTree) tree;
                result = (T) make.Assert(t.getCondition(), t.getDetail());
                break;
            }
            case ASSIGNMENT: {
                AssignmentTree t = (AssignmentTree) tree;
                result = (T) make.Assignment(t.getVariable(), t.getExpression());
                break;
            }
            case BLOCK: {
                BlockTree t = (BlockTree) tree;
                result = (T) make.Block(t.getStatements(), t.isStatic());
                break;
            }
            case CASE: {
                CaseTree t = (CaseTree) tree;
                result = (T) make.Case(t.getExpression(), t.getStatements());
                break;
            }
            case CATCH: {
                CatchTree t = (CatchTree) tree;
                result = (T) make.Catch(t.getParameter(), t.getBlock());
                break;
            }
            case CONDITIONAL_EXPRESSION: {
                ConditionalExpressionTree t = (ConditionalExpressionTree) tree;
                result = (T) make.ConditionalExpression(t.getCondition(), t.getTrueExpression(), t.getFalseExpression());
                break;
            }
            case DO_WHILE_LOOP: {
                DoWhileLoopTree t = (DoWhileLoopTree) tree;
                result = (T) make.DoWhileLoop(t.getCondition(), t.getStatement());
                break;
            }
            case ENHANCED_FOR_LOOP: {
                EnhancedForLoopTree t = (EnhancedForLoopTree) tree;
                result = (T) make.EnhancedForLoop(t.getVariable(), t.getExpression(), t.getStatement());
                break;
            }
            case EXPRESSION_STATEMENT: {
                ExpressionStatementTree t = (ExpressionStatementTree) tree;
                result = (T) make.ExpressionStatement(t.getExpression());
                break;
            }
            case FOR_LOOP: {
                ForLoopTree t = (ForLoopTree) tree;
                result = (T) make.ForLoop(t.getInitializer(), t.getCondition(), t.getUpdate(), t.getStatement());
                break;
            }
            case IF: {
                IfTree t = (IfTree) tree;
                result = (T) make.If(t.getCondition(), t.getThenStatement(), t.getElseStatement());
                break;
            }
            case IMPORT: {
                ImportTree t = (ImportTree) tree;
                result = (T) make.Import(t.getQualifiedIdentifier(), t.isStatic());
                break;
            }
            case INSTANCE_OF: {
                InstanceOfTree t = (InstanceOfTree) tree;
                result = (T) make.InstanceOf(t.getExpression(), t.getType());
                break;
            }
            case METHOD_INVOCATION: {
                MethodInvocationTree t = (MethodInvocationTree) tree;
                result = (T) make.MethodInvocation((List<? extends ExpressionTree>) t.getTypeArguments(), t.getMethodSelect(), t.getArguments());
                break;
            }
            case MODIFIERS: {
                ModifiersTree t = (ModifiersTree) tree;
                result = (T) make.Modifiers(t.getFlags(), t.getAnnotations());
                break;
            }
            case NEW_ARRAY: {
                NewArrayTree t = (NewArrayTree) tree;
                result = (T) make.NewArray(t.getType(), t.getDimensions(), t.getInitializers());
                break;
            }
            case NEW_CLASS: {
                NewClassTree t = (NewClassTree) tree;
                result =  (T) make.NewClass(t.getEnclosingExpression(), (List<? extends ExpressionTree>) t.getTypeArguments(), t.getIdentifier(), t.getArguments(), t.getClassBody());
                break;
            }
            case LAMBDA_EXPRESSION: {
                LambdaExpressionTree t = (LambdaExpressionTree) tree;
                result = (T) make.LambdaExpression(t.getParameters(), t.getBody());
                break;
            }
            case PARENTHESIZED: {
                ParenthesizedTree t = (ParenthesizedTree) tree;
                result = (T) make.Parenthesized(t.getExpression());
                break;
            }
            case PRIMITIVE_TYPE: {
                PrimitiveTypeTree t = (PrimitiveTypeTree) tree;
                result = (T) make.PrimitiveType(t.getPrimitiveTypeKind());
                break;
            }
            case RETURN: {
                ReturnTree t = (ReturnTree) tree;
                result = (T) make.Return(t.getExpression());
                break;
            }
            case EMPTY_STATEMENT: {
                result = (T) make.EmptyStatement();
                break;
            }
            case SWITCH: {
                SwitchTree t = (SwitchTree) tree;
                result = (T) make.Switch(t.getExpression(), t.getCases());
                break;
            }
            case SYNCHRONIZED: {
                SynchronizedTree t = (SynchronizedTree) tree;
                result = (T) make.Synchronized(t.getExpression(), t.getBlock());
                break;
            }
            case THROW: {
                ThrowTree t = (ThrowTree) tree;
                result = (T) make.Throw(t.getExpression());
                break;
            }
            case TRY: {
                TryTree t = (TryTree) tree;
                result = (T) make.Try(t.getResources(), t.getBlock(), t.getCatches(), t.getFinallyBlock());
                break;
            }
            case PARAMETERIZED_TYPE: {
                ParameterizedTypeTree t = (ParameterizedTypeTree) tree;
                result = (T) make.ParameterizedType(t.getType(), t.getTypeArguments());
                break;
            }
            case UNION_TYPE: {
                UnionTypeTree t = (UnionTypeTree) tree;
                result = (T) make.UnionType(t.getTypeAlternatives());
                break;
            }
            case TYPE_CAST: {
                TypeCastTree t = (TypeCastTree) tree;
                result = (T) make.TypeCast(t.getType(), t.getExpression());
                break;
            }
            case WHILE_LOOP: {
                WhileLoopTree t = (WhileLoopTree) tree;
                result = (T) make.WhileLoop(t.getCondition(), t.getStatement());
                break;
            }
            case BITWISE_COMPLEMENT:
            case LOGICAL_COMPLEMENT:
            case POSTFIX_INCREMENT:
            case POSTFIX_DECREMENT:
            case PREFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case UNARY_PLUS:
            case UNARY_MINUS: {
                UnaryTree t = (UnaryTree) tree;
                result = (T) make.Unary(t.getKind(), t.getExpression());
                break;
            }
            case MULTIPLY:
            case DIVIDE:
            case REMAINDER:
            case PLUS:
            case MINUS:
            case LEFT_SHIFT:
            case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_EQUAL:
            case GREATER_THAN_EQUAL:
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case AND:
            case XOR:
            case OR:
            case CONDITIONAL_AND:
            case CONDITIONAL_OR: {
                BinaryTree t = (BinaryTree) tree;
                result = (T) make.Binary(t.getKind(), t.getLeftOperand(), t.getRightOperand());
                break;
            }
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case AND_ASSIGNMENT:
            case XOR_ASSIGNMENT:
            case OR_ASSIGNMENT:{
                CompoundAssignmentTree t = (CompoundAssignmentTree) tree;
                result = (T) make.CompoundAssignment(t.getKind(), t.getVariable(), t.getExpression());
                break;
            }
            case INT_LITERAL:
            case LONG_LITERAL:
            case FLOAT_LITERAL:
            case DOUBLE_LITERAL:
            case BOOLEAN_LITERAL:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case NULL_LITERAL: {
                LiteralTree t = (LiteralTree) tree;
                result = (T) make.Literal(t.getValue());
                break;
            }
            case UNBOUNDED_WILDCARD:
            case EXTENDS_WILDCARD:
            case SUPER_WILDCARD: {
                WildcardTree t = (WildcardTree) tree;
                result = (T) make.Wildcard(t.getKind(), t.getBound());
                break;
            }
            default:
                // do nothing;
                break;
        }
        genUtils.copyComments(tree, result, true);
        genUtils.copyComments(tree, result, false);
        return result;
    }
}
