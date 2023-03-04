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

/**
 * Represents an unary operation expression
 * <pre>e.g.<pre> +$a,
 * -3,
 * -foo(),
 * +-+-$a
 */
public class UnaryOperation extends Expression {

    public enum Operator {
        PLUS("+"), //NOI18N
        MINUS("-"), //NOI18N
        NOT("!"), //NOI18N
    	TILDA("~"); //NOI18N

        private final String operatorSign;

        private Operator(String operatorSign) {
            this.operatorSign = operatorSign;
        }

        @Override
        public String toString() {
            return operatorSign;
        }
    }

    private Expression expression;
    private UnaryOperation.Operator operator;

    public UnaryOperation(int start, int end, Expression expr, UnaryOperation.Operator operator) {
        super(start, end);

        if (expr == null) {
            throw new IllegalArgumentException();
        }
        this.expression = expr;
        this.operator = operator;
    }

    /**
     * Returns the expression of this unary operation.
     *
     * @return the expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * the operation type - one of {@link #OP_MINUS}, {@link #OP_NOT},
     * {@link #OP_PLUS}, {@link #OP_TILDA}
     * @return operation type
     */
    public UnaryOperation.Operator getOperator() {
        return operator;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getOperator() + getExpression(); //NOI18N
    }

}
