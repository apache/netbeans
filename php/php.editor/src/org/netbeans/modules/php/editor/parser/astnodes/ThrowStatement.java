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
 * Represent a break statement
 *
 * <pre>
 * e.g.&lt;pre&gt; throw $exceptionClass;
 *
 */
public class ThrowStatement extends Statement {

    private Expression expression;

    public ThrowStatement(int start, int end, Expression expr) {
        super(start, end);

        if (expr == null) {
            throw new IllegalArgumentException();
        }
        this.expression = expr;
    }

    /**
     * Returns the expression of this throw statement.
     *
     * @return the expression node
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "throw " + getExpression(); //NOI18N
    }

}
