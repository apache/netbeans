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
 * Represent do while statement.
 * <pre>e.g.<pre>
 * do {
 *   echo $i;
 * } while ($i > 0);
 */
public class DoStatement extends Statement {

    private Expression condition;
    private Statement body;

    public DoStatement(int start, int end, Expression condition, Statement body) {
        super(start, end);

        if (condition == null || body == null) {
            throw new IllegalArgumentException();
        }
        this.condition = condition;
        this.body = body;
    }

    /**
     * @return the body component of this do statement
     */
    public Statement getBody() {
        return this.body;
    }

    /**
     * Returns the condition expression of this do statement.
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
        return "do " + getBody() + " while (" + getCondition() + ")"; //NOI18N
    }

}
