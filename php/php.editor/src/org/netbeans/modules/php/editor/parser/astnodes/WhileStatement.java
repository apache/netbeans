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
 * Represents while statement.
 * <pre>e.g.<pre>
 * while (expr)
 *   statement;
 *
 * while (expr):
 *   statement
 *   ...
 * endwhile;
 */
public class WhileStatement extends Statement {

    private Expression condition;
    private Statement body;

    public WhileStatement(int start, int end, Expression condition, Statement action) {
        super(start, end);

        if (condition == null || action == null) {
            throw new IllegalArgumentException();
        }

        this.condition = condition;
        this.body = action;
    }

    /**
     * @return the body component of this while statement
     */
    public Statement getBody() {
        return this.body;
    }

    /**
     * Returns the condition expression of this while statement.
     *
     * @return the expression node
     */
    public Expression getCondition() {
        return this.condition;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "while (" + getCondition() + ")" + getBody(); //NOI18N
    }

}
