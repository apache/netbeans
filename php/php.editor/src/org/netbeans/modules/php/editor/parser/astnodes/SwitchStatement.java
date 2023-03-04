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
 * Represents a switch statement.
 * <pre>e.g.<pre>
 * switch ($i) {
 *   case 0:
 *     echo "i equals 0";
 *     break;
 *   case 1:
 *     echo "i equals 1";
 *     break;
 *   default:
 *     echo "i not equals 0 or 1";
 *     break;
 * }
 */
public class SwitchStatement extends Statement {

    private Expression expression;
    private Block body;

    public SwitchStatement(int start, int end, Expression expression, Block body) {
        super(start, end);

        if (expression == null || body == null) {
            throw new IllegalArgumentException();
        }

        this.expression = expression;
        this.body = body;
    }

    /**
     * Returns the expression of this switch statement.
     *
     * @return the expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * @return the body component of this switch statement
     */
    public Block getBody() {
        return this.body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "switch (" + getExpression() + ")" + getBody(); //NOI18N
    }

}
