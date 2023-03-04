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
package org.netbeans.modules.php.editor.parser.astnodes;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 * Represents conditional expression
 * Holds the condition, if true expression and if false expression
 * each on e can be any expression
 * <pre>e.g.
 * (bool) $a ? 3 : 4
 * $a > 0 ? $a : -$a
 * $a > 0 ?: -$a
 * $a > 0 ?? -$a
 * </pre>
 */
public class ConditionalExpression extends Expression {

    public enum OperatorType {
        QUESTION_MARK("?") { // NOI18N
            @Override
            public boolean isOperatorToken(Token<PHPTokenId> token) {
                throw new IllegalStateException();
            }
        },
        ELVIS("?:", true) { // NOI18N
            @Override
            public boolean isOperatorToken(Token<PHPTokenId> token) {
                // XXX introduce token for elvis
                assert false;
                return false;
            }
        },
        COALESCE("??", true) { // NOI18N
            @Override
            public boolean isOperatorToken(Token<PHPTokenId> token) {
                return token.id() == PHPTokenId.PHP_OPERATOR
                        && TokenUtilities.textEquals("??", token.text()); // NOI18N
            }
        };

        private final String operatorSign;
        private final boolean shortened;


        private OperatorType(String operatorSign) {
            this(operatorSign, false);
        }

        private OperatorType(String operatorSign, boolean shortened) {
            this.operatorSign = operatorSign;
            this.shortened = shortened;
        }

        public abstract boolean isOperatorToken(Token<PHPTokenId> token);

        public boolean isShortened() {
            return shortened;
        }

        @Override
        public String toString() {
            return operatorSign;
        }

    }


    private final Expression condition;
    private final OperatorType operator;
    private final Expression ifTrue;
    private final Expression ifFalse;


    public ConditionalExpression(int start, int end, Expression condition, OperatorType operator, Expression ifTrue, Expression ifFalse) {
        super(start, end);

        if (condition == null || ifFalse == null) {
            throw new IllegalArgumentException();
        }
        this.condition = condition;
        this.operator = operator;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    /**
     * Returns the condition of this conditional expression.
     *
     * @return the condition node
     */
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * Returns the operator of this conditional expression.
     *
     * @return the conditional operator
     */
    public OperatorType getOperator() {
        return operator;
    }

    /**
     * Returns the "then" part of this conditional expression.
     *
     * @return the "then" expression node
     */
    public Expression getIfTrue() {
        return ifTrue;
    }

    /**
     * Returns the "else" part of this conditional expression.
     *
     * @return the "else" expression node
     */
    public Expression getIfFalse() {
        return this.ifFalse;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getCondition() + " ? " + getIfTrue() + " : " + getIfFalse(); //NOI18N
    }

}
