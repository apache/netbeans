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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * Utility class used to check if an operator has precedence over another
 * operator and to see if parentheses are needed.
 * @author Ralph Ruijs
 */
public class OperatorPrecedence {

    private static final int ASSIGNMENT = 0; // += -= *= /= %= &= ^= |= <<= >>= >>>=
    private static final int TERNARY = 1; // ? :
    private static final int LOGICAL_OR = 2; // ||
    private static final int LOGICAL_AND = 3; // &&
    private static final int BITWISE_INCLUSIVE_OR = 4; // |
    private static final int BITWISE_EXCLUSIVE_OR = 5; // ^
    private static final int BITWISE_AND = 6; // &
    private static final int EQUALITY = 7; // == !=
    private static final int RATIONAL = 8; // < > <= >= instanceof
    private static final int SHIFT = 9; // << >> >>>
    private static final int ADDITIVE = 10; // + -
    private static final int MULTIPLICATIVE = 11; // * / %
    private static final int UNARY = 12; // ++expr --expr +expr -expr ~ !
    private static final int POSTFIX = 13; // expr++ expr--
    
    /**
     * Returns the precedence of an operator. Higher is stronger precedence.
     * @param kind, the operator kind
     * @return operator precedence value
     */
    public static int getOperatorPrecedence(Tree.Kind kind) {
        switch (kind) {
            case AND:
                return BITWISE_AND;
            case BITWISE_COMPLEMENT:
                return UNARY;
            case CONDITIONAL_AND:
                return LOGICAL_AND;
            case CONDITIONAL_EXPRESSION:
                return TERNARY;
            case CONDITIONAL_OR:
                return LOGICAL_OR;
            case DIVIDE:
                return MULTIPLICATIVE;
            case EQUAL_TO:
                return EQUALITY;
            case GREATER_THAN:
                return RATIONAL;
            case INSTANCE_OF:
                return RATIONAL;
            case LEFT_SHIFT:
                return SHIFT;
            case LESS_THAN:
                return RATIONAL;
            case LESS_THAN_EQUAL:
                return RATIONAL;
            case LOGICAL_COMPLEMENT:
                return UNARY;
            case MINUS:
                return ADDITIVE;
            case MULTIPLY:
                return MULTIPLICATIVE;
            case NOT_EQUAL_TO:
                return EQUALITY;
            case OR:
                return BITWISE_INCLUSIVE_OR;
            case PLUS:
                return ADDITIVE;
            case POSTFIX_DECREMENT:
                return POSTFIX;
            case POSTFIX_INCREMENT:
                return POSTFIX;
            case PREFIX_DECREMENT:
                return UNARY;
            case PREFIX_INCREMENT:
                return UNARY;
            case REMAINDER:
                return MULTIPLICATIVE;
            case RIGHT_SHIFT:
                return SHIFT;
            case UNARY_MINUS:
                return UNARY;
            case UNARY_PLUS:
                return UNARY;
            case UNSIGNED_RIGHT_SHIFT:
                return SHIFT;
            case XOR:
                return BITWISE_EXCLUSIVE_OR;
            default:
                return 14;
        }
    }

    /**
     * Check to see if parentheses are needed for substitution.
     * @param location, the location to substitute
     * @param expressionToFind, the expression to substitute
     * @param expression, the expression to substitute with
     * @return false when certain not needed, true otherwise
     */
    public static boolean needsParentheses(TreePath location, ExpressionTree expressionToFind, ExpressionTree expression, WorkingCopy workingcopy) {
        Trees trees = workingcopy.getTrees();
        CompilationUnitTree cut = workingcopy.getCompilationUnit();
        Element elementToFind = trees.getElement(trees.getPath(cut, expressionToFind));
        return needsParentheses(location, elementToFind, expression, workingcopy);
    }

    /**
     * Check to see if parentheses are needed for substitution.
     * @param location, the location to substitute
     * @param elementToFind, the element to substitute
     * @param expression, the expression to substitute with
     * @return false when certain not needed, true otherwise
     */
    public static boolean needsParentheses(TreePath location, Element elementToFind, ExpressionTree expression, WorkingCopy workingcopy) {
        Trees trees = workingcopy.getTrees();
        CompilationUnitTree cut = workingcopy.getCompilationUnit();
        
        if (!needsParentheses(expression)) {
            return false;
        }

        TreePath parentPath = location.getParentPath();
        Tree parent = parentPath.getLeaf();
        switch (parent.getKind()) {
            case PARENTHESIZED:
            case METHOD_INVOCATION:
            case VARIABLE:
            case RETURN:
            case ASSIGNMENT:
                return false;
        }
        if(parent.getKind() == Tree.Kind.PLUS && expression.getKind() == Tree.Kind.PLUS) {
            if (((BinaryTree) expression).getLeftOperand().getKind() == Tree.Kind.STRING_LITERAL
                    || ((BinaryTree) parent).getLeftOperand().getKind() == Tree.Kind.STRING_LITERAL
                    || ((BinaryTree) expression).getRightOperand().getKind() == Tree.Kind.STRING_LITERAL
                    || ((BinaryTree) parent).getRightOperand().getKind() ==Tree.Kind.STRING_LITERAL) {
                return true;
            }
        }
        if(parent.getKind() == Tree.Kind.MINUS) {
            ExpressionTree rightOperand = ((BinaryTree) parent).getRightOperand();
            Element rightElement = trees.getElement(trees.getPath(cut, rightOperand));
            
            if(elementToFind != null && elementToFind.equals(rightElement)) {
                return true;
            }
        }

        switch (parent.getKind()) {
            case PLUS:
            case AND:
            case BITWISE_COMPLEMENT:
            case CONDITIONAL_AND:
            case CONDITIONAL_EXPRESSION:
            case CONDITIONAL_OR:
            case DIVIDE:
            case EQUAL_TO:
            case GREATER_THAN:
            case INSTANCE_OF:
            case LEFT_SHIFT:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
            case LOGICAL_COMPLEMENT:
            case MINUS:
            case MULTIPLY:
            case NOT_EQUAL_TO:
            case OR:
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
            case REMAINDER:
            case RIGHT_SHIFT:
            case UNARY_MINUS:
            case UNARY_PLUS:
            case UNSIGNED_RIGHT_SHIFT:
            case XOR:
                int substitutePrecedence = OperatorPrecedence.getOperatorPrecedence(expression.getKind());
                int locationPrecedence = OperatorPrecedence.getOperatorPrecedence(parent.getKind());
                if (substitutePrecedence >= locationPrecedence) {
                    return false;
                }
        }
        return true;
    }

    public static boolean needsParentheses(Tree expression) {
        switch (expression.getKind()) {
            case INT_LITERAL:
            case LONG_LITERAL:
            case FLOAT_LITERAL:
            case DOUBLE_LITERAL:
            case BOOLEAN_LITERAL:
            case CHAR_LITERAL:
            case NULL_LITERAL:
            case IDENTIFIER:
            case NEW_ARRAY:
            case NEW_CLASS:
            case METHOD_INVOCATION:
            case STRING_LITERAL:
            case PARENTHESIZED:
                return false;
            default:
                return true;
        }
    }
}
