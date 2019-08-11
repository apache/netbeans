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
 * Represents an infix expression.
 * e.g.
 * <pre>
 * $a + 1,
 * 3 - 2,
 * foo() * $a->bar(),
 * 'string'.$c
 * </pre>
 */
public class InfixExpression extends Expression {

    public enum OperatorType {
        IS_IDENTICAL("==="), //NOI18N
        IS_NOT_IDENTICAL("!=="), //NOI18N
        IS_EQUAL("=="), //NOI18N
    	IS_NOT_EQUAL("!="), //NOI18N
        RGREATER("<"), //NOI18N
        IS_SMALLER_OR_EQUAL("<="), //NOI18N
        LGREATER(">"), //NOI18N
        IS_GREATER_OR_EQUAL(">="), //NOI18N
        SPACESHIP("<=>"), //NOI18N
        BOOL_OR("||"), //NOI18N
        BOOL_AND("&&"), //NOI18N
        STRING_OR("or"), //NOI18N
    	STRING_AND("and"), //NOI18N
        STRING_XOR("xor"), //NOI18N
        OR("|"), //NOI18N
        AND("&"), //NOI18N
        XOR("^"), //NOI18N
        CONCAT("."), //NOI18N
        PLUS("+"), //NOI18N
        MINUS("-"), //NOI18N
    	MUL("*"), //NOI18N
        DIV("/"), //NOI18N
        MOD("%"), //NOI18N
        SL("<<"), //NOI18N
    	SR(">>"), //NOI18N
    	POW("**"); //NOI18N

        private final String operatorSign;

        private OperatorType(final String operatorSign) {
            this.operatorSign = operatorSign;
        }

        @Override
        public String toString() {
            return operatorSign;
        }
    }

    private Expression left;
    private InfixExpression.OperatorType operator;
    private Expression right;

    public InfixExpression(int start, int end, Expression left, InfixExpression.OperatorType operator, Expression right) {
        super(start, end);

        if (right == null || left == null ) {
            throw new IllegalArgumentException();
        }

        this.left = left;
        this.right = right;
        this.operator = operator;
    }



    /**
     * Returns the operator of this infix expression.
     *
     * @return the infix operator
     */
    public InfixExpression.OperatorType getOperator() {
        return this.operator;
    }

    /**
     * Returns the left operand of this infix expression.
     *
     * @return the left operand node
     */
    public Expression getLeft() {
        return this.left;
    }

    /**
     * Returns the right operand of this infix expression.
     *
     * @return the right operand node
     */
    public Expression getRight() {
        return this.right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getLeft() + " " + getOperator() + " " + getRight(); //NOI18N
    }

}
