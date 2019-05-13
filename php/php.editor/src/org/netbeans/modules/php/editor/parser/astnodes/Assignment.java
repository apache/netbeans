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
 * Represents an assignment statement.
 * e.g.
 * <pre>
 * $a = 5,
 * $a += 5,
 * $a .= $b,
 * </pre>
 */
public class Assignment extends Expression {

    public enum Type {
        EQUAL("="), //NOI18N
        PLUS_EQUAL("+="), //NOI18N
        MINUS_EQUAL("-="), //NOI18N
        MUL_EQUAL("*="), //NOI18N
        DIV_EQUAL("/="), //NOI18N
        CONCAT_EQUAL(".="), //NOI18N
        MOD_EQUAL("%="), //NOI18N
        AND_EQUAL("&="), //NOI18N
        OR_EQUAL("|="), //NOI18N
        XOR_EQUAL("^="), //NOI18N
        SL_EQUAL("<<="), //NOI18N
        SR_EQUAL(">>="), //NOI18N
        POW_EQUAL("**="), //NOI18N
        COALESCE_EQUAL("??="); //NOI18N PHP 7.4

        private final String operator;

        private Type(String operator) {
            this.operator = operator;
        }

        @Override
        public String toString() {
            return operator;
        }
    }

    private VariableBase leftHandSide;
    private Assignment.Type operator;
    private Expression rightHandSide;

    public Assignment(int start, int end, VariableBase leftHandSide, Assignment.Type operator, Expression rightHandSide) {
        super(start, end);
        if (leftHandSide == null || rightHandSide == null) {
            throw new IllegalArgumentException();
        }
        this.leftHandSide = leftHandSide;
        this.operator = operator;
        this.rightHandSide = rightHandSide;
//        leftHandSide.setParent(this);
//        rightHandSide.setParent(this);
    }

    /**
     * Returns the operator of this assignment expression.
     *
     * @return the assignment operator
     */
    public Assignment.Type getOperator() {
        return this.operator;
    }

    /**
     * Returns the left hand side of this assignment expression.
     *
     * @return the left hand side node
     */
    public VariableBase getLeftHandSide() {
        return this.leftHandSide;
    }

    /**
     * Returns the right hand side of this assignment expression.
     *
     * @return the right hand side node
     */
    public Expression getRightHandSide() {
        return this.rightHandSide;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getLeftHandSide() + " " + getOperator() + " " + getRightHandSide(); //NOI18N
    }

}
