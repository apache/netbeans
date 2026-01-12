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
 * Represents a function composition expression
 * chaining multiple callables from left to right
 * It's marked by the pipe operator "|>"
 * @see https://wiki.php.net/rfc/pipe-operator-v3
 * <pre>
 * $a |> strtoupper(...)
 * $a |> trim(...)
 * </pre>
 * 
 */
public class CompositionExpression extends Expression {

    /**
     * Operator type used for the composition expression
     * possible rfc with using "+" for composition
     * https://wiki.php.net/rfc/function-composition
     */
    public enum OperatorType { 
        PIPE("|>"); // NOI18N

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
    private CompositionExpression.OperatorType operator;
    private Expression right;

    public CompositionExpression(int start, int end, Expression left, CompositionExpression.OperatorType operator, Expression right) {
        super(start, end);

        if (right == null || left == null) {
            throw new IllegalArgumentException();
        }

        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    /**
     * Returns the operator of this composition expression.
     *
     * @return the composition operator
     */
    public CompositionExpression.OperatorType getOperator() {
        return this.operator;
    }

    /**
     * Returns the left expression of this composition expression.
     *
     * @return the left expression node
     */
    public Expression getLeft() {
        return this.left;
    }

    /**
     * Returns the right expression of this composition expression.
     *
     * @return the right expression node
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
