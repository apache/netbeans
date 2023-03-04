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
 * Represents a prefix expression
 * <pre>e.g.<pre> --$a,
 * --foo()
 */
public class PrefixExpression extends Expression {

    public enum Operator {
        INC("++"), //NOI18N
        DEC("--"); //NOI18N

        private final String operatorSign;

        private Operator(final String operatorSign) {
            this.operatorSign = operatorSign;
        }

        @Override
        public String toString() {
            return operatorSign;
        }
    }

    private VariableBase variable;
    private PrefixExpression.Operator operator;

    public PrefixExpression(int start, int end, VariableBase variable, PrefixExpression.Operator operator) {
        super(start, end);

        if (variable == null) {
            throw new IllegalArgumentException();
        }

        this.variable = variable;
        this.operator = operator;
    }


    /**
     * Returns the operator of this prefix expression.
     *
     * @return the prefix operator
     */
    public PrefixExpression.Operator getOperator() {
        return this.operator;
    }

    /**
     * Returns the variable in the prefix expression.
     *
     * @return the expression node
     */
    public VariableBase getVariable() {
        return variable;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getOperator() + getVariable(); //NOI18N
    }

}
